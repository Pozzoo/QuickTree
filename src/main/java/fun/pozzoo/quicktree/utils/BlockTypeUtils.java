package fun.pozzoo.quicktree.utils;

import org.bukkit.Material;
import org.bukkit.Tag;

public class BlockTypeUtils {

    private BlockTypeUtils() {
    }

    public static boolean isWoodenLogs(Material material) {
        // Prefer tags (more accurate than name matching across versions)
        if (Tag.LOGS.isTagged(material) || Tag.LOGS_THAT_BURN.isTagged(material)) return true;

        // Fallback for any edge-case materials not covered by tags.
        String name = material.name();
        return name.contains("_LOG") || name.contains("_WOOD") || name.contains("_STEM");
    }

    public static boolean isLeaves(Material material) {
        return Tag.LEAVES.isTagged(material) || material.name().contains("_LEAVES");
    }

    public static boolean isKindaLeaves(Material material) {
        // Huge mushrooms + nether wart blocks are treated as leaves-like
        String name = material.name();
        return name.contains("_MUSHROOM_BLOCK") || name.contains("_WART_BLOCK");
    }
}
