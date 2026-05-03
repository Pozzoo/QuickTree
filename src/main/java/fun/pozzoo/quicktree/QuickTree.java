package fun.pozzoo.quicktree;

import fun.pozzoo.quicktree.events.BlockListener;
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
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();

        woodManager = new WoodManager();
        storageManager = new StorageManager(instance);
        metrics = new Metrics(instance, 22027);
        new BlockListener(instance);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        metrics.shutdown();
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
