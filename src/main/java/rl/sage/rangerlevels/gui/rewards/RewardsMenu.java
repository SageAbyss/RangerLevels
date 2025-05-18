// src/main/java/rl/sage/rangerlevels/gui/rewards/RewardsMenu.java
package rl.sage.rangerlevels.gui.rewards;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.gui.PlayerInfoUtils;
import java.util.Arrays;

public class RewardsMenu {

    /**
     * Abre el menú de recompensas desbloqueadas del jugador.
     */
    public static void open(ServerPlayerEntity player) {
        // Inventario virtual de 27 slots (3 filas x 9)
        Inventory inv = new Inventory(27);
        inv.clearContent();

        // Botón de información (cabeza del jugador)
        inv.setItem(10, PlayerInfoUtils.getInfoItem(player, 10));

        // Botones de tipos de recompensa
        inv.setItem(12, MenuItemBuilder.createButton(
                "§6Recompensas por Nivel",
                Arrays.asList("Ver recompensas desbloqueadas por nivel"),
                Items.EXPERIENCE_BOTTLE,
                "reward_level",
                12
        ));

        inv.setItem(14, MenuItemBuilder.createButton(
                "§dRecompensas por Paquete",
                Arrays.asList("Ver recompensas desbloqueadas por paquete"),
                Items.CHEST,
                "reward_package",
                14
        ));

        inv.setItem(16, MenuItemBuilder.createButton(
                "§bRecompensa por Nivel Exacto",
                Arrays.asList("Ver recompensa para el nivel exacto alcanzado"),
                Items.PAPER,
                "reward_exact",
                16
        ));

        // Abrir menú
        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, playerInv, p) -> new RewardsMenuContainer(windowId, playerInv, inv),
                new StringTextComponent("§6Recompensas RangerLevels")
        ));
    }
}
