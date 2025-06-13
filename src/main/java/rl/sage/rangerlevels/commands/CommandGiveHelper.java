package rl.sage.rangerlevels.commands;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.items.CustomItemRegistry;

public class CommandGiveHelper {

    /**
     * Intenta entregar al jugador el ítem <itemId> en la cantidad <count>.
     * Dado que RangerItemDefinition.createStack siempre crea stacks de 1 con UUID único,
     * se iterará count veces para generar count instancias.
     *
     * @param player  Jugador a quien le damos el ítem
     * @param itemId  ID registrado en CustomItemRegistry
     * @param count   Cantidad a otorgar (>=1)
     * @param source  CommandSource para enviar mensajes de éxito/fallo
     * @return 1 si se procesó correctamente, 0 si hubo error (ítem desconocido)
     */
    public static int giveItem(ServerPlayerEntity player, String itemId, int count, CommandSource source) {
        // 1) Verificar existencia del ID en CustomItemRegistry
        if (!CustomItemRegistry.contains(itemId)) {
            source.sendFailure(new StringTextComponent(
                    TextFormatting.RED + "Ítem desconocido: " + itemId
            ));
            return 0;
        }

        // 2) Loop para crear y dar 'count' stacks de 1 unidad cada uno
        for (int i = 0; i < count; i++) {
            ItemStack stack = CustomItemRegistry.create(itemId, 1);
            if (stack.isEmpty()) {
                // En teoría no ocurre porque contains devolvió true, pero si falla, saltamos.
                continue;
            }
            boolean added = player.inventory.add(stack);
            if (!added) {
                // Si no cabe en inventario, dropearlo en el suelo
                player.drop(stack, false);
            }
        }

        // 3) Mensaje de éxito
        source.sendSuccess(new StringTextComponent(
                TextFormatting.GREEN + "Se entregó ×" + count + " " + itemId +
                        " a §b" + player.getName().getString()
        ), true);

        return 1;
    }
}
