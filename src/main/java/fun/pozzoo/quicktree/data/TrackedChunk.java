package fun.pozzoo.quicktree.data;

import java.util.HashSet;
import java.util.Set;

public class TrackedChunk {

    private final Set<Short> blocks = new HashSet<>();

    public void add(short pos) {
        blocks.add(pos);
    }

    public void remove(short pos) {
        blocks.remove(pos);
    }

    public boolean contains(short pos) {
        return blocks.contains(pos);
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    public Set<Short> getAll() {
        return blocks;
    }
}
