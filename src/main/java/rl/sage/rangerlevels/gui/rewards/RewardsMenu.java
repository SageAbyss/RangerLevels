// src/main/java/rl/sage/rangerlevels/gui/rewards/RewardsMenu.java
package rl.sage.rangerlevels.gui.rewards;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.gui.PlayerInfoUtils;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.Arrays;

public class RewardsMenu {

    /**
     * Abre el menú de recompensas desbloqueadas del jugador.
     */
    public static void open(ServerPlayerEntity player) {
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.NOTE_BLOCK_BIT,
                SoundCategory.MASTER,
                1.0f,
                0.8f
        );
        // Inventario virtual de 27 slots (3 filas x 9)
        Inventory inv = new Inventory(27);
        inv.clearContent();

        // Botón de información (cabeza del jugador)
        inv.setItem(10, PlayerInfoUtils.getInfoItem(player, 10));

        // Botones de tipos de recompensa
        inv.setItem(12, MenuItemBuilder.createButton(
                "§6Recompensas por Nivel",
                Arrays.asList("§7Ver recompensas desbloqueadas por nivel"),
                Items.EXPERIENCE_BOTTLE,
                "reward_everylevel",
                12
        ));

        inv.setItem(14, MenuItemBuilder.createButton(
                "§dRecompensas por Paquete",
                Arrays.asList("§7Ver recompensas desbloqueadas por paquete"),
                Items.CHEST,
                "reward_package",
                14
        ));

        inv.setItem(16, MenuItemBuilder.createButton(
                "§bRecompensa por Nivel Exacto",
                Arrays.asList("§7Ver recompensa para el nivel exacto alcanzado"),
                Items.PAPER,
                "reward_exact",
                16
        ));

        // Botón de Volver al menú principal
        inv.setItem(22, MenuItemBuilder.createButton(
                "§cVolver",
                Arrays.asList("§6Regresa al menú principal"),
                Items.ARROW,
                "back",
                22
        ));

        // Abrir menú
        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, playerInv, p) -> new RewardsMenuContainer(windowId, playerInv, inv),
                new StringTextComponent("§6Recompensas")
        ));
    }
}
