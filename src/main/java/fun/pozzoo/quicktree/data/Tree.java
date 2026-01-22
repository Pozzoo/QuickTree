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
    private final Set<Location> leaves;

    public Tree() {
        treeModel = new HashSet<>();
        treeDisplay = new ArrayList<>();
        leaves = new HashSet<>();
    }

    public Set<Location> getTreeModel() {
        return treeModel;
    }

    public void setTreeModel(Set<Location> treeModel) {
        this.treeModel.clear();
        this.treeModel.addAll(treeModel);
    }

    public List<BlockDisplay> getTreeDisplay() {
        return treeDisplay;
    }

    public Set<Location> getLeaves() {
        return leaves;
    }

    public void setLeaves(Set<Location> leaves) {
        this.leaves.clear();
        this.leaves.addAll(leaves);
    }
}
