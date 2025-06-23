// File: rl/sage/rangerlevels/gui/altar/InvocationSelectionMenu.java
package rl.sage.rangerlevels.gui.altar;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.items.altar.AltarRecipe;

import java.util.Collections;
import java.util.List;

public class InvocationSelectionMenu {
    public static void open(ServerPlayerEntity player, List<AltarRecipe> matches) {
        int needed = matches.size() + 1;
        int size = 9;
        while (size < matches.size()) {
            size += 9;
        }
        if (size < 27) size = 27;
        if (size > 54) size = 54;

        Inventory inv = new Inventory(size);
        inv.clearContent();

        // Para cada receta, crear un botón en un slot
        for (int i = 0; i < matches.size() && i < size - 1; i++) {
            AltarRecipe recipe = matches.get(i);
            ItemStack iconStack = recipe.getResultSupplier().get().copy();
            iconStack.setCount(1);

            String raw = recipe.getId().getPath();
            String friendly = toFriendlyName(raw);

            // Prefijo reset/color para evitar cursiva por defecto:
            // Aquí uso §r (reset) + §f (blanco), ajusta color si quieres otro:
            String display = "§r§f" + friendly;

            List<String> lore = Collections.singletonList("§7Haz clic para invocar: " + friendly);
            // El lore empieza con §7, que es color gris y resetea formatos previos.

            inv.setItem(i, MenuItemBuilder.createButton(
                    display,
                    lore,
                    iconStack.getItem(),
                    raw,
                    i
            ));
        }
        // Botón cancelar en la última casilla útil
        int cancelSlot = size - 1;
        String cancelName = "§r§cCancelar"; // §r reset, §c rojo, sin cursiva
        List<String> cancelLore = Collections.singletonList("§7No invocar nada");
        inv.setItem(cancelSlot, MenuItemBuilder.createButton(
                cancelName,
                cancelLore,
                net.minecraft.item.Items.BARRIER,
                "cancel",
                cancelSlot
        ));

        // Título sin cursiva:
        ITextComponent title = new StringTextComponent("✦ Elige invocación ✦")
                .withStyle(style -> style.withItalic(false));

        player.openMenu(new SimpleNamedContainerProvider(
                (wid, pinv, pl) -> new InvocationSelectionContainer(wid, pinv, inv, matches),
                title
        ));
    }

    private static String toFriendlyName(String raw) {
        String[] parts = raw.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)))
                    .append(p.substring(1))
                    .append(" ");
        }
        return sb.toString().trim();
    }
}
