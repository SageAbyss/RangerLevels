package rl.sage.rangerlevels.events;


import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import rl.sage.rangerlevels.capability.IPlayerRewards;
import rl.sage.rangerlevels.capability.PlayerRewardsProvider;
import rl.sage.rangerlevels.capability.RewardStatus;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Clase que envía un recordatorio al jugador si tiene recompensas pendientes:
 *  1) Cada vez que entra al servidor.
 *  2) Cada X minutos (configurable en Config.yml bajo ExpConfig.rewardReminder.intervalMinutes).
 *
 * El recordatorio se muestra como TITLE + SUBTITLE con un degradado pastel y sonido.
 */
@Mod.EventBusSubscriber(modid = "rangerlevels", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RewardReminderHandler {

    // Contador de ticks (20 ticks = 1 segundo)
    private static int tickCounter = 0;

    /**
     * Evento que se dispara cada tick del servidor.
     * Se usa para contar y, cada X minutos, enviar recordatorio a todos los jugadores online.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ExpConfig cfg = ExpConfig.get();
        int intervalMinutes = 0;
        if (cfg != null && cfg.rewardReminder != null) {
            intervalMinutes = cfg.rewardReminder.intervalMinutes;
        }
        if (intervalMinutes <= 0) return;

        // Convertir minutos a ticks
        int intervalTicks = intervalMinutes * 60 * 20;
        tickCounter++;

        if (tickCounter >= intervalTicks) {
            tickCounter = 0;
            sendReminderToAllOnline();
        }
    }

    /**
     * Evento que se dispara cuando el jugador inicia sesión.
     * Envía recordatorio inmediato si tiene recompensas pendientes.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        sendReminderIfPending(player);
    }

    /**
     * Recorre todos los jugadores online y les manda el TITLE+SUBTITLE si tienen recompensas pendientes.
     */
    private static void sendReminderToAllOnline() {
        MinecraftServer server = net.minecraftforge.fml.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        PlayerList playerList = server.getPlayerList();
        for (ServerPlayerEntity player : playerList.getPlayers()) {
            sendReminderIfPending(player);
        }
    }

    /**
     * Si el jugador tiene recompensas PENDING, envía TITLE + SUBTITLE con degradado pastel y sonido.
     */
    private static void sendReminderIfPending(ServerPlayerEntity player) {
        @Nullable
        IPlayerRewards cap = player.getCapability(PlayerRewardsProvider.REWARDS_CAP, null).orElse(null);
        if (cap == null) return;

        int pendingCount = 0;
        for (Map.Entry<String, RewardStatus> entry : cap.getStatusMap().entrySet()) {
            if (entry.getValue() == RewardStatus.PENDING) {
                pendingCount++;
            }
        }
        if (pendingCount == 0) return;

        // Construir TITLE y SUBTITLE con degradado pastel usando GradientText
        String titleText = "Recompensas Pendientes";
        String subtitleText = "Tienes " + pendingCount + " recompensa(s) por reclamar";

        // Degradado entre dos colores pastel
        IFormattableTextComponent titleComponent = GradientText.of(
                titleText,
                "#FFB6C1", // pastel pink
                "#ADD8E6"  // pastel blue
        );
        IFormattableTextComponent subtitleComponent = GradientText.of(
                subtitleText,
                "#E0BBE4", // pastel lavender
                "#FFDAC1"  // pastel peach
        );

        // Tiempos de fadeIn, stay, fadeOut (en ticks)
        int fadeIn = 10;   // 0.5 segundos
        int stay   = 60;   // 3 segundos
        int fadeOut = 10;  // 0.5 segundos

        // Enviar paquete de tiempos
        player.connection.send(new STitlePacket(fadeIn, stay, fadeOut));
        // Enviar TITLE
        player.connection.send(new STitlePacket(STitlePacket.Type.TITLE, titleComponent));
        // Enviar SUBTITLE
        player.connection.send(new STitlePacket(STitlePacket.Type.SUBTITLE, subtitleComponent));

        // Reproducir sonido de recordatorio
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.NOTE_BLOCK_BELL,
                SoundCategory.PLAYERS,
                1.0f,
                0.8f
        );
    }
}
