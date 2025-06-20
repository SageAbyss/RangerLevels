// File: rl/sage/rangerlevels/gui/modificadores/ShinyMenu.java
package rl.sage.rangerlevels.gui.modificadores;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.util.PlayerSoundUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;

import java.util.Collections;

public class ShinyMenu {
    public static void open(ServerPlayerEntity player) {
        PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);

        Inventory inv = new Inventory(27);
        inv.clearContent();

        // slot 11 = center of row 2: Shiny
        inv.setItem(12, MenuItemBuilder.createButton(
                "§aShiny",
                Collections.singletonList("§7Haz tu Pokémon Shiny"),
                Items.NAME_TAG,
                "shiny",
                12
        ));
        // slot 13: No Shiny
        inv.setItem(14, MenuItemBuilder.createButton(
                "§cNo Shiny",
                Collections.singletonList("§7Quita el efecto Shiny"),
                Items.NAME_TAG,
                "noshiny",
                14
        ));
        // slot 26: cerrar
        inv.setItem(26, MenuItemBuilder.createButton(
                "§cCerrar",
                Collections.singletonList("§7Sin cambios"),
                net.minecraft.item.Items.BARRIER,
                "close",
                26
        ));

        player.openMenu(new SimpleNamedContainerProvider(
                (wid, pinv, pl) -> new ShinyMenuContainer(wid, pinv, inv),
                new StringTextComponent("✦ Seleccionar Shiny ✦")
        ));
    }
}
