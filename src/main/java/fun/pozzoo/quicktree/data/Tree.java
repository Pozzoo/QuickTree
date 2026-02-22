package fun.pozzoo.quicktree.data;

import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;

import java.util.*;

public class Tree {
    private final LinkedList<Location> treeModel;
    private final List<BlockDisplay> treeDisplay;
    private final LinkedList<Location> leaves;

    public Tree() {
        treeModel = new LinkedList<>();
        treeDisplay = new ArrayList<>();
        leaves = new LinkedList<>();
    }

    public LinkedList<Location> getTreeModel() {
        return treeModel;
    }

    public void setTreeModel(LinkedList<Location> treeModel) {
        this.treeModel.clear();
        this.treeModel.addAll(treeModel);
    }

    public List<BlockDisplay> getTreeDisplay() {
        return treeDisplay;
    }

    public LinkedList<Location> getLeaves() {
        return leaves;
    }

    public void setLeaves(LinkedList<Location> leaves) {
        this.leaves.clear();
        this.leaves.addAll(leaves);
    }
}
