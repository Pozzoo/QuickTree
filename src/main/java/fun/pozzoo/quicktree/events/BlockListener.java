package fun.pozzoo.quicktree.events;

import fun.pozzoo.quicktree.QuickTree;
import fun.pozzoo.quicktree.utils.BlockTypeUtils;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;


public class BlockListener implements Listener {

    public BlockListener(QuickTree plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (BlockTypeUtils.isWoodenLogs(event.getBlock().getType()) || BlockTypeUtils.isKindaLeaves(event.getBlock().getType()))
            QuickTree.getInstance().getStorageManager().mark(event.getBlock());
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(BlockTypeUtils.isWoodenLogs(event.getBlock().getType()))) return;
        if (event.getPlayer().isSneaking()) return;

        if (QuickTree.getInstance().getStorageManager().isPlayerPlaced(event.getBlock())) {
            QuickTree.getInstance().getStorageManager().unmark(event.getBlock());
            return;
        }

        event.setCancelled(true);

        Location location = event.getBlock().getLocation();

        QuickTree.getInstance().getWoodManager().createTree(location);
        QuickTree.getInstance().getWoodManager().createTreeDisplay(location);

        event.getPlayer().damageItemStack(EquipmentSlot.HAND, QuickTree.getInstance().getWoodManager().getTree(location).getTreeModel().size());
        QuickTree.getInstance().getWoodManager().destroyTree(location, event.getPlayer().getLocation().getDirection());
    }
}
