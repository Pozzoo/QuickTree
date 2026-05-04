package fun.pozzoo.quicktree.events;

import fun.pozzoo.quicktree.QuickTree;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {

    private final QuickTree plugin;

    public ChunkListener(QuickTree plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        plugin.getStorageManager().saveChunk(event.getChunk());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        plugin.getStorageManager().loadChunk(event.getChunk());
    }
}
