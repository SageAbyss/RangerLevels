package rl.sage.rangerlevels.pass;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.capability.IPassCapability;
import rl.sage.rangerlevels.capability.PassCapabilities;

/**
 * Clase utilitaria para verificar si un pase expiró, restaurar el anterior y notificar al jugador.
 */
public class PassUtil {

    /**
     * Chequea el pase actual; si expiró, restaura el anterior y notifica al jugador.
     * @return PassType actualizado del jugador.
     */
    public static PassType checkAndRestorePass(ServerPlayerEntity player) {
        IPassCapability cap = PassCapabilities.get(player);
        PassType antes = PassManager.getCurrentPass(player);
        boolean sigueActivo = cap.hasActivePass();  // al llamar, restaurará si expiró
        PassType despues = PassManager.getCurrentPass(player);

        // Si “antes” tenía un tier mayor que “después”, el pase temporal expiró y se restauró algo menor
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
