package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import rl.sage.rangerlevels.gui.help.HelpMenu;
import rl.sage.rangerlevels.gui.pass.BuyPassMenu;
import rl.sage.rangerlevels.gui.rewards.RewardsMenu;

/**
 * Contenedor para “Main Menu”. Hereda toda la lógica de bloqueo/restauración
 * de BaseMenuContainer y solo implementa la acción según buttonId.
 */
public class MainMenuContainer extends BaseMenuContainer {

    public MainMenuContainer(int windowId,
                             PlayerInventory playerInv,
                             Inventory menuInventory) {
        super(windowId, playerInv, menuInventory);
    }

    @Override
    protected void handleButtonClick(String buttonId, ServerPlayerEntity player) {
        switch (buttonId) {
            case "help":
                player.closeContainer();
                player.getServer().execute(() -> {
                    HelpMenu.open(player);
                });
                break;
            case "rewards":
                player.closeContainer();
                player.getServer().execute(() -> {
                    RewardsMenu.open(player);
                });
                break;
            case "buy":
                player.closeContainer();
                player.getServer().execute(() -> {
                    BuyPassMenu.open(player);
                });
                break;
            case "info":
                // Ejemplo, si tuvieras InfoMenu:
                // player.closeContainer();
                // InfoMenu.open(player);
                break;
            default:
                break;
        }
    }
}
