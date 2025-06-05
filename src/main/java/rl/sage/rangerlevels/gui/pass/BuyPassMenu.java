package rl.sage.rangerlevels.gui.pass;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Items;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.Arrays;

/**
 * Abre el menú “Comprar Pase” (3×9) con botones para Super, Ultra y Master Pass,
 * más un botón “Volver” (tag = "back") y un ítem de info del jugador (slot 10).
 */
public class BuyPassMenu {

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

        // Super Pass (slot 12)
        inv.setItem(12, MenuItemBuilder.createButton(
                rl.sage.rangerlevels.pass.PassType.SUPER.getGradientDisplayName(),
                Arrays.asList(
                        "§7XP ×1.25 Exp",
                        "§7Limitador diario +10%",
                        "§7Recompensas exclusivas Super"
                ),
                Items.EMERALD,
                "buy_super",
                12
        ));

        // Ultra Pass (slot 14)
        inv.setItem(14, MenuItemBuilder.createButton(
                rl.sage.rangerlevels.pass.PassType.ULTRA.getGradientDisplayName(),
                Arrays.asList(
                        "§7XP ×1.5 Exp",
                        "§7Limitador diario +20%",
                        "§7Recompensas exclusivas Ultra"
                ),
                Items.DIAMOND,
                "buy_ultra",
                14
        ));

        // Master Pass (slot 16)
        inv.setItem(16, MenuItemBuilder.createButton(
                rl.sage.rangerlevels.pass.PassType.MASTER.getGradientDisplayName(),
                Arrays.asList(
                        "§7XP ×2.0 Exp",
                        "§7Limitador diario +50%",
                        "§7Recompensas exclusivas Master"
                ),
                Items.NETHER_STAR,
                "buy_master",
                16
        ));

        // Info del jugador (slot 10)
        inv.setItem(10, rl.sage.rangerlevels.gui.PlayerInfoUtils.getInfoItem(player, 10));

        // Botón “Volver” (slot 22)
        inv.setItem(22, MenuItemBuilder.createButton(
                "§cVolver",
                Arrays.asList("§6Regresa al menú principal"),
                Items.ARROW,
                "back",
                22
        ));

        // Abrir el contenedor
        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, playerInv, p) -> new BuyPassMenuContainer(windowId, playerInv, inv),
                new StringTextComponent("§6Comprar Pase")
        ));
    }
}
