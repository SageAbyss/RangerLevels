// File: rl/sage/rangerlevels/gui/modificadores/NaturalezaMenu.java
package rl.sage.rangerlevels.gui.modificadores;

import com.pixelmonmod.pixelmon.api.pokemon.Nature;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.util.PlayerSoundUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;

public class NaturalezaMenu {
    public static void open(ServerPlayerEntity player) {
        PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);

        // 3 filas × 9 columnas = 27 slots
        Inventory inv = new Inventory(27);
        inv.clearContent();

        Nature[] all = Nature.values();
        for (int i = 0; i < all.length && i < 27; i++) {
            Nature nat = all[i];
            ItemStack icon = MenuItemBuilder.createButton(
                    "§a" + nat.getLocalizedName(),
                    java.util.Collections.singletonList("§7Selecciona esta naturaleza"),
                    net.minecraft.item.Items.NAME_TAG,
                    nat.name(),
                    i
            );
            inv.setItem(i, icon);
        }

        // Slot 26: cerrar
        ItemStack close = MenuItemBuilder.createButton(
                "§cCerrar",
                java.util.Collections.singletonList("§7Sin cambios"),
                net.minecraft.item.Items.BARRIER,
                "close",
                26
        );
        inv.setItem(26, close);

        player.openMenu(new SimpleNamedContainerProvider(
                (wid, pinv, pl) -> new NaturalezaMenuContainer(wid, pinv, inv),
                new StringTextComponent("✦ Seleccionar Naturaleza ✦")
        ));
    }
}
