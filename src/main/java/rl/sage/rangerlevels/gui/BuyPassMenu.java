package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.pass.PassManager.PassType;

import java.util.ArrayList;
import java.util.List;

public class BuyPassMenu {
    public static void open(ServerPlayerEntity player) {
        // Inventario tipo cofre 3x9
        Inventory inv = new Inventory(27);
        inv.clearContent();

        // Definimos manualmente los slots donde irán SUPER, ULTRA y MASTER
        int[] slots = {10, 12, 14};
        int idx = 0;
        for (PassType type : PassType.values()) {
            if (type == PassType.FREE) continue;  // omitimos FREE
            if (idx >= slots.length) break;

            int slot = slots[idx++];
            // Título: usamos el nombre gradient y lo convertimos a String
            String title = type.getGradientDisplayName().getString();
            // Lore: descripción + URL
            List<String> lore = new ArrayList<>();
            lore.add(type.getDescription());
            lore.add("Compra: " + type.getPurchaseUrl());

            inv.setItem(
                    slot,
                    MenuItemBuilder.createButton(
                            title,
                            lore,
                            // Elegimos el ícono según el pase
                            type == PassType.SUPER ? Items.EMERALD :
                                    type == PassType.ULTRA ? Items.DIAMOND :
                                            Items.NETHER_STAR,
                            type.name().toLowerCase(),
                            slot
                    )
            );
        }

        // Botón "Pase Actual" en slot 16
        {
            String title = "📜 Pase Actual";
            List<String> lore = new ArrayList<>();
            lore.add("Tu pase actual:");
            lore.add(player.getDisplayName().getString() + " → " +
                    PassManager.getPass(player).getGradientDisplayName().getString());

            inv.setItem(
                    16,
                    MenuItemBuilder.createButton(
                            title,
                            lore,
                            Items.PAPER,
                            "current",
                            16
                    )
            );
        }

        // Item opcional: info del jugador en slot 22 (usa tu clase existente)
        inv.setItem(22, PlayerInfoUtils.getInfoItem(player, 22));

        // Abrimos el contenedor
        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, invPlayer, p) -> new BuyPassContainer(windowId, invPlayer, inv),
                new StringTextComponent("§6Comprar Pase")
        ));
    }
}
