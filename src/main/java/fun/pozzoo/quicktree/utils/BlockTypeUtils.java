package fun.pozzoo.quicktree.utils;

import org.bukkit.Material;

public class BlockTypeUtils {
    public static boolean isWoodenLogs(Material material) {
        return material.name().contains("_LOG")
                ||  material.name().contains("_WOOD")
                ||  material.name().contains("_STEM");
    }

    public static boolean isLeaves(Material material) {
        return material.name().contains("_LEAVES");
    }

    public static boolean isKindaLeaves(Material material) {
        return material.name().contains("_MUSHROOM_BLOCK")
                || material.name().contains("_WART_BLOCK");
    }
}
