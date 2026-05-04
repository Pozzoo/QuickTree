package fun.pozzoo.quicktree.managers;

import fun.pozzoo.quicktree.data.TrackedChunk;
import fun.pozzoo.quicktree.utils.BlockPosUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class StorageManager {

    private static final int FILE_MAGIC = 0x51545245; // "QTRE"
    private static final int MAX_ENTRIES_PER_CHUNK_FILE = 1_000_000;

    private static final class ChunkKey {
        private final UUID worldId;
        private final int x;
        private final int z;

        private ChunkKey(UUID worldId, int x, int z) {
            this.worldId = worldId;
            this.x = x;
            this.z = z;
        }

        public UUID worldId() {
            return worldId;
        }

        public int x() {
            return x;
        }

        public int z() {
            return z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChunkKey)) return false;
            ChunkKey chunkKey = (ChunkKey) o;
            return x == chunkKey.x && z == chunkKey.z && worldId.equals(chunkKey.worldId);
        }

        @Override
        public int hashCode() {
            int result = worldId.hashCode();
            result = 31 * result + x;
            result = 31 * result + z;
            return result;
        }
    }

    private final JavaPlugin plugin;
    private final Map<ChunkKey, TrackedChunk> chunkMap = new HashMap<>();
    private final Path dataFolder;

    public StorageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder().toPath().resolve("chunks");
        try {
            Files.createDirectories(dataFolder);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create chunks data directory: " + dataFolder, e);
        }
    }

    private ChunkKey chunkKey(Chunk chunk) {
        return new ChunkKey(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
    }

    private Path chunkFilePath(ChunkKey key) {
        return dataFolder.resolve(key.worldId() + "_" + key.x() + "_" + key.z() + ".bin");
    }

    public void mark(Block block) {
        ChunkKey key = chunkKey(block.getChunk());
        TrackedChunk tracked = chunkMap.get(key);
        if (tracked == null) {
            tracked = loadFromDisk(key);
            if (tracked == null) tracked = new TrackedChunk();
            chunkMap.put(key, tracked);
        }

        tracked.add(BlockPosUtil.pack(block.getX() & 0xF, block.getY(), block.getZ() & 0xF));
    }

    public boolean isPlayerPlaced(Block block) {
        ChunkKey key = chunkKey(block.getChunk());
        TrackedChunk tracked = chunkMap.get(key);
        if (tracked == null) {
            tracked = loadFromDisk(key);
            if (tracked != null) chunkMap.put(key, tracked);
        }
        if (tracked == null) return false;

        int packed = BlockPosUtil.pack(block.getX() & 0xF, block.getY(), block.getZ() & 0xF);
        return tracked.contains(packed);
    }

    public void unmark(Block block) {
        ChunkKey key = chunkKey(block.getChunk());
        TrackedChunk tracked = chunkMap.get(key);
        if (tracked == null) {
            tracked = loadFromDisk(key);
            if (tracked != null) chunkMap.put(key, tracked);
        }
        if (tracked == null) return;

        tracked.remove(BlockPosUtil.pack(block.getX() & 0xF, block.getY(), block.getZ() & 0xF));

        if (tracked.isEmpty()) {
            chunkMap.remove(key);
            Path file = chunkFilePath(key);
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to delete empty chunk tracking file: " + file, e);
            }
        }
    }

    public void saveChunk(Chunk chunk) {
        ChunkKey key = chunkKey(chunk);
        TrackedChunk tracked = chunkMap.get(key);
        if (tracked == null) return;

        Path file = chunkFilePath(key);

        if (tracked.isEmpty()) {
            try {
                Files.deleteIfExists(file);
                chunkMap.remove(key);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to delete empty chunk tracking file: " + file, e);
            }
            return;
        }

        if (writeTrackedFile(file, tracked)) chunkMap.remove(key);
    }

    public void loadChunk(Chunk chunk) {
        ChunkKey key = chunkKey(chunk);
        TrackedChunk tracked = loadFromDisk(key);
        if (tracked != null) chunkMap.put(key, tracked);
    }

    private TrackedChunk loadFromDisk(ChunkKey key) {
        Path file = chunkFilePath(key);
        if (!Files.exists(file)) return null;

        TrackedChunk tracked = new TrackedChunk();

        // Format: [int magic][int count][count * int packedPos]
        try (DataInputStream in = new DataInputStream(Files.newInputStream(file))) {
            int magic = in.readInt();
            if (magic != FILE_MAGIC) {
                plugin.getLogger().log(Level.WARNING, "Unsupported chunk tracking file format (bad magic). Deleting: " + file);
                Files.deleteIfExists(file);
                return null;
            }

            int count = in.readInt();
            if (count < 0 || count > MAX_ENTRIES_PER_CHUNK_FILE) {
                plugin.getLogger().log(Level.WARNING, "Corrupt chunk tracking file (bad count=" + count + "). Deleting: " + file);
                Files.deleteIfExists(file);
                return null;
            }

            for (int i = 0; i < count; i++) tracked.add(in.readInt());
        } catch (EOFException e) {
            plugin.getLogger().log(Level.WARNING, "Truncated chunk tracking file. Deleting: " + file, e);
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
            }
            return null;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load chunk tracking file: " + file, e);
            return null;
        }

        if (tracked.isEmpty()) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to delete empty chunk tracking file: " + file, e);
            }
            return null;
        }

        return tracked;
    }

    public void flushAll() {
        Iterator<Map.Entry<ChunkKey, TrackedChunk>> it = chunkMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ChunkKey, TrackedChunk> entry = it.next();
            ChunkKey key = entry.getKey();
            TrackedChunk tracked = entry.getValue();
            Path file = chunkFilePath(key);

            if (tracked == null || tracked.isEmpty()) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to delete empty chunk tracking file: " + file, e);
                }
                it.remove();
                continue;
            }

            writeTrackedFile(file, tracked);
            it.remove();
        }
    }

    private boolean writeTrackedFile(Path file, TrackedChunk tracked) {
        Path tmp = file.resolveSibling(file.getFileName().toString() + ".tmp");

        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(tmp))) {
            out.writeInt(FILE_MAGIC);
            out.writeInt(tracked.getAll().size());
            for (int pos : tracked.getAll()) out.writeInt(pos);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to write chunk tracking temp file: " + tmp, e);
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException ignored) {
            }
            return false;
        }

        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveFailed) {
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException moveFailed) {
                plugin.getLogger().log(Level.WARNING, "Failed to move chunk tracking file into place: " + file, moveFailed);
                try {
                    Files.deleteIfExists(tmp);
                } catch (IOException ignored) {
                }
                return false;
            }
        }

        return true;
    }
}
