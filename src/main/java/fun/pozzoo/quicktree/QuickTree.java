package fun.pozzoo.quicktree;

import fun.pozzoo.quicktree.events.BlockListener;
import fun.pozzoo.quicktree.events.ChunkListener;
import fun.pozzoo.quicktree.managers.StorageManager;
import fun.pozzoo.quicktree.managers.WoodManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class QuickTree extends JavaPlugin {
    private static QuickTree instance;
    private WoodManager woodManager;
    private StorageManager storageManager;
    private Metrics metrics;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        woodManager = new WoodManager(this);
        storageManager = new StorageManager(this);
        metrics = new Metrics(this, 22027);

        // Listener registration
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunkListener(this), this);
    }

    @Override
    public void onDisable() {
        if (woodManager != null) woodManager.shutdown();
        if (storageManager != null) storageManager.flushAll();
        if (metrics != null) metrics.shutdown();
        instance = null;
    }

    public static QuickTree getInstance() {
        return instance;
    }

    public WoodManager getWoodManager() {
        return woodManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }
}
