package fun.pozzoo.quicktree.utils;

public class BlockPosUtil {

    /**
     * Packs a block position within a chunk section into an int.
     *
     * <p>Layout (from MSB to LSB):
     * <ul>
     *   <li>4 bits: x (0-15)</li>
     *   <li>4 bits: z (0-15)</li>
     *   <li>16 bits: y (signed)</li>
     * </ul>
     *
     * <p>This supports modern Minecraft height ranges (including negative Y).
     */
    public static int pack(int x, int y, int z) {
        return ((x & 0xF) << 20) | ((z & 0xF) << 16) | (y & 0xFFFF);
    }

    /**
     * Converts the legacy 16-bit format (x:4, z:4, y:8 unsigned) into the current int format.
     */
    public static int fromLegacyShort(short legacyPacked) {
        int unsigned = legacyPacked & 0xFFFF;
        int x = (unsigned >> 12) & 0xF;
        int z = (unsigned >> 8) & 0xF;
        int y = unsigned & 0xFF;
        return pack(x, y, z);
    }
}
