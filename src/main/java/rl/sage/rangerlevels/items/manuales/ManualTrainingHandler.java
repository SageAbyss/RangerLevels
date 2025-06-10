// File: rl/sage/rangerlevels/items/manuales/ManualTrainingHandler.java
package rl.sage.rangerlevels.items.manuales;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import rl.sage.rangerlevels.items.RangerItemDefinition;

public class ManualTrainingHandler {

    /**
     * Recorre el inventario en busca del Manual de Entrenamiento
     * de mayor tier y devuelve su bonus:
     *  Raro       → 0.10
     *  Épico      → 0.15
     *  Legendario → 0.20
     * Si no tiene ningún manual, devuelve 0.0.
     */
    public static double getBonus(ServerPlayerEntity player) {
        boolean hasLegendario = false;
        boolean hasEpico      = false;
        boolean hasRaro       = false;

        for (ItemStack stack : player.inventory.items) {
            String id = RangerItemDefinition.getIdFromStack(stack);
            if (ManualEntrenamientoLegendario.ID.equals(id)) {
                hasLegendario = true;
                break; // es el máximo posible
            } else if (ManualEntrenamientoEpico.ID.equals(id)) {
                hasEpico = true;
            } else if (ManualEntrenamientoRaro.ID.equals(id)) {
                hasRaro = true;
            }
        }

        if (hasLegendario) {
            return 0.20;
        } else if (hasEpico) {
            return 0.15;
        } else if (hasRaro) {
            return 0.10;
        } else {
            return 0.0;
        }
    }

    /**
     * Aplica el bonus (1 + getBonus) sobre una cantidad base de exp
     * y redondea al entero más cercano.
     */
    public static int applyBonus(ServerPlayerEntity player, int baseExp) {
        double bonus = getBonus(player);
        return (int) Math.round(baseExp * (1.0 + bonus));
    }
}
