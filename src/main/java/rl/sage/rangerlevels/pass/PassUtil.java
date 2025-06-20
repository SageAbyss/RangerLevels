// src/main/java/rl/sage/rangerlevels/pass/PassUtil.java
package rl.sage.rangerlevels.pass;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.capability.IPassCapability;

public class PassUtil {

    /**
     * Ahora recibe la capability ya segura (no volverá a lanzar excepción).
     */
    public static PassType checkAndRestorePass(ServerPlayerEntity player,
                                               IPassCapability cap) {
        PassType antes = PassManager.getCurrentPass(player);
        boolean sigueActivo = cap.hasActivePass();  // internamente restaura si expiró
        PassType despues = PassManager.getCurrentPass(player);

        if (antes.getTier() > despues.getTier()) {
            StringTextComponent msg = new StringTextComponent(
                    TextFormatting.GRAY + "❖ Tu pase temporal " +
                            antes.getGradientDisplayName() +
                            TextFormatting.GRAY + " ha expirado."
            );
            player.sendMessage(msg, player.getUUID());
        }
        return despues;
    }
}
