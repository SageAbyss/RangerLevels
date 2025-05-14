package rl.sage.rangerlevels.limiter;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import rl.sage.rangerlevels.capability.LimiterProvider;

/**
 * Scheduler global que persiste en disco el próximo reset y ejecuta el reinicio.
 */
@Mod.EventBusSubscriber
public class LimiterScheduler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        // Usa el Overworld como referencia para acceder a datos persistentes
        ServerWorld overworld = server.getLevel(ServerWorld.OVERWORLD);
        if (overworld == null) return;

        // Carga o crea el WorldSavedData
        LimiterWorldData data = LimiterWorldData.get(overworld);

        long now = System.currentTimeMillis() / 1000L;
        long next = data.getNextResetTime();

        // Inicializa si es la primera vez
        if (next == 0L) {
            data.setNextResetTime(now + LimiterManager.getWindowSeconds());
            return;
        }

        // Si aún no es tiempo de reiniciar, salir
        if (now < next) return;

        // Reprograma el próximo reset y resetea los datos
        data.setNextResetTime(now + LimiterManager.getWindowSeconds());

        // Reset de todos los jugadores conectados
        server.getPlayerList().getPlayers().forEach(player ->
                LimiterProvider.get(player).ifPresent(limiter ->
                        limiter.resetWindow(now)
                )
        );
    }
}
