package fun.pozzoo.quicktree;

import fun.pozzoo.quicktree.data.Tree;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import java.util.*;


public class WoodManager {
    private final List<Material> woods;
    private final Map<Location, Tree> trees;
    private final List<Vector> coordsVector;
    private final Random random;

    public WoodManager() {
        woods = new ArrayList<>();
        trees = new HashMap<>();
        coordsVector = new ArrayList<>();

        random = new Random();

        warmupWoods();
        warmupCoords();
    }

    private void warmupWoods() {
        this.woods.add(Material.ACACIA_LOG);
        this.woods.add(Material.BIRCH_LOG);
        this.woods.add(Material.CHERRY_LOG);
        this.woods.add(Material.JUNGLE_LOG);
        this.woods.add(Material.DARK_OAK_LOG);
        this.woods.add(Material.MANGROVE_LOG);
        this.woods.add(Material.OAK_LOG);
        this.woods.add(Material.SPRUCE_LOG);
        this.woods.add(Material.CRIMSON_STEM);
        this.woods.add(Material.WARPED_STEM);
    }

    private void warmupCoords() {
        coordsVector.add(new Vector(1, 0, 0));
        coordsVector.add(new Vector(0, 0, 1));
        coordsVector.add(new Vector(-1, 0, 0));
        coordsVector.add(new Vector(-1, 0, 0));
        coordsVector.add(new Vector(0, 0, -1));
        coordsVector.add(new Vector(0, 0, -1));
        coordsVector.add(new Vector(1, 0, 0));
        coordsVector.add(new Vector(1, 0, 0));
        coordsVector.add(new Vector(-1, 0, 1));
    }

    public boolean isWoodenLogs(Material material) {
        return this.woods.contains(material);
    }

    public Vector getCoordsVector(int i) {
        return coordsVector.get(i);
    }

    public void destroyTree(Location initialLocation) {
        for (Location location : trees.get(initialLocation).getTreeModel()) {
            location.getBlock().breakNaturally(true);
        }

        trees.get(initialLocation).getTreeModel().clear();
    }

    public void createTree(Location location) {
        trees.put(location, new Tree(location));

        checkAround(location, location);
    }

    public void checkAround(Location initialLocation, Location target) {

        Location location1 = target.clone();

        while (woods.contains(location1.getBlock().getType())) {

            for (int i = 0; i < coordsVector.size(); i++) {
                if (isWoodenLogs(location1.getBlock().getType()) && (!trees.get(initialLocation).getTreeModel().contains(location1))) {
                    trees.get(initialLocation).getTreeModel().add(location1.clone());

                    if (trees.get(initialLocation).getTreeModel().size() <= 30) {
                        checkAround(initialLocation, target);
                    }
                }
                location1.add(getCoordsVector(i));
            }
            location1.add(0, 1, 0);
        }
    }

    public void createTreeDisplay(Location initalLocation) {
        Tree tree = trees.get(initalLocation);

        for (Location location : tree.getTreeModel()) {
            BlockDisplay blockDisplay = location.getWorld().spawn(location, BlockDisplay.class);
            blockDisplay.setBlock(location.getBlock().getBlockData());

            trees.get(initalLocation).getTreeDisplay().add(blockDisplay);
        }

        animateTree(random.nextInt(4), initalLocation);
    }

    private void animateTree(int direction, Location location) {
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
                    blockDisplay.getWorld().spawnParticle(Particle.BLOCK, blockDisplay.getLocation(), 20, 1, 5, 0, Material.SPRUCE_LOG.createBlockData());

                }
                trees.get(location).getTreeDisplay().clear();
            }
        }.runTaskLater(QuickTree.getInstance(), 20);
    }

    public Tree getTree(Location location) {
        return trees.get(location);
    }
}
