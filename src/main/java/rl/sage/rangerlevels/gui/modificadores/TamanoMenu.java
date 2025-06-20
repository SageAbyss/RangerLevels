// File: rl/sage/rangerlevels/gui/modificadores/TamanoMenu.java
package rl.sage.rangerlevels.gui.modificadores;

import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.util.PlayerSoundUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;

public class TamanoMenu {
    public static void open(ServerPlayerEntity player) {
        PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);

        Inventory inv = new Inventory(27);
        inv.clearContent();

        EnumGrowth[] all = EnumGrowth.values();
        for (int i = 0; i < all.length && i < 27; i++) {
            inv.setItem(i, MenuItemBuilder.createButton(
                    "§a" + all[i].getLocalizedName(),
                    java.util.Collections.singletonList("§7Selecciona este tamaño"),
                    net.minecraft.item.Items.NAME_TAG,
                    all[i].name(),
                    i
            ));
        }
        // cerrar en slot 26
        inv.setItem(26, MenuItemBuilder.createButton(
                "§cCerrar",
                java.util.Collections.singletonList("§7Sin cambios"),
                net.minecraft.item.Items.BARRIER,
                "close",
                26
        ));

        player.openMenu(new SimpleNamedContainerProvider(
                (wid, pinv, pl) -> new TamanoMenuContainer(wid, pinv, inv),
                new StringTextComponent("✦ Seleccionar Tamaño ✦")
        ));
    }
}
