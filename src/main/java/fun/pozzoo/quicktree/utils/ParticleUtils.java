package fun.pozzoo.quicktree.utils;

import org.bukkit.Particle;

public class ParticleUtils {

    private static final Particle BLOCK_PARTICLE;

    static {
        Particle particle;
        try {
            particle = Particle.valueOf("BLOCK");
        } catch (IllegalArgumentException e) {
            particle = Particle.valueOf("BLOCK_CRACK");
        }
        BLOCK_PARTICLE = particle;
    }

    public static Particle getBlockParticle() {
        return BLOCK_PARTICLE;
    }
}
