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
    private static final int BASE_FALL_TICKS = 10;
    private static final int TICKS_PER_TREE_BLOCK = 1;
    private static final int MAX_FALL_TICKS = 60;
    private static final int TERRAIN_SCAN_UP = 4;
    private static final int TERRAIN_SCAN_DOWN = 16;

    private static final int[][] DIRECTIONS = {
            {-1, 0, -1}, {-1, 0, 0}, {-1, 0, 1},
            {0, 0, -1},               {0, 0, 1},
            {1, 0, -1},  {1, 0, 0},   {1, 0, 1},

            {-1, 1, -1}, {-1, 1, 0}, {-1, 1, 1},
            {0, 1, -1},  {0, 1, 0},  {0, 1, 1},
            {1, 1, -1},  {1, 1, 0},  {1, 1, 1}
    };

    private final Map<Location, Tree> trees;
    private final Random random;

    private record BlockDepth(Location location, int depth) {
    }

    public WoodManager() {
        trees = new HashMap<>();
        random = new Random();
    }

    public void destroyTree(Location initialLocation, Vector playerDirection) {
        Tree tree = trees.get(initialLocation);

        if (tree == null) {
            return;
        }

        for (Location location : tree.getTreeModel()) {
            location.getBlock().breakNaturally(true);
        }

        tree.getTreeModel().clear();
        animateTree(random.nextInt(4), initialLocation, playerDirection);
    }

    public void createTree(Location location) {
        Tree tree = new Tree();
        LinkedList<Location> logs = searchLogs(location);

        tree.setTreeModel(logs);
        tree.setLeaves(searchLeavesFromLogs(logs));

        trees.put(location, tree);
    }

    public LinkedList<Location> searchLogs(Location start) {
        LinkedList<Location> logs = new LinkedList<>();
        Set<Location> visited = new HashSet<>();
        Queue<Location> queue = new ArrayDeque<>();

        queue.add(start);
        logs.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Location current = queue.poll();

            for (int[] d : DIRECTIONS) {
                Location next = current.getBlock().getRelative(d[0], d[1], d[2]).getLocation();

                if (!BlockTypeUtils.isWoodenLogs(next.getBlock().getType())) continue;
                if (!visited.add(next)) continue;

                // Structure protection
                if (QuickTree.getInstance().getStorageManager().isPlayerPlaced(next.getBlock())) {
                    return new LinkedList<>(); // abort entire felling
                }

                logs.add(next);
                queue.add(next);
            }

            // Prevent from animating "Trees" that are 1 block tall
            if (logs.getFirst().getY() == logs.getLast().getY()) {
                for (Location log : logs) {
                    log.getBlock().breakNaturally();
                }

                break;
            }

            if (logs.size() > 512) break; // safety cap
        }

        return logs;
    }

    public LinkedList<Location> searchLeavesFromLogs(LinkedList<Location> logs) {
        LinkedList<Location> leaves = new LinkedList<>();
        Set<Location> visited = new HashSet<>();
        Queue<BlockDepth> queue = new ArrayDeque<>();

        // Seed BFS with logs
        for (Location log : logs) {
            queue.add(new BlockDepth(log, 0));
            visited.add(log);
        }

        while (!queue.isEmpty()) {
            BlockDepth current = queue.poll();

            if (current.depth >= 4) continue;

            for (int[] d : DIRECTIONS) {
                Location next = current.location.getBlock().getRelative(d[0], d[1], d[2]).getLocation();
                if (!visited.add(next)) continue;

                if (BlockTypeUtils.isLeaves(next.getBlock().getType())) {
                    Leaves data = (Leaves) next.getBlock().getBlockData();
                    if (data.isPersistent()) continue;

                    leaves.add(next);
                    queue.add(new BlockDepth(next, current.depth + 1));
                } else if (BlockTypeUtils.isKindaLeaves(next.getBlock().getType())) {
                    leaves.add(next);
                    queue.add(new BlockDepth(next, current.depth + 1));
                }
            }
        }

        return leaves;
    }


    public void createTreeDisplay(Location initialLocation) {
        Tree tree = trees.get(initialLocation);

        if (tree == null) {
            return;
        }

        for (Location location : tree.getTreeModel()) {
            BlockDisplay blockDisplay = location.getWorld().spawn(location, BlockDisplay.class);
            blockDisplay.setBlock(location.getBlock().getBlockData());

            tree.getTreeDisplay().add(blockDisplay);
        }
    }

    private void animateTree(int direction, Location location, Vector playerDirection) {
        explodeLeaves(location);

        int playerDirectionInt = ((Math.round(playerDirection.getX())) == 0) ? (Math.round(playerDirection.getZ()) == -1 ? 0 : 1) : (Math.round(playerDirection.getX()) == 1 ? 2 : 3);
        int finalDirection = (direction % 2 == 0) ? (direction + 1 == playerDirectionInt ? (direction - 1) & 3 : direction) : (direction - 1 == playerDirectionInt ? (direction + 1) & 3 : direction);

        Tree tree = trees.get(location);

        if (tree == null || tree.getTreeDisplay().isEmpty()) {
            return;
        }

        int maxIterations = getFallAnimationTicks(tree);

        new BukkitRunnable() {
            int iterations = 0;

            @Override
            public void run() {
                Tree tree = trees.get(location);

                if (tree == null || tree.getTreeDisplay().isEmpty()) {
                    this.cancel();
                    return;
                }

                double progress = Math.min(1.0, (double) (iterations + 1) / maxIterations);
                double angle = Math.toRadians(90.0 * progress);

                applyTerrainFollowingTreePose(tree, location, finalDirection, angle, progress);

                iterations++;

                if (iterations >= maxIterations) {
                    this.cancel();
                    explodeBlocks(location);
                }
            }
        }.runTaskTimer(QuickTree.getInstance(), 0, 1);
    }

    private void applyTerrainFollowingTreePose(Tree tree, Location pivot, int direction, double angle, double progress) {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        Vector rotatedCenterOffset = getRotatedCenterOffset(direction, angle);
        double lowestRotatedCornerY = getLowestRotatedBlockCornerY(direction, angle);
        Map<Long, Double> terrainCache = new HashMap<>();

        for (BlockDisplay blockDisplay : tree.getTreeDisplay()) {
            Location baseLocation = blockDisplay.getLocation();
            Transformation transformation = blockDisplay.getTransformation();

            double baseCenterX = baseLocation.getX() + 0.5;
            double baseCenterY = baseLocation.getY() + 0.5;
            double baseCenterZ = baseLocation.getZ() + 0.5;

            double heightFromPivot = baseCenterY - pivot.getY();
            double effectiveHingeDistance = heightFromPivot + 0.5;

            double targetCenterX = baseCenterX;
            double targetCenterY = baseCenterY - effectiveHingeDistance * (1.0 - cos);
            double targetCenterZ = baseCenterZ;

            double sideShapeOffset = getSideShapeOffset(baseLocation, pivot, direction);

            switch (direction) {
                case 0 -> { // North
                    targetCenterX = pivot.getX() + 0.5 + sideShapeOffset;
                    targetCenterZ = baseCenterZ - effectiveHingeDistance * sin;
                    transformation.getLeftRotation().identity().rotateX((float) -angle);
                }
                case 1 -> { // South
                    targetCenterX = pivot.getX() + 0.5 + sideShapeOffset;
                    targetCenterZ = baseCenterZ + effectiveHingeDistance * sin;
                    transformation.getLeftRotation().identity().rotateX((float) angle);
                }
                case 2 -> { // East
                    targetCenterX = baseCenterX + effectiveHingeDistance * sin;
                    targetCenterZ = pivot.getZ() + 0.5 + sideShapeOffset;
                    transformation.getLeftRotation().identity().rotateZ((float) -angle);
                }
                case 3 -> { // West
                    targetCenterX = baseCenterX - effectiveHingeDistance * sin;
                    targetCenterZ = pivot.getZ() + 0.5 + sideShapeOffset;
                    transformation.getLeftRotation().identity().rotateZ((float) angle);
                }
            }

            double targetX = targetCenterX - rotatedCenterOffset.getX();
            double rotatedY = targetCenterY - rotatedCenterOffset.getY();
            double targetZ = targetCenterZ - rotatedCenterOffset.getZ();

            double terrainY = getCachedTerrainSurfaceY(
                    terrainCache,
                    baseLocation,
                    targetCenterX,
                    rotatedY,
                    targetCenterZ
            );

            double terrainAdjustedY = terrainY - lowestRotatedCornerY;
            double targetY = lerp(rotatedY, terrainAdjustedY, progress);

            if (targetY < terrainAdjustedY) {
                targetY = terrainAdjustedY;
            }

            transformation.getTranslation().x = (float) (targetX - baseLocation.getX());
            transformation.getTranslation().y = (float) (targetY - baseLocation.getY());
            transformation.getTranslation().z = (float) (targetZ - baseLocation.getZ());

            blockDisplay.setTransformation(transformation);
        }
    }

    private double getLowestRotatedBlockCornerY(int direction, double angle) {
        double rotationAngle = switch (direction) {
            case 0, 2 -> -angle;
            case 1, 3 -> angle;
            default -> 0.0;
        };

        double sin = Math.sin(rotationAngle);
        double cos = Math.cos(rotationAngle);

        return switch (direction) {
            case 0, 1 -> Math.min(0.0, cos) + Math.min(0.0, -sin); // X rotation: y' = y*cos - z*sin
            case 2, 3 -> Math.min(0.0, sin) + Math.min(0.0, cos);  // Z rotation: y' = x*sin + y*cos
            default -> 0.0;
        };
    }

    private double getCachedTerrainSurfaceY(Map<Long, Double> cache, Location fallbackLocation, double x, double currentY, double z) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        long key = (((long) blockX) << 32) ^ (blockZ & 0xffffffffL);

        Double cachedY = cache.get(key);

        if (cachedY != null) {
            return cachedY;
        }

        double terrainY = getTerrainSurfaceY(fallbackLocation, blockX, blockZ, currentY);
        cache.put(key, terrainY);

        return terrainY;
    }

    private double getTerrainSurfaceY(
            Location fallbackLocation,
            int blockX,
            int blockZ,
            double currentY
    ) {
        int maxY = Math.min(
                fallbackLocation.getWorld().getMaxHeight() - 1,
                (int) Math.ceil(currentY) + TERRAIN_SCAN_UP
        );

        int minY = Math.max(
                fallbackLocation.getWorld().getMinHeight(),
                (int) Math.floor(currentY) - TERRAIN_SCAN_DOWN
        );

        for (int y = maxY; y >= minY; y--) {
            Material material = fallbackLocation.getWorld().getBlockAt(blockX, y, blockZ).getType();

            if (isTerrainBlock(material)) {
                return y + 1.0;
            }
        }

        return currentY;
    }

    private double lerp(double start, double end, double amount) {
        return start + (end - start) * amount;
    }

    private boolean isTerrainBlock(Material material) {
        return material.isSolid()
                && !BlockTypeUtils.isWoodenLogs(material)
                && !BlockTypeUtils.isLeaves(material)
                && !BlockTypeUtils.isKindaLeaves(material);
    }

    private void explodeBlocks(Location location) {
        new BukkitRunnable() {

            @Override
            public void run() {
                Tree tree = trees.get(location);

                if (tree == null) {
                    return;
                }

                for (BlockDisplay blockDisplay : tree.getTreeDisplay()) {
                    Location particleLocation = blockDisplay.getLocation().clone()
                            .add(Vector.fromJOML(blockDisplay.getTransformation().getTranslation()));

                    blockDisplay.remove();
                    blockDisplay.getWorld().spawnParticle(
                            ParticleUtils.getBlockParticle(),
                            particleLocation,
                            20,
                            0,
                            1,
                            0,
                            blockDisplay.getBlock().getMaterial().createBlockData()
                    );
                }

                tree.getTreeDisplay().clear();
            }
        }.runTaskLater(QuickTree.getInstance(), 20);
    }

    private void explodeLeaves(Location location) {
        Tree tree = trees.get(location);

        if (tree == null) {
            return;
        }

        LinkedList<Location> leaves = tree.getLeaves();

        if (!leaves.isEmpty()) {
            Sound sound = leaves.iterator().next().getBlock().getBlockSoundGroup().getBreakSound();
            location.getWorld().playSound(location, sound, 1.3f, 0.8f);
        }

        for (Location loc : leaves) {
            loc.getBlock().breakNaturally();
        }
    }

    public Tree getTree(Location location) {
        return trees.get(location);
    }

    private double getSideShapeOffset(Location blockLocation, Location pivot, int direction) {
        return switch (direction) {
            case 0, 1 -> blockLocation.getX() - pivot.getX(); // Falling north/south: preserve east-west width
            case 2, 3 -> blockLocation.getZ() - pivot.getZ(); // Falling east/west: preserve north-south width
            default -> 0.0;
        };
    }

    private int getTreeHeight(Tree tree) {
        if (tree == null || tree.getTreeDisplay().isEmpty()) {
            return 1;
        }

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (BlockDisplay blockDisplay : tree.getTreeDisplay()) {
            int y = blockDisplay.getLocation().getBlockY();

            if (y < minY) {
                minY = y;
            }

            if (y > maxY) {
                maxY = y;
            }
        }

        return Math.max(1, maxY - minY + 1);
    }

    private int getFallAnimationTicks(Tree tree) {
        int treeHeight = getTreeHeight(tree);
        return Math.min(MAX_FALL_TICKS, BASE_FALL_TICKS + treeHeight * TICKS_PER_TREE_BLOCK);
    }

    private Vector getRotatedCenterOffset(int direction, double angle) {
        return switch (direction) {
            case 0 -> rotateAroundX(-angle); // North
            case 1 -> rotateAroundX(angle);  // South
            case 2 -> rotateAroundZ(-angle); // East
            case 3 -> rotateAroundZ(angle);  // West
            default -> new Vector(0.5, 0.5, 0.5);
        };
    }

    private Vector rotateAroundX(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        return new Vector(
                0.5,
                0.5 * cos - 0.5 * sin,
                0.5 * sin + 0.5 * cos
        );
    }

    private Vector rotateAroundZ(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        return new Vector(
                0.5 * cos - 0.5 * sin,
                0.5 * sin + 0.5 * cos,
                0.5
        );
    }
}