package com.github.pozzoo.quicktree.Listeners;

import com.github.pozzoo.quicktree.QuickTree;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListener implements Listener {

    public BlockListener(QuickTree plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(QuickTree.getInstance().getWoodManager().isWoodenLogs(event.getBlock().getType()))) return;
        if (event.getPlayer().isSneaking()) return;

        event.setCancelled(true);

        Location location = event.getBlock().getLocation();

        QuickTree.getInstance().getWoodManager().checkAround(location);

        QuickTree.getInstance().getWoodManager().createTree();

        event.getPlayer().getInventory().getItemInMainHand().damage(QuickTree.getInstance().getWoodManager().getTreeModelSet().size(), event.getPlayer());

        QuickTree.getInstance().getWoodManager().destroyTree();
    }

}
