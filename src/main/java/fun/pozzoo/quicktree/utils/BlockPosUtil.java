package fun.pozzoo.quicktree.utils;

public class BlockPosUtil {
    public static short pack(int x, int y, int z) {
        return (short) ((x & 0xF) << 12 | (z & 0xF) << 8 | (y & 0xFF));
    }
}
