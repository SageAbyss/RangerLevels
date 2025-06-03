package rl.sage.rangerlevels.limiter;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.LazyOptional;
import rl.sage.rangerlevels.capability.ILimiter;
import rl.sage.rangerlevels.capability.LimiterProvider;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.pass.PassType;
import rl.sage.rangerlevels.purge.PlayerPurgeNotifier;
import rl.sage.rangerlevels.purge.PurgeManager;
import rl.sage.rangerlevels.util.GradientText;

public class LimiterHelper {

    /**
     * Intenta otorgar EXP al jugador respetando:
     *  - Fin de temporada (purge)
     *  - Limitador global (config)
     *  - Límite diario base ajustado según su pase
     */
    public static void giveExpWithLimit(ServerPlayerEntity player, int amount) {
        // 1) Si la temporada terminó, notificar y bloquear EXP
        if (PurgeManager.isPurgeEnded()) {
            if (!PlayerPurgeNotifier.hasNotified(player)) {
                player.sendMessage(
                        new StringTextComponent("§cEl pase ha finalizado. Ya no puedes obtener EXP."),
                        Util.NIL_UUID
                );
                PlayerPurgeNotifier.markNotified(player);
            }
            return;
        }

        // 2) Si el limitador está desactivado en config, damos EXP sin tope
        if (!LimiterManager.isEnabled()) {
            LevelProvider.giveExpAndNotify(player, amount);
            return;
        }

        // 3) Intentamos obtener el capability de limitador
        LazyOptional<ILimiter> opt = LimiterProvider.get(player);
        if (opt.isPresent()) {
            ILimiter cap = opt.orElseThrow(IllegalStateException::new);

            // 4) Calculamos el tope base y ajustado según el pase del jugador
            int baseMax = LimiterManager.getMaxExp();
            PassType pass = PassManager.getCurrentPass(player);

            float factor;
            switch (pass) {
                case SUPER:
                    factor = 1.10f;
                    break;
                case ULTRA:
                    factor = 1.20f;
                    break;
                case MASTER:
                    factor = 1.50f;
                    break;
                default: // FREE y cualquier otro caso
                    factor = 1.0f;
            }

            int maxAllowed = (int)(baseMax * factor);

            // 5) Intentamos agregar EXP sin superar el tope ajustado
            int granted = cap.addExp(amount, maxAllowed);

            if (granted > 0) {
                // Si se agregó algo, reseteamos notificación y damos EXP real
                cap.setNotified(false);
                LevelProvider.giveExpAndNotify(player, granted);
            } else {
                // Si no queda EXP y no se ha notificado, avisamos al jugador
                if (!cap.wasNotified()) {
                    IFormattableTextComponent sep = GradientText.of(
                            "                                                                      ",
                            "#FF0000", "#FF7F00", "#FFFF00",
                            "#00FF00", "#0000FF", "#4B0082", "#9400D3"
                    ).withStyle(TextFormatting.STRIKETHROUGH);
                    IFormattableTextComponent msg = new StringTextComponent(
                            TextFormatting.GOLD + "¡Has alcanzado el límite de EXP diario!"
                    );
                    player.displayClientMessage(sep, false);
                    player.displayClientMessage(msg, false);
                    player.displayClientMessage(sep, false);
                    cap.setNotified(true);
                }
                // Si ya notificó, no hacemos nada más
            }

        } else {
            // Fallback: si no hay capability, damos EXP sin límite
            LevelProvider.giveExpAndNotify(player, amount);
        }
    }
}
