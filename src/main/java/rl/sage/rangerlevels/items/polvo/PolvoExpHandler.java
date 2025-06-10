// File: rl/sage/rangerlevels/items/polvo/PolvoExpHandler.java
package rl.sage.rangerlevels.items.polvo;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import rl.sage.rangerlevels.items.RangerItemDefinition;

import java.util.ArrayList;
import java.util.List;

public class PolvoExpHandler {
    public static double getBonus(ServerPlayerEntity player) {
        boolean hasMitico  = false;
        boolean hasEstelar = false;
        boolean hasRaro    = false;

        List<ItemStack> stacksToCheck = new ArrayList<>(player.inventory.items);
        stacksToCheck.add(player.inventory.offhand.get(0));

        for (ItemStack stack : stacksToCheck) {
            if (stack.isEmpty()) continue;
            String id = RangerItemDefinition.getIdFromStack(stack);
            if (id == null) continue;

            if (PolvoExpMitico.ID.equals(id)) {
                hasMitico = true;
                break;
            } else if (PolvoExpEstelar.ID.equals(id)) {
                hasEstelar = true;
            } else if (PolvoExpRaro.ID.equals(id)) {
                hasRaro = true;
            }
        }

        if (hasMitico)  return 0.35;
        if (hasEstelar) return 0.20;
        if (hasRaro)    return 0.10;
        return 0.0;
    }

    public static int applyBonus(ServerPlayerEntity player, int baseExp) {
        double bonus = getBonus(player);
        return (int) Math.round(baseExp * (1.0 + bonus));
    }
}
