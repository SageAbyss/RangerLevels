package rl.sage.rangerlevels.multiplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.Style;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.HashSet;
import java.util.Set;

import static rl.sage.rangerlevels.RangerLevels.PREFIX;

/**
 * Cada segundo revisa:
 * 1) Si queda exactamente 59s en el multiplicador global o privado → manda aviso.
 * 2) Si un multiplicador expiró → lo restablece a 1.0.
 */
@Mod.EventBusSubscriber(modid = "rangerlevels", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MultiplierNotifier {
    private static final MultiplierManager MGR = MultiplierManager.instance();

    /** Para no repetir el aviso global de 59s */
    private static boolean warnedGlobal59 = false;
    private static boolean warnedGlobal1 = false;

    /** Para no repetir avisos privados de 59s (clave = nombre de jugador) */
    private static final Set<String> warnedPrivate59 = new HashSet<>();
    private static final Set<String> warnedPrivate1 = new HashSet<>();


    /** Contador de ticks para agrupar cada ~20 ticks = 1 segundo */
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        // Solo al final de la fase END
        if (event.phase != TickEvent.Phase.END) return;

        // Cada ~20 ticks (~1s)
        if (++tickCounter < 20) return;
        tickCounter = 0;

        long globalRem = MGR.getGlobalRemainingSeconds();
        double globalVal = MGR.getGlobal();

        // ——— AVISO GLOBAL 59s ———
        if (globalVal > 1.0 && globalRem == 59 && !warnedGlobal59) {
            IFormattableTextComponent msg = PREFIX.copy()
                    .append(new StringTextComponent(" El multiplicador GLOBAL termina en 60 segundos.")
                            .withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)));

            ServerLifecycleHooks.getCurrentServer()
                    .getPlayerList().getPlayers()
                    .forEach(p -> p.sendMessage(msg, p.getUUID()));

            warnedGlobal59 = true;
        }
        // ——— AVISO GLOBAL 59s ———
        if (globalVal > 1.0 && globalRem == 1 && !warnedGlobal1) {
            IFormattableTextComponent msg = PREFIX.copy()
                    .append(new StringTextComponent(" El multiplicador GLOBAL finalizó")
                            .withStyle(Style.EMPTY.withColor(TextFormatting.RED)));

            ServerLifecycleHooks.getCurrentServer()
                    .getPlayerList().getPlayers()
                    .forEach(p -> p.sendMessage(msg, p.getUUID()));

            warnedGlobal1 = true;
        }

        // Reset del flag si baja de 59s o vuelve indefinido/expirado
        if (globalRem != 59 && warnedGlobal59) {
            warnedGlobal59 = false;
        }
        if (globalRem != 1 && warnedGlobal59) {
            warnedGlobal59 = false;
        }

        // ——— EXPIRACIÓN GLOBAL ———
        if (globalVal > 1.0 && globalRem <= 0) {
            MGR.setGlobal(1.0, -1);
        }

        // ——— PRIVADOS ———
        for (ServerPlayerEntity player : ServerLifecycleHooks
                .getCurrentServer().getPlayerList().getPlayers()) {

            String name = player.getName().getString();
            long personalRem = MGR.getPlayerRemainingSeconds(player);
            double personalVal = MGR.getPlayer(player);

            // Aviso a 59s
            if (personalVal > 1.0 && personalRem == 59 && !warnedPrivate59.contains(name)) {
                IFormattableTextComponent msg = PREFIX.copy()
                        .append(new StringTextComponent(" Tu multiplicador PRIVADO termina en 60 segundos.")
                                .withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)));

                player.sendMessage(msg, player.getUUID());
                warnedPrivate59.add(name);
            }

            if (personalVal > 1.0 && personalRem == 1 && !warnedPrivate1.contains(name)) {
                IFormattableTextComponent msg = PREFIX.copy()
                        .append(new StringTextComponent(" Tu multiplicador PRIVADO finalizó.")
                                .withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)));

                player.sendMessage(msg, player.getUUID());
                warnedPrivate1.add(name);
            }

            // Reset del aviso si cambia de 59s
            if (personalRem != 59 && warnedPrivate59.contains(name)) {
                warnedPrivate59.remove(name);
            }
            if (personalRem != 1 && warnedPrivate1.contains(name)) {
                warnedPrivate1.remove(name);
            }

            // Expiración privada
            if (personalVal > 1.0 && personalRem <= 0) {
                MGR.setPlayer(name, 1.0, -1);
            }
        }
    }
}
