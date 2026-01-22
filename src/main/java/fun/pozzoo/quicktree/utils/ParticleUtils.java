package fun.pozzoo.quicktree.utils;

import org.bukkit.Particle;

public class ParticleUtils {
    public static Particle getBlockParticle() {
        Particle BLOCK_PARTICLE;

        try {
            BLOCK_PARTICLE = Particle.valueOf("BLOCK");
        } catch (IllegalArgumentException e) {
            BLOCK_PARTICLE = Particle.valueOf("BLOCK_CRACK");
        }
        return BLOCK_PARTICLE;
    }

}
