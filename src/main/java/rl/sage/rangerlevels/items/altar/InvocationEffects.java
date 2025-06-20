package rl.sage.rangerlevels.items.altar;

import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.concurrent.ThreadLocalRandom;

public class InvocationEffects {

    /**
     * Partículas estilo “victoria épica” alrededor de pos, tal como en InvocationSession
     */
    public static void spawnEpicVictory(ServerWorld world, BlockPos pos) {
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 1.0;
        double centerZ = pos.getZ() + 0.5;
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        // Anillos concéntricos de END_ROD y CAMPFIRE_COSY_SMOKE
        int ringCount = 3;
        for (int ring = 1; ring <= ringCount; ring++) {
            double radius = ring * 2.0;
            int points = 40;
            double y = centerY + ring * 0.3;
            for (int i = 0; i < points; i++) {
                double angle = 2 * Math.PI * i / points;
                double x = centerX + Math.cos(angle) * radius;
                double z = centerZ + Math.sin(angle) * radius;
                world.sendParticles(ParticleTypes.END_ROD,
                        x, y + (rand.nextDouble() - 0.5) * 0.5, z,
                        1, 0, 0, 0, 0.0);
                world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        x, y + rand.nextDouble() * 0.5, z,
                        1, 0, 0, 0, 0.0);
            }
        }

        // Espiral ascendente de DRAGON_BREATH
        int spiralSteps = 60;
        double maxSpiralHeight = 4.0;
        for (int i = 0; i < spiralSteps; i++) {
            double t = i * (2 * Math.PI / spiralSteps) * 3;
            double fraction = (double) i / spiralSteps;
            double radius = 1.0 + fraction * 3.0;
            double x = centerX + Math.cos(t) * radius;
            double z = centerZ + Math.sin(t) * radius;
            double y = centerY + fraction * maxSpiralHeight;
            world.sendParticles(ParticleTypes.DRAGON_BREATH,
                    x, y, z,
                    1, 0, 0, 0, 0.0);
        }

        // Explosión central de HAPPY_VILLAGER + FLAME
        int centerBurst = 50;
        for (int i = 0; i < centerBurst; i++) {
            double offsetX = (rand.nextDouble() - 0.5) * 2.0;
            double offsetZ = (rand.nextDouble() - 0.5) * 2.0;
            double offsetY = rand.nextDouble() * 1.5;
            double x = centerX + offsetX;
            double y = centerY + offsetY;
            double z = centerZ + offsetZ;
            world.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    x, y, z,
                    1, 0, 0, 0, 0.0);
            world.sendParticles(ParticleTypes.FLAME,
                    x, y, z,
                    1, 0, 0, 0, 0.0);
        }
    }
}
