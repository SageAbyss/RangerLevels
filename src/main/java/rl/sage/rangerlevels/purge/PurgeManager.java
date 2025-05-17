package rl.sage.rangerlevels.purge;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.util.TimeUtil;

@Mod.EventBusSubscriber
public class PurgeManager {
    private static final int TICKS_PER_SECOND = 20;

    /** Cada tick del servidor se regula el conteo */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END) return;

        ServerWorld world = ServerLifecycleHooks.getCurrentServer().getLevel(World.OVERWORLD);
        PurgeData data = PurgeData.get(world);
        ExpConfig.PurgeConfig cfg = ExpConfig.get().purge;

        // 1) Si está desactivado, solo marcamos el estado (pausa)
        if (!cfg.Enable) return;

        // 2) Sincronizamos si cambiaron los segundos totales en config
        long newTotal = TimeUtil.parseDuration(cfg.Timer);
        if (data.getConfigTotalSeconds() != newTotal) {
            data.setConfigTotalSeconds(newTotal);
            data.setRemainingSeconds(newTotal);
            data.setReminderSent(false);
            data.setPurgeEnded(false);
        }

        // 3) Si ya terminó, no reincidir
        if (data.hasPurgeEnded()) return;

        // 4) Decrementa cada segundo real
        if (world.getGameTime() % TICKS_PER_SECOND == 0) {
            long rem = data.getRemainingSeconds() - 1;
            data.setRemainingSeconds(rem);

            // 5) Reminder al 5%
            long thresh = (long)(data.getConfigTotalSeconds() * 0.05);
            if (!data.isReminderSent() && rem <= thresh) {
                broadcast(cfg.Reminder);
                data.setReminderSent(true);
            }

            // 6) Cuando llegue a cero: purga finalizada
            if (rem <= 0) {
                broadcast(cfg.Broadcast);
                data.setPurgeEnded(true);
            }
        }
    }

    /** Bloquea cualquier experiencia si la purga ya terminó. */
    @SubscribeEvent
    public static void onPlayerXp(PlayerXpEvent.PickupXp ev) {
        ServerWorld world = (ServerWorld) ev.getEntity().level;
        PurgeData data = PurgeData.get(world);
        if (data.hasPurgeEnded()) {
            ev.setCanceled(true);
        }
    }

    /** Envía un mensaje a todo el servidor (chat público). */
    private static void broadcast(String msg) {
        StringTextComponent text = new StringTextComponent(msg);
        ServerLifecycleHooks.getCurrentServer()
                .getPlayerList()
                .broadcastMessage(text, ChatType.SYSTEM, Util.NIL_UUID);
    }
}
