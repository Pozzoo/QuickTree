package fun.pozzoo.quicktree.data;

import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tree {
    private final Set<Location> treeModel;
    private final List<BlockDisplay> treeDisplay;

    public Tree(Location location) {
        treeModel = new HashSet<>();
        treeDisplay = new ArrayList<>();

        treeModel.add(location);
    }

    public Set<Location> getTreeModel() {
        return treeModel;
    }

    public List<BlockDisplay> getTreeDisplay() {
        return treeDisplay;
    }
}
