package fun.pozzoo.quicktree.managers;

import fun.pozzoo.quicktree.QuickTree;
import fun.pozzoo.quicktree.data.Tree;
import fun.pozzoo.quicktree.utils.ParticleUtils;
import fun.pozzoo.quicktree.utils.WoodUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import java.util.*;


public class WoodManager {
    private final Map<Location, Tree> trees;
    private final List<Vector> coordsVector;
    private final Random random;

    public WoodManager() {
        trees = new HashMap<>();
        coordsVector = new ArrayList<>();

        random = new Random();

        warmupCoords();
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

        while (WoodUtils.isWoodenLogs(location1.getBlock().getType())) {

            for (int i = 0; i < coordsVector.size(); i++) {
                if (WoodUtils.isWoodenLogs(location1.getBlock().getType()) && (!trees.get(initialLocation).getTreeModel().contains(location1))) {
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
                    blockDisplay.getWorld().spawnParticle(ParticleUtils.getBlockParticle(), (blockDisplay.getLocation().add(Vector.fromJOML(blockDisplay.getTransformation().getTranslation()))), 20, 0, 1, 0, blockDisplay.getBlock().getMaterial().createBlockData());

                }
                trees.get(location).getTreeDisplay().clear();
            }
        }.runTaskLater(QuickTree.getInstance(), 20);
    }

    public Tree getTree(Location location) {
        return trees.get(location);
    }
}
