// File: rl/sage/rangerlevels/items/reliquias/ExpRepeaterHandler.java
package rl.sage.rangerlevels.items.reliquias;

import net.minecraft.entity.player.ServerPlayerEntity;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import java.util.Optional;

public class ExpRepeaterHandler {

    /**
     * Devuelve el bonus según la Reliquia Temporal de mayor tier:
     *   Común   → 0.10
     *   Raro    → 0.15
     *   Legend. → 0.25
     *   Estelar → 0.50
     */
    public static double getBonus(ServerPlayerEntity player) {
        boolean hasEstelar    = false;
        boolean hasLegendario = false;
        boolean hasRaro       = false;
        boolean hasComun      = false;

        for (net.minecraft.item.ItemStack stack : player.inventory.items) {
            String id = RangerItemDefinition.getIdFromStack(stack);
            if (ReliquiaTemporalEstelar.ID.equals(id)) {
                hasEstelar = true; break;
            } else if (ReliquiaTemporalLegendario.ID.equals(id)) {
                hasLegendario = true;
            } else if (ReliquiaTemporalRaro.ID.equals(id)) {
                hasRaro = true;
            } else if (ReliquiaTemporalComun.ID.equals(id)) {
                hasComun = true;
            }
        }
        if (hasEstelar)    return 0.50;
        if (hasLegendario) return 0.25;
        if (hasRaro)       return 0.15;
        if (hasComun)      return 0.10;
        return 0.0;
    }

    /**
     * Repite la última EXP obtenida y le añade el bonus de la reliquia.
     * Usa LevelProvider.giveExpAndNotify para dar la EXP repetida.
     * (Para que esto funcione necesitas que tu ILevel exponga el último valor
     *  de EXP ganado, p.ej. cap.getLastGain().)
     */
    public static void repeatLastExp(ServerPlayerEntity player) {
        double bonus = getBonus(player);
        Optional<rl.sage.rangerlevels.capability.ILevel> opt = LevelProvider.get(player).resolve();
        if (!opt.isPresent()) return;
        rl.sage.rangerlevels.capability.ILevel cap = opt.get();

        int last = cap.getLastGain();           // <-- necesitas este método en tu ILevel
        if (last <= 0) return;

        int toGive = (int) Math.round(last * (1.0 + bonus));
        LevelProvider.giveExpAndNotify(player, toGive);
    }
}
