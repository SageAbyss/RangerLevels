package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.pass.PassManager;

import java.util.Arrays;

public class BuyPassMenu {
    public static void open(ServerPlayerEntity player) {
        Inventory inv = new Inventory(27);
        inv.clearContent();

        inv.setItem(10, MenuItemBuilder.createButton(
                PassManager.PassType.SUPER.getDescription(),
                Arrays.asList(
                        PassManager.PassType.SUPER.getGradientDisplayName().getString(),
                        "Haz clic para comprar"
                ),
                Items.EMERALD,
                "super",
                10
        ));
        inv.setItem(12, MenuItemBuilder.createButton(
                PassManager.PassType.ULTRA.getDescription(),
                Arrays.asList(
                        PassManager.PassType.ULTRA.getGradientDisplayName().getString(),
                        "Haz clic para comprar"
                ),
                Items.DIAMOND,
                "ultra",
                12
        ));
        inv.setItem(14, MenuItemBuilder.createButton(
                PassManager.PassType.MASTER.getDescription(),
                Arrays.asList(
                        PassManager.PassType.MASTER.getGradientDisplayName().getString(),
                        "Haz clic para comprar"
                ),
                Items.NETHER_STAR,
                "master",
                14
        ));
        inv.setItem(16, MenuItemBuilder.createButton(
                "📜 Pase Actual",
                Arrays.asList(
                        PassManager.getPass(player).getGradientDisplayName().getString(),
                        "Tu pase actual"
                ),
                Items.PAPER,
                "current",
                16
        ));

        // Info siempre: solo el nombre del jugador
        inv.setItem(22, PlayerInfoUtils.getInfoItem(player, 22));

        player.openMenu(new SimpleNamedContainerProvider(
                (wid, pinv, p) -> new BuyPassContainer(wid, pinv, inv),
                new StringTextComponent("§6Comprar Pase")
        ));
    }
}
