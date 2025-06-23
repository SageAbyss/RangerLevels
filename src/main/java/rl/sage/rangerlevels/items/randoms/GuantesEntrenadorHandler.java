// File: rl/sage/rangerlevels/items/amuletos/GuantesEntrenadorHandler.java
package rl.sage.rangerlevels.items.randoms;

import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import java.util.Objects;

/**
 * Handler para el bonus de GuantesDelEntrenador.
 */
public class GuantesEntrenadorHandler {
    /**
     * Retorna 0.5 si el jugador tiene los guantes y el Pixelmon derrotado es salvaje nivel >= 50.
     */
    public static double getBonus(ServerPlayerEntity player, PixelmonEntity defeated) {
        // Verificar nivel
        int level = defeated.getPokemon().getPokemonLevel();
        if (level < 50) return 0.0;
        // Buscar en inventario
        for (ItemStack stack : player.inventory.items) {
            String id = RangerItemDefinition.getIdFromStack(stack);
            if (Objects.equals(GuantesDelEntrenador.ID, id)) {
                return 0.5;
            }
        }
        return 0.0;
    }
}