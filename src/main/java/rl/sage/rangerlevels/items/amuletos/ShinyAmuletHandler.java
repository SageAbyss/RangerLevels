// File: rl/sage/rangerlevels/items/ShinyAmuletHandler.java
package rl.sage.rangerlevels.items.amuletos;

import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.items.RangerItemDefinition;

import java.util.Random;

/**
 * Encapsula la lógica de uso del Amuleto Shiny:
 *   - Comprueba si el jugador lleva el Amuleto Shiny (Legendaria o Estelar) en inventario.
 *   - Usa el porcentaje de probabilidad definido en cada clase (5 % para Legendaria, 10 % para Estelar).
 *   - Si el roll es exitoso, convierte el Pokémon capturado en Shiny y consume 1 unidad del Amuleto Shiny correspondiente.
 *   - Envía un mensaje decorado al jugador cuando el amuleto se consuma.
 */
public class ShinyAmuletHandler {
    private static final Random RNG = new Random();

    /**
     * Intenta activar el efecto del Amuleto Shiny durante la captura.
     *
     * @param player Jugador que está capturando.
     * @param pkmn   La entidad Pixelmon capturada (antes de añadirse al inventario).
     */
    public static void tryUse(ServerPlayerEntity player, PixelmonEntity pkmn) {
        // 1) Buscar Amuleto Shiny (Estelar o Legendaria) en inventario
        boolean hasAmulet = false;
        int amuletSlot = -1;
        double chancePercent = 0.0;

        for (int slot = 0; slot < player.inventory.getContainerSize(); slot++) {
            ItemStack stack = player.inventory.getItem(slot);
            if (stack != null && !stack.isEmpty()) {
                String id = RangerItemDefinition.getIdFromStack(stack);

                // Prioridad a Estelar: 10 %
                if (ShinyAmuletEstelar.ID.equals(id)) {
                    hasAmulet = true;
                    amuletSlot = slot;
                    chancePercent = ShinyAmuletEstelar.getChancePercent();
                    break;
                }

                // Luego Legendaria: 5 %
                if (ShinyAmuletLegendaria.ID.equals(id)) {
                    hasAmulet = true;
                    amuletSlot = slot;
                    chancePercent = ShinyAmuletLegendaria.getChancePercent();
                    // No break; aún podría haber Estelar en otro slot,
                    // pero asumimos que el jugador no llevará ambos a la vez.
                    break;
                }
                if (ShinyAmuletMitico.ID.equals(id)) {
                    hasAmulet = true;
                    amuletSlot = slot;
                    chancePercent = ShinyAmuletMitico.getChancePercent();
                    // No break; aún podría haber Estelar en otro slot,
                    // pero asumimos que el jugador no llevará ambos a la vez.
                    break;
                }
            }
        }

        if (!hasAmulet) {
            // No lleva ningún Amuleto Shiny, no hacemos nada
            return;
        }

        // 2) Hacer el "roll" con la probabilidad adecuada
        double roll = RNG.nextDouble() * 100.0;
        if (roll < chancePercent) {
            // 3) Éxito: Convertir Pokémon en Shiny
            pkmn.getPokemon().setShiny(true);

            // 4) Consumir 1 unidad del amuleto encontrado
            ItemStack amuletStack = player.inventory.getItem(amuletSlot);
            amuletStack.shrink(1);
            if (amuletStack.isEmpty()) {
                player.inventory.setItem(amuletSlot, ItemStack.EMPTY);
            }

            // 5) Mensaje al jugador (bonito y decorado)
            player.displayClientMessage(
                    new StringTextComponent(
                            "§d✦§c§ki§d✦ Amuleto Shiny consumido ✦§c§ki§d✦ §r\n"
                                    + "§a¡Tu Pokémon se ha convertido en §e✧Shiny✧"
                    ),
                    false
            );
        }
        // Si el roll falla, no se consume el amuleto y no pasa nada más
    }
}
