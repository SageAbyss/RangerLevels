// File: rl/sage/rangerlevels/items/boxes/MeteorImpactEffects.java
package rl.sage.rangerlevels.items.boxes;

import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.concurrent.ThreadLocalRandom;

public class MeteorImpactEffects {

    /**
     * Simula el impacto de un meteorito en pos:
     * - Columna de humo y fuego
     * - Chispas volando radialmente
     * - Explosión de polvo en el suelo
     * - Sonidos de impacto y retumbo
     */
    public static void spawnMeteorImpact(ServerWorld world, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        // 1) Efecto de impacto: explosión de partículas de polvo y fuego
        for (int i = 0; i < 100; i++) {
            double offsetX = (rand.nextDouble() - 0.5) * 4.0;
            double offsetZ = (rand.nextDouble() - 0.5) * 4.0;
            double offsetY = rand.nextDouble() * 2.0;
            world.sendParticles(ParticleTypes.FLAME,
                    cx + offsetX, cy + offsetY, cz + offsetZ,
                    1, 0, 0, 0, 0.05);
            world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    cx + offsetX, cy + offsetY, cz + offsetZ,
                    1, 0, 0, 0, 0.1);
            world.sendParticles(ParticleTypes.SOUL,
                    cx + offsetX * 0.5, cy + offsetY * 0.5, cz + offsetZ * 0.5,
                    1, 0, 0, 0, 0.02);
        }

        // 2) Columna ascendente de fuego y humo (simula la estela del meteorito)
        int heightSteps = 20;
        for (int i = 0; i < heightSteps; i++) {
            double fraction = (double)i / heightSteps;
            double y = cy + fraction * 6.0;
            world.sendParticles(ParticleTypes.LAVA,
                    cx, y, cz,
                    2, 0.2, 0.2, 0.2, 0.02);
            world.sendParticles(ParticleTypes.SMOKE,
                    cx, y, cz,
                    4, 0.3, 0.3, 0.3, 0.05);
        }

        // 3) Anillo de chispas radial
        int sparks = 60;
        double radius = 3.0;
        for (int i = 0; i < sparks; i++) {
            double angle = 2 * Math.PI * i / sparks;
            double x = cx + Math.cos(angle) * radius;
            double z = cz + Math.sin(angle) * radius;
            double y = cy + rand.nextDouble() * 1.5;
            world.sendParticles(ParticleTypes.CRIT,
                    x, y, z,
                    1, Math.cos(angle)*0.1, 0.1, Math.sin(angle)*0.1, 0.02);
        }

        // 4) Sonidos de impacto y eco
        world.playSound(null, pos, SoundEvents.GENERIC_EXPLODE,
                SoundCategory.BLOCKS, 4.0f, 0.8f);
        world.playSound(null, pos, SoundEvents.GENERIC_HURT,
                SoundCategory.HOSTILE, 2.0f, 1.2f);
    }
}
