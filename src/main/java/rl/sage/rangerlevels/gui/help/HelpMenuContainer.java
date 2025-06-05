package rl.sage.rangerlevels.gui.help;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import rl.sage.rangerlevels.gui.BaseMenuContainer;
import rl.sage.rangerlevels.gui.HelpButtonUtils;
import rl.sage.rangerlevels.gui.MainMenu;

/**
 * Contenedor para el menú de Ayuda. Extiende BaseMenuContainer para heredar
 * la lógica de interceptar clics y restaurar slots. En el constructor
 * imprimimos un mensaje de depuración para verificar que se creó correctamente.
 */
public class HelpMenuContainer extends BaseMenuContainer {

    public HelpMenuContainer(int windowId,
                             PlayerInventory playerInv,
                             Inventory menuInventory) {
        super(windowId, playerInv, menuInventory);
    }

    @Override
    protected void handleButtonClick(String buttonId, ServerPlayerEntity player) {
        switch (buttonId) {
            case "back":
                // Volver al menú principal
                player.closeContainer();
                player.getServer().execute(() -> {
                    MainMenu.open(player);
                });
                break;
            case "topic1":
                HelpButtonUtils.sendHelpTopic(player, 1);
                break;
            case "topic2":
                HelpButtonUtils.sendHelpTopic(player, 2);
                break;
            case "topic3":
                HelpButtonUtils.sendHelpTopic(player, 3);
                break;
            case "topic4":
                HelpButtonUtils.sendHelpTopic(player, 4);
                break;
            case "topic5":
                HelpButtonUtils.sendHelpTopic(player, 5);
                break;
            default:
                break;
        }
    }
}
