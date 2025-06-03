package rl.sage.rangerlevels.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.items.CustomItemRegistry;

/**
 * Clase helper donde definimos giveItem(...)
 */
public class CommandGiveHelper {

    /**
     * Intenta entregar al jugador el ítem <itemId> en la cantidad <count>.
     * @param player  Jugador a quien le damos el ítem
     * @param itemId  ID registrado en CustomItemRegistry
     * @param count   Cantidad a otorgar
     * @param source  CommandSource para enviar mensajes de éxito/fallo
     * @return 1 si se entregó correctamente, 0 si hubo error (ítem desconocido)
     */
    public static int giveItem(ServerPlayerEntity player, String itemId, int count, CommandSource source) {
        // 1) Generar el ItemStack desde el registry
        ItemStack stack = CustomItemRegistry.create(itemId, count);
        if (stack.isEmpty()) {
            // Si devolvió EMPTY, el ID no existe en el registry
            source.sendFailure(new StringTextComponent(
                    TextFormatting.RED + "Ítem desconocido: " + itemId
            ));
            return 0;
        }

        // 2) Intentar agregar al inventario. Si no cabe, se droppea al suelo.
        boolean added = player.inventory.add(stack.copy());
        if (!added) {
            player.drop(stack.copy(), false);
        }

        // 3) Mensaje de éxito (broadcast=true para que aparece también en logs si es operador)
        source.sendSuccess(new StringTextComponent(
                TextFormatting.GREEN + "Se entregó ×" + count + " " + itemId +
                        " a §b" + player.getName().getString()
        ), true);

        return 1;
    }
}
