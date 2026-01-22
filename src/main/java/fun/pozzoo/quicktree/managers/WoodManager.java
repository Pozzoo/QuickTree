package fun.pozzoo.quicktree.managers;

import fun.pozzoo.quicktree.QuickTree;
import fun.pozzoo.quicktree.data.Tree;
import fun.pozzoo.quicktree.utils.ParticleUtils;
import fun.pozzoo.quicktree.utils.BlockTypeUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import java.util.*;


public class WoodManager {
    private final Map<Location, Tree> trees;
    private final Random random;

    private static final int[][] DIRECTIONS = {
            {-1,-1,-1},{-1,-1,0},{-1,-1,1},
            {-1, 0,-1},{-1, 0,0},{-1, 0,1},
            {-1, 1,-1},{-1, 1,0},{-1, 1,1},
            { 0,-1,-1},{ 0,-1,0},{ 0,-1,1},
            { 0, 0,-1},          { 0, 0,1},
            { 0, 1,-1},{ 0, 1,0},{ 0, 1,1},
            { 1,-1,-1},{ 1,-1,0},{ 1,-1,1},
            { 1, 0,-1},{ 1, 0,0},{ 1, 0,1},
            { 1, 1,-1},{ 1, 1,0},{ 1, 1,1}
    };


    public WoodManager() {
        trees = new HashMap<>();
        random = new Random();
    }

    public void destroyTree(Location initialLocation) {
        for (Location location : trees.get(initialLocation).getTreeModel()) {
            location.getBlock().breakNaturally(true);
        }

        trees.get(initialLocation).getTreeModel().clear();
        animateTree(random.nextInt(4), initialLocation);
    }

    public void createTree(Location location) {
        Tree tree = new Tree();
        Set<Location> logs = searchForLogs(location);

        tree.setTreeModel(logs);
        tree.setLeaves(searchForLeaves(logs));

        trees.put(location, tree);
    }

    public Set<Location> searchForLogs(Location start) {
        Set<Location> logs = new HashSet<>();
        Queue<Location> queue = new ArrayDeque<>();

        queue.add(start);
        logs.add(start);

        while (!queue.isEmpty()) {
            Location current = queue.poll();

            for (int[] d : DIRECTIONS) {
                Location next = current.getBlock().getRelative(d[0], d[1], d[2]).getLocation();

                if (!BlockTypeUtils.isWoodenLogs(next.getBlock().getType())) continue;
                if (logs.contains(next)) continue;

                // Structure protection
                if (QuickTree.getInstance().getStorageManager().isPlayerPlaced(next.getBlock())) {
                    return Set.of(); // abort entire felling
                }

                logs.add(next);
                queue.add(next);
            }

            if (logs.size() > 512) break; // safety cap
        }

        return logs;
    }

    public Set<Location> searchForLeaves(Set<Location> logs) {
        Set<Location> leaves = new HashSet<>();

        for (Location log : logs) {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dy = -4; dy <= 4; dy++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        Location b = log.getBlock().getRelative(dx, dy, dz).getLocation();

                        if (!BlockTypeUtils.isLeaves(b.getBlock().getType())) continue;

                        Leaves data = (Leaves) b.getBlock().getBlockData();
                        if (data.isPersistent()) continue;
                        if (data.getDistance() > 6) continue;

                        leaves.add(b);
                    }
                }
            }
        }
        return leaves;
    }


    public void createTreeDisplay(Location initialLocation) {
        Tree tree = trees.get(initialLocation);

        for (Location location : tree.getTreeModel()) {
            BlockDisplay blockDisplay = location.getWorld().spawn(location, BlockDisplay.class);
            blockDisplay.setBlock(location.getBlock().getBlockData());

            trees.get(initialLocation).getTreeDisplay().add(blockDisplay);
        }
    }

    private void animateTree(int direction, Location location) {
        explodeLeaves(location);

        new BukkitRunnable() {
            int iterations = 0;

            @Override
            public void run() {
                for (BlockDisplay blockDisplay : trees.get(location).getTreeDisplay()) {
                    int height = 0;

                    Location location = blockDisplay.getLocation().clone();

                    while (location.getBlock().getType().equals(Material.AIR)) {
                        location.add(0, -1, 0);
                        height++;
                    }

                    Transformation transformation = blockDisplay.getTransformation();

                    switch (direction) {
                        case 0 -> {
                            transformation.getLeftRotation().rotateZ((float) Math.toRadians((double) 90 / 15));

                            transformation.getTranslation().x -= (float) (height - 1) / 15;
                            transformation.getTranslation().y -= (float) (height - 1) / 15;
                        }
                        case 1 -> {
                            transformation.getLeftRotation().rotateX((float) -Math.toRadians((double) 90 / 15));

                            transformation.getTranslation().z -= (float) (height - 1) / 15;
                            transformation.getTranslation().y -= (float) (height - 1) / 15;
                        }
                        case 2 -> {
                            transformation.getLeftRotation().rotateZ(-(float) Math.toRadians((double) 90 / 15));

                            transformation.getTranslation().x += (float) (height) / 15;
                            transformation.getTranslation().y -= (float) (height - 2) / 15;
                        }
                        case 3 -> {
                            transformation.getLeftRotation().rotateX((float) Math.toRadians((double) 90 / 15));

                            transformation.getTranslation().z += (float) (height) / 15;
                            transformation.getTranslation().y -= (float) (height - 2) / 15;
                        }
                    }

                    blockDisplay.setTransformation(transformation);
                }
                iterations++;

                if (iterations >= 15) {
                    this.cancel();
                    explodeBlocks(location);
                }
            }
        }.runTaskTimer(QuickTree.getInstance(), 0, 1);
    }

    private void explodeBlocks(Location location) {
        new BukkitRunnable() {

            @Override
            public void run() {

                for (BlockDisplay blockDisplay : trees.get(location).getTreeDisplay()) {
                    blockDisplay.remove();
                    blockDisplay.getWorld().spawnParticle(ParticleUtils.getBlockParticle(), (blockDisplay.getLocation().add(Vector.fromJOML(blockDisplay.getTransformation().getTranslation()))), 20, 0, 1, 0, blockDisplay.getBlock().getMaterial().createBlockData());

                }
                trees.get(location).getTreeDisplay().clear();
            }
        }.runTaskLater(QuickTree.getInstance(), 20);
    }

    private void explodeLeaves(Location location) {
        Set<Location> leaves = trees.get(location).getLeaves();

        if (!leaves.isEmpty()) {
            Sound sound = leaves.iterator().next().getBlock().getBlockSoundGroup().getBreakSound();
            location.getWorld().playSound(location, sound, 1.3f, 0.8f);
        }

        for (Location loc : trees.get(location).getLeaves()) {
            loc.getBlock().breakNaturally();
        }
    }

    public Tree getTree(Location location) {
        return trees.get(location);
    }
}
