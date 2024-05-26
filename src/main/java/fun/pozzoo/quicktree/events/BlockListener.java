package fun.pozzoo.quicktree.events;

import fun.pozzoo.quicktree.QuickTree;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.MainHand;


public class BlockListener implements Listener {

    public BlockListener(QuickTree plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(QuickTree.getInstance().getWoodManager().isWoodenLogs(event.getBlock().getType()))) return;
        if (event.getPlayer().isSneaking()) return;

        event.setCancelled(true);

        Location location = event.getBlock().getLocation();

        QuickTree.getInstance().getWoodManager().createTree(location);
        QuickTree.getInstance().getWoodManager().createTreeDisplay(location);

        event.getPlayer().damageItemStack(EquipmentSlot.HAND, QuickTree.getInstance().getWoodManager().getTree(location).getTreeModel().size());
        QuickTree.getInstance().getWoodManager().destroyTree(location);
    }
}
