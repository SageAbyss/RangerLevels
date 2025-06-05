package rl.sage.rangerlevels.gui.rewards;

import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.item.ItemStack;
import rl.sage.rangerlevels.gui.BaseMenuContainer6;

/**
 * Antes extendía ChestContainer(GENERIC_9x6), ahora extiende BaseMenuContainer6.
 */
public class ExactLevelMenuContainer extends BaseMenuContainer6 {
    private final Inventory menuInv;

    public ExactLevelMenuContainer(int windowId, PlayerInventory inv, Inventory menuInv) {
        super(windowId, inv, menuInv);
        this.menuInv = menuInv;
    }

    @Override
    protected void handleButtonClick(String buttonId, ServerPlayerEntity player) {
        // 1) Paginación: "page:<número>"
        if (buttonId.startsWith("page:")) {
            try {
                int newPage = Integer.parseInt(buttonId.split(":")[1]);
                ExactLevelMenu.open(player, newPage);
            } catch (NumberFormatException ignore) {}
            return;
        }

        switch (buttonId) {
            case "claim_all":
                ExactLevelMenu.claimAll(player);
                player.getServer().execute(() -> {
                    ExactLevelMenu.open(player, 1);
                });
                break;

            case "back":
                player.closeContainer();
                player.getServer().execute(() -> {
                    RewardsMenu.open(player);
                });
                break;

            default:
                // "Exact.<nivel>.<ruta>.<page>"
                if (buttonId.startsWith("Exact.")) {
                    String[] parts = buttonId.split("\\.");
                    if (parts.length == 4) {
                        String nivel = parts[1];
                        String ruta  = parts[2];
                        int page;
                        try {
                            page = Integer.parseInt(parts[3]);
                        } catch (NumberFormatException e) {
                            page = 1;
                        }
                        ExactLevelMenu.claimSingle(player, nivel, ruta);
                        player.getServer().execute(() -> {
                            ExactLevelMenu.open(player, 1);
                        });
                    }
                }
                break;
        }
    }
}
