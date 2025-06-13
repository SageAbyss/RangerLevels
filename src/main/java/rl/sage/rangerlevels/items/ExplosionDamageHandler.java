package rl.sage.rangerlevels.items;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ExplosionDamageHandler {

    /**
     * Cancela cualquier daño de tipo explosión a jugadores que
     * estén en fase FINALIZE de una InvocationSession a ≤30 bloques del altar,
     * y fija su vida a medio corazón (1.0f).
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 1) Sólo daño por explosión
        if (!event.getSource().isExplosion()) return;

        // 2) Sólo jugadores
        if (!(event.getEntityLiving() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();

        // 3) Para cada sesión activa en InvocationManager
        for (InvocationSession session : InvocationManager.getSessions()) {
            // 3a) Debe estar en fase FINALIZE
            if (!session.isInFinalizePhase()) continue;

            // 3b) Calcula distancia 3D jugador ↔ altar
            BlockPos altarPos = session.getPos();
            double dx = player.getX() - (altarPos.getX() + 0.5);
            double dy = player.getY() - (altarPos.getY() + 0.5);
            double dz = player.getZ() - (altarPos.getZ() + 0.5);
            double distSq = dx*dx + dy*dy + dz*dz;

            // 3c) Si está a ≤30 bloques (30² = 900)
            if (distSq <= 900.0) {
                // 4) Cancela el daño original
                event.setCanceled(true);
                // 5) Fija la vida al jugador a medio corazón
                player.setHealth(1.0f);
                break;  // no hace falta seguir comprobando otras sesiones
            }
        }
    }
}
