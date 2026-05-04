package fun.pozzoo.quicktree.data;

import java.util.HashSet;
import java.util.Set;

public class TrackedChunk {

    private final Set<Integer> blocks = new HashSet<>();

    public void add(int pos) {
        blocks.add(pos);
    }

    public void remove(int pos) {
        blocks.remove(pos);
    }

    public boolean contains(int pos) {
        return blocks.contains(pos);
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    public Set<Integer> getAll() {
        return blocks;
    }
}
