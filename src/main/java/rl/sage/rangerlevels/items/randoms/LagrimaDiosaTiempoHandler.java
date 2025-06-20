package rl.sage.rangerlevels.items.randoms;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.limiter.LimiterHelper;
import rl.sage.rangerlevels.config.ItemsConfig;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class LagrimaDiosaTiempoHandler {

    // No mantener INTERVAL_MS fijo; leer de config en tiempo de ejecución.
    // Mapa de jugador -> último timestamp en ms en que se le dio EXP
    private static final Map<UUID, Long> lastGive = new ConcurrentHashMap<>();

    private static boolean hasLagrima(ServerPlayerEntity player) {
        for (ItemStack stack : player.inventory.items) {
            if (!stack.isEmpty() &&
                    LagrimaDiosaTiempo.ID.equals(RangerItemDefinition.getIdFromStack(stack))) {
                return true;
            }
        }
        ItemStack off = player.inventory.offhand.get(0);
        return !off.isEmpty() &&
                LagrimaDiosaTiempo.ID.equals(RangerItemDefinition.getIdFromStack(off));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END) return;
        if (!(ev.player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.player;
        UUID uuid = player.getUUID();

        ItemsConfig.LagrimaTiempoConfig cfg = ItemsConfig.get().lagrimaTiempo;
        int expAmount = cfg.expAmount;
        int intervalMin = cfg.intervalMinutes;
        if (intervalMin <= 0) {
            // Evitar división por cero; si config invalid, no dar exp.
            return;
        }
        long intervalMs = intervalMin * 60L * 1000L;

        if (hasLagrima(player)) {
            long now = System.currentTimeMillis();
            Long last = lastGive.get(uuid);
            if (last == null) {
                // Inicializar contador sin dar EXP inmediatamente, o si quieres dar al equipar:
                lastGive.put(uuid, now);
            } else if (now - last >= intervalMs) {
                // Otorgar exp según config
                LimiterHelper.giveExpWithLimit(player, expAmount);
                lastGive.put(uuid, now);
            }
        } else {
            if (lastGive.containsKey(uuid)) {
                lastGive.remove(uuid);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent ev) {
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        lastGive.remove(ev.getPlayer().getUUID());
    }
}
