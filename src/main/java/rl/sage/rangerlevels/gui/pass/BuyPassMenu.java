package rl.sage.rangerlevels.gui.pass;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Items;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.gui.PlayerInfoUtils;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.pass.PassType;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.Arrays;

public class BuyPassMenu {

    public static void open(ServerPlayerEntity player) {
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.NOTE_BLOCK_BIT,
                SoundCategory.MASTER,
                1.0f,
                0.8f
        );
        Inventory inv = new Inventory(27);
        inv.clearContent();

        // Super Pass
        inv.setItem(12, MenuItemBuilder.createButton(
                PassType.SUPER.getGradientDisplayName(),  // gradient name
                Arrays.asList(
                        "§7XP ×1.25 Exp",
                        "§7Limitador diario +10%",
                        "§7Recompensas exclusivas Super"
                ),
                Items.EMERALD,
                "buy_super",
                12
        ));

        // Ultra Pass
        inv.setItem(14, MenuItemBuilder.createButton(
                PassType.ULTRA.getGradientDisplayName(),
                Arrays.asList(
                        "§7XP ×1.5 Exp",
                        "§7Limitador diario +20%",
                        "§7Recompensas exclusivas Ultra"
                ),
                Items.DIAMOND,
                "buy_ultra",
                14
        ));

        // Master Pass
        inv.setItem(16, MenuItemBuilder.createButton(
                PassType.MASTER.getGradientDisplayName(),
                Arrays.asList(
                        "§7XP ×2.0 Exp",
                        "§7Limitador diario +50%",
                        "§7Recompensas exclusivas Master"
                ),
                Items.NETHER_STAR,
                "buy_master",
                16
        ));

        // Cabeza con PlayerInfoUtils
        inv.setItem(10, PlayerInfoUtils.getInfoItem(player, 10));

        // Botón Volver
        inv.setItem(22, MenuItemBuilder.createButton(
                "§cVolver",
                Arrays.asList("§7Regresa al menú principal"),
                Items.ARROW,
                "back",
                22
        ));

        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, playerInv, p) ->
                        new BuyPassMenuContainer(windowId, playerInv, inv),
                new StringTextComponent("§6Comprar Pase")
        ));
    }
}
