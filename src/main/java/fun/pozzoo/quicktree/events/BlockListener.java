package fun.pozzoo.quicktree.events;

import fun.pozzoo.quicktree.QuickTree;
import fun.pozzoo.quicktree.data.Tree;
import fun.pozzoo.quicktree.utils.BlockTypeUtils;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;

public class BlockListener implements Listener {

    private final QuickTree plugin;

    public BlockListener(QuickTree plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (BlockTypeUtils.isWoodenLogs(event.getBlock().getType()) || BlockTypeUtils.isKindaLeaves(event.getBlock().getType())) plugin.getStorageManager().mark(event.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!BlockTypeUtils.isWoodenLogs(event.getBlock().getType())) return;

        var player = event.getPlayer();
        if (player.isSneaking()) return;

        if (plugin.getStorageManager().isPlayerPlaced(event.getBlock())) {
            plugin.getStorageManager().unmark(event.getBlock());
            return;
        }

        Location location = event.getBlock().getLocation();

        // If the log cluster includes player-placed blocks, do not override vanilla behavior.
        if (!plugin.getWoodManager().createTree(location)) return;

        event.setCancelled(true);

        plugin.getWoodManager().createTreeDisplay(location);

        Tree tree = plugin.getWoodManager().getTree(location);
        if (tree != null) player.damageItemStack(EquipmentSlot.HAND, tree.getTreeModel().size());

        plugin.getWoodManager().destroyTree(location, player.getLocation().getDirection());
    }
}
