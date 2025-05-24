package rl.sage.rangerlevels.purge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.config.ExpConfig.PurgeConfig;
import rl.sage.rangerlevels.util.TextFormatterUtil;
import rl.sage.rangerlevels.util.TimeUtil;
import rl.sage.rangerlevels.purge.PurgeData;

import java.util.Objects;

@Mod.EventBusSubscriber
public class PurgeManager {
    private static final int TICKS_PER_SECOND = 20;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        PurgeData data = PurgeData.get(Objects.requireNonNull(server.getLevel(World.OVERWORLD)));
        PurgeConfig cfg = ExpConfig.get().purge;

        if (!cfg.Enable) return;

        long newTotal = TimeUtil.parseDuration(cfg.Timer);
        if (data.getConfigTotalSeconds() != newTotal) {
            data.setConfigTotalSeconds(newTotal);
            data.setRemainingSeconds(newTotal);
            data.setReminderSent(false);
            data.setPurgeEnded(false);
        }

        if (data.hasPurgeEnded()) return;

        if (server.getTickCount() % TICKS_PER_SECOND == 0) {
            long rem = data.getRemainingSeconds() - 1;
            data.setRemainingSeconds(rem);

            long thresh = (long) (data.getConfigTotalSeconds() * 0.05);
            if (!data.isReminderSent() && rem <= thresh) {
                broadcast(cfg.Reminder);
                data.setReminderSent(true);
            }

            if (rem <= 0) {
                broadcast(cfg.Broadcast);
                data.setPurgeEnded(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerXp(PlayerXpEvent.PickupXp ev) {
        if (isPurgeEnded()) {
            ev.setCanceled(true);
        }
    }

    public static boolean isPurgeEnded() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;
        return PurgeData.get(Objects.requireNonNull(server.getLevel(World.OVERWORLD))).hasPurgeEnded();
    }

    private static void broadcast(java.util.List<String> lines) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (String rawLine : lines) {
            // Parsea automáticamente legacy, hex, xhex y gradientes
            IFormattableTextComponent comp = TextFormatterUtil.parse(rawLine);

            server.getPlayerList()
                    .broadcastMessage(comp, ChatType.SYSTEM, Util.NIL_UUID);
        }
    }
}
