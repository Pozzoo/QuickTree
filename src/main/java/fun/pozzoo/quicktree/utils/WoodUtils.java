package fun.pozzoo.quicktree.utils;

import org.bukkit.Material;

public class WoodUtils {
    public static boolean isWoodenLogs(Material material) {
        return material.name().contains("_LOG")
                ||  material.name().contains("_WOOD")
                ||  material.name().contains("_STEM");
    }
}
