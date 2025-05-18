package rl.sage.rangerlevels.purge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.TextColorUtil;
import rl.sage.rangerlevels.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber
public class PurgeManager {
    private static final int TICKS_PER_SECOND = 20;
    private static final Pattern COLOR_CODE = Pattern.compile("&#([0-9A-Fa-f]{6})");

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END) return;

        ServerWorld world = ServerLifecycleHooks.getCurrentServer().getLevel(World.OVERWORLD);
        PurgeData data = PurgeData.get(world);
        ExpConfig.PurgeConfig cfg = ExpConfig.get().purge;

        if (!cfg.Enable) return;

        long newTotal = TimeUtil.parseDuration(cfg.Timer);
        if (data.getConfigTotalSeconds() != newTotal) {
            data.setConfigTotalSeconds(newTotal);
            data.setRemainingSeconds(newTotal);
            data.setReminderSent(false);
            data.setPurgeEnded(false);
        }

        if (data.hasPurgeEnded()) return;

        if (world.getGameTime() % TICKS_PER_SECOND == 0) {
            long rem = data.getRemainingSeconds() - 1;
            data.setRemainingSeconds(rem);

            long thresh = (long)(data.getConfigTotalSeconds() * 0.05);
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

    public static boolean isPurgeEnded() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;
        ServerWorld world = server.getLevel(World.OVERWORLD);
        return world != null && PurgeData.get(world).hasPurgeEnded();
    }

    @SubscribeEvent
    public static void onPlayerXp(PlayerXpEvent.PickupXp ev) {
        if (isPurgeEnded()) {
            ev.setCanceled(true);
        }
    }

    private static void broadcast(List<String> lines) {
        for (String rawLine : lines) {
            // 1) Detecta todos los marcadores &#RRGGBB
            Matcher matcher = COLOR_CODE.matcher(rawLine);
            List<String> hexCodes = new ArrayList<>();
            while (matcher.find()) {
                hexCodes.add("#" + matcher.group(1));
            }

            // 2) Limpia los marcadores del texto
            String cleanText = rawLine.replaceAll("&#[0-9A-Fa-f]{6}", "");

            // 3) Construye el componente
            IFormattableTextComponent comp;
            if (hexCodes.isEmpty()) {
                comp = new StringTextComponent(cleanText);
            } else if (hexCodes.size() == 1) {
                Style style = TextColorUtil.styleFromHex(hexCodes.get(0));
                comp = new StringTextComponent(cleanText).setStyle(style);
            } else {
                comp = GradientText.of(cleanText, hexCodes.toArray(new String[0]));
            }

            // 4) Envía el mensaje
            ServerLifecycleHooks.getCurrentServer()
                    .getPlayerList()
                    .broadcastMessage(comp, ChatType.SYSTEM, Util.NIL_UUID);
        }
    }
}
