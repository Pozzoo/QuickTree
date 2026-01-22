package fun.pozzoo.quicktree.events;

import fun.pozzoo.quicktree.QuickTree;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {
    public ChunkListener(QuickTree plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        QuickTree.getInstance().getStorageManager().saveChunk(event.getChunk());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        QuickTree.getInstance().getStorageManager().loadChunk(event.getChunk());
    }
}
