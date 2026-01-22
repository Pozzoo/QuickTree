package fun.pozzoo.quicktree.managers;

import fun.pozzoo.quicktree.data.TrackedChunk;
import fun.pozzoo.quicktree.utils.BlockPosUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class StorageManager {

    private final Map<Long, TrackedChunk> chunkMap = new HashMap<>();
    private final Path dataFolder;

    public StorageManager(JavaPlugin plugin) {
        this.dataFolder = plugin.getDataFolder().toPath().resolve("chunks");
        try {
            Files.createDirectories(dataFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long chunkKey(Chunk chunk) {
        return (((long) chunk.getX()) << 32) ^ (chunk.getZ() & 0xffffffffL);
    }

    public void mark(Block block) {
        Chunk chunk = block.getChunk();
        long key = chunkKey(chunk);

        TrackedChunk tracked = chunkMap.computeIfAbsent(key, k -> new TrackedChunk());

        short packed = BlockPosUtil.pack(
                block.getX() & 0xF,
                block.getY(),
                block.getZ() & 0xF
        );

        tracked.add(packed);
    }

    public boolean isPlayerPlaced(Block block) {
        TrackedChunk tracked = chunkMap.get(chunkKey(block.getChunk()));
        if (tracked == null) return false;

        short packed = BlockPosUtil.pack(
                block.getX() & 0xF,
                block.getY(),
                block.getZ() & 0xF
        );

        return tracked.contains(packed);
    }

    public void unmark(Block block) {
        TrackedChunk tracked = chunkMap.get(chunkKey(block.getChunk()));
        if (tracked == null) return;

        short packed = BlockPosUtil.pack(
                block.getX() & 0xF,
                block.getY(),
                block.getZ() & 0xF
        );

        tracked.remove(packed);
    }

    public void saveChunk(Chunk chunk) {
        long key = chunkKey(chunk);
        TrackedChunk tracked = chunkMap.remove(key);

        if (tracked == null || tracked.isEmpty()) return;

        Path file = dataFolder.resolve(chunk.getWorld().getUID() + "_" + chunk.getX() + "_" + chunk.getZ() + ".bin");

        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(file))) {
            for (short pos : tracked.getAll()) {
                out.writeShort(pos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadChunk(Chunk chunk) {
        Path file = dataFolder.resolve(chunk.getWorld().getUID() + "_" + chunk.getX() + "_" + chunk.getZ() + ".bin");
        if (!Files.exists(file)) return;

        TrackedChunk tracked = new TrackedChunk();

        try (DataInputStream in = new DataInputStream(Files.newInputStream(file))) {
            while (in.available() > 0) {
                tracked.add(in.readShort());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        chunkMap.put(chunkKey(chunk), tracked);
    }

}
