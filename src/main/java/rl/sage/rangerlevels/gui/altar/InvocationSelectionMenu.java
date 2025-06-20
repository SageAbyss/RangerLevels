// File: rl/sage/rangerlevels/gui/altar/InvocationSelectionMenu.java
package rl.sage.rangerlevels.gui.altar;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.items.altar.AltarRecipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InvocationSelectionMenu {
    public static void open(ServerPlayerEntity player, List<AltarRecipe> matches) {
        // Sonido de apertura opcional
        // PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);

        // Determinar tamaño de inventario: múltiplo de 9, >= matches.size(), max 54.
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
            String display = toFriendlyName(raw);
            List<String> lore = Collections.singletonList("§7Haz clic para invocar: " + display);
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
        inv.setItem(cancelSlot, MenuItemBuilder.createButton(
                "§cCancelar",
                Collections.singletonList("§7No invocar nada"),
                net.minecraft.item.Items.BARRIER,
                "cancel",
                cancelSlot
        ));

        ITextComponent title = new StringTextComponent("✦ Elige invocación ✦");
        player.openMenu(new SimpleNamedContainerProvider(
                (wid, pinv, pl) -> new InvocationSelectionContainer(wid, pinv, inv, matches),
                title
        ));
    }

    private static String toFriendlyName(String raw) {
        // Ejemplo: reemplazar '_' por espacio, capitalizar palabras
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

    private static ItemStack decorateButton(ItemStack stack) {
        // Opcional: aplicar flags de ocultar nbt o efecto glow
        // Por ejemplo:
        // CompoundNBT tag = stack.getOrCreateTag();
        // NBTUtils.applyAllHideFlags(tag);
        // stack.setTag(tag);
        return stack;
    }

    private static ItemStack ItemStackButton(ItemStack icon, String name, List<String> lore, String buttonId, int slot) {
        // Usa tu MenuItemBuilder
        return MenuItemBuilder.createButton(
                name,
                lore,
                icon.getItem(),
                buttonId,
                slot
        );
    }
}
