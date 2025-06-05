package rl.sage.rangerlevels.gui.rewards;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.item.ItemStack;
import rl.sage.rangerlevels.gui.BaseMenuContainer6;
import rl.sage.rangerlevels.gui.MenuSlot;

/**
 * Antes extendía ChestContainer(GENERIC_9x6), ahora extiende BaseMenuContainer6.
 */
public class PackagesLevelMenuContainer extends BaseMenuContainer6 {
    private final Inventory menuInv;

    public PackagesLevelMenuContainer(int windowId, PlayerInventory inv, Inventory menuInv) {
        super(windowId, inv, menuInv);
        this.menuInv = menuInv;
    }

    @Override
    protected void handleButtonClick(String buttonId, ServerPlayerEntity player) {
        // 1) Paginación: "page:<número>"
        if (buttonId.startsWith("page:")) {
            try {
                int newPage = Integer.parseInt(buttonId.split(":")[1]);
                PackagesLevelMenu.open(player, newPage);
            } catch (NumberFormatException ignore) {}
            return;
        }

        switch (buttonId) {
            case "claim_all":
                PackagesLevelMenu.claimAll(player);
                player.getServer().execute(() -> {
                    PackagesLevelMenu.open(player, 1);
                });
                break;

            case "back":
                player.closeContainer();
                player.getServer().execute(() -> {
                    RewardsMenu.open(player);
                });
                break;

            default:
                // "Packages.<iv>.<nivel>.<ruta>.<page>"
                if (buttonId.startsWith("Packages.")) {
                    String[] parts = buttonId.split("\\.");
                    if (parts.length == 5) {
                        String iv     = parts[1];
                        String nivel  = parts[2];
                        String ruta   = parts[3];
                        int pagina;
                        try {
                            pagina = Integer.parseInt(parts[4]);
                        } catch (NumberFormatException e) {
                            pagina = 1;
                        }
                        PackagesLevelMenu.claimSingle(player, iv, nivel, ruta);
                        player.getServer().execute(() -> {
                            PackagesLevelMenu.open(player, 1);
                        });
                    }
                }
                break;
        }
    }
}
