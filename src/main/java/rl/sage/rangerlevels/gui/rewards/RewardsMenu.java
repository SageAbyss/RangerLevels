package rl.sage.rangerlevels.gui.rewards;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Items;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.gui.PlayerInfoUtils;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.Arrays;

/**
 * Abre el menú “Recompensas” (3×9). Contiene:
 *  - Botón de info (slot 10).
 *  - “Recompensas por Nivel” (slot 12, tag = "reward_everylevel").
 *  - “Recompensas por Paquete” (slot 14, tag = "reward_package").
 *  - “Recompensa por Nivel Exacto” (slot 16, tag = "reward_exact").
 *  - Botón “Volver” (slot 22, tag = "back").
 */
public class RewardsMenu {

    public static void open(ServerPlayerEntity player) {
        // Reproducir sonido al abrir
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.NOTE_BLOCK_BIT,
                SoundCategory.MASTER,
                1.0f,
                0.8f
        );

        // Inventario virtual de 27 ranuras (3 filas × 9 columnas)
        Inventory inv = new Inventory(27);
        inv.clearContent();

        // Botón de info (slot 10)
        inv.setItem(10, PlayerInfoUtils.getInfoItem(player, 10));

        // “Recompensas por Nivel” (slot 12, tag = "reward_everylevel")
        inv.setItem(12, MenuItemBuilder.createButton(
                "§6Recompensas por Nivel",
                Arrays.asList("§7Ver recompensas desbloqueadas por nivel"),
                Items.EXPERIENCE_BOTTLE,
                "reward_everylevel",
                12
        ));

        // “Recompensas por Paquete” (slot 14, tag = "reward_package")
        inv.setItem(14, MenuItemBuilder.createButton(
                "§dRecompensas por Paquete",
                Arrays.asList("§7Ver recompensas desbloqueadas por paquete"),
                Items.CHEST,
                "reward_package",
                14
        ));

        // “Recompensa por Nivel Exacto” (slot 16, tag = "reward_exact")
        inv.setItem(16, MenuItemBuilder.createButton(
                "§bRecompensa por Nivel Exacto",
                Arrays.asList("§7Ver recompensa para el nivel exacto alcanzado"),
                Items.PAPER,
                "reward_exact",
                16
        ));

        // Botón “Volver” (slot 22, tag = "back")
        inv.setItem(22, MenuItemBuilder.createButton(
                "§cVolver",
                Arrays.asList("§6Regresa al menú principal"),
                Items.ARROW,
                "back",
                22
        ));

        // Abrir el contenedor
        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, playerInv, p) -> new RewardsMenuContainer(windowId, playerInv, inv),
                new StringTextComponent("§6Recompensas")
        ));
    }
}
