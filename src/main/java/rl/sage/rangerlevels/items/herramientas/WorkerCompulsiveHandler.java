// File: rl/sage/rangerlevels/items/bonuses/WorkerCompulsiveHandler.java
package rl.sage.rangerlevels.items.herramientas;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import rl.sage.rangerlevels.config.ItemsConfig;
import rl.sage.rangerlevels.items.RangerItemDefinition;

public class WorkerCompulsiveHandler {

    /** Devuelve el bonus de talar según el mejor hacha que tenga. */
    public static double getBonus(ServerPlayerEntity player) {
        boolean hasEpic = false, hasRare = false;
        for (ItemStack stack : player.inventory.items) {
            String id = RangerItemDefinition.getIdFromStack(stack);
            if (HachaTrabajadorCompulsivoEpico.ID.equals(id)) {
                hasEpic = true;
                break;
            } else if (HachaTrabajadorCompulsivoRaro.ID.equals(id)) {
                hasRare = true;
            }
        }
        if (hasEpic) {
            return ItemsConfig.get().axeBonus.epicPercent / 100.0;
        } else if (hasRare) {
            return ItemsConfig.get().axeBonus.rarePercent / 100.0;
        } else {
            return 0.0;
        }
    }

    /** Aplica el bonus sobre una EXP base y redondea al entero más cercano. */
    public static int applyBonus(ServerPlayerEntity player, int baseExp) {
        double bonus = getBonus(player);
        return (int)Math.round(baseExp * (1.0 + bonus));
    }
}
