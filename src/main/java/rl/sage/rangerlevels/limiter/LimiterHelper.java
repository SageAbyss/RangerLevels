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
import rl.sage.rangerlevels.purge.PlayerPurgeNotifier;
import rl.sage.rangerlevels.purge.PurgeManager;
import rl.sage.rangerlevels.util.GradientText;

public class LimiterHelper {

    public static void giveExpWithLimit(ServerPlayerEntity player, int amount) {
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
        if (!LimiterManager.isEnabled()) {
            LevelProvider.giveExpAndNotify(player, amount);
            return;
        }

        LazyOptional<ILimiter> opt = LimiterProvider.get(player);
        if (opt.isPresent()) {
            ILimiter cap = opt.orElseThrow(IllegalStateException::new);
            int granted = cap.addExp(amount, LimiterManager.getMaxExp());

            if (granted > 0) {
                // cualquier vez que repongamos EXP, reseteamos el flag
                cap.setNotified(false);
                LevelProvider.giveExpAndNotify(player, granted);
            } else {
                // si no queda EXP y aún no se notificó
                if (!cap.wasNotified()) {
                    IFormattableTextComponent sep = GradientText.of(
                            "                                                                      ",
                            "#FF0000","#FF7F00","#FFFF00",
                            "#00FF00","#0000FF","#4B0082","#9400D3"
                    ).withStyle(TextFormatting.STRIKETHROUGH);
                    IFormattableTextComponent msg = new StringTextComponent(
                            TextFormatting.GOLD + "¡Has alcanzado el límite de EXP diario!"
                    );
                    player.displayClientMessage(sep, false);
                    player.displayClientMessage(msg, false);
                    player.displayClientMessage(sep, false);
                    cap.setNotified(true);
                }
                // si ya notificó, no hacemos nada
            }
        } else {
            // fallback
            LevelProvider.giveExpAndNotify(player, amount);
        }
    }
}
