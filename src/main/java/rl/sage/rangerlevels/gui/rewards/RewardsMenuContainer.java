package rl.sage.rangerlevels.gui.rewards;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.inventory.container.ChestContainer;
import rl.sage.rangerlevels.gui.BaseMenuContainer;
import rl.sage.rangerlevels.gui.MainMenu;
import rl.sage.rangerlevels.gui.MenuSlot;

/**
 * Contenedor para el menú “Recompensas” (3×9). Hereda de BaseMenuContainer,
 * que bloquea toda manipulación y solo permite clic izquierdo normal sobre botones con NBT.
 * Al pulsar:
 *   - “reward_everylevel”: cierra y abre EveryLevelMenu (tick siguiente).
 *   - “reward_package”: cierra y abre PackagesLevelMenu (tick siguiente).
 *   - “reward_exact”: cierra y abre ExactLevelMenu (tick siguiente).
 *   - “back”: cierra y vuelve a MainMenu (tick siguiente).
 */
public class RewardsMenuContainer extends BaseMenuContainer {

    public RewardsMenuContainer(int windowId, PlayerInventory inv, Inventory menuInv) {
        super(windowId, inv, menuInv);
    }

    @Override
    protected void handleButtonClick(String buttonId, ServerPlayerEntity player) {
        switch (buttonId) {
            case "reward_everylevel":
                player.closeContainer();
                player.getServer().execute(() -> {
                    EveryLevelMenu.open(player);
                });
                break;
            case "reward_package":
                player.closeContainer();
                player.getServer().execute(() -> {
                    PackagesLevelMenu.open(player);
                });
                break;
            case "reward_exact":
                player.closeContainer();
                player.getServer().execute(() -> {
                    ExactLevelMenu.open(player);
                });
                break;
            case "back":
                player.closeContainer();
                player.getServer().execute(() -> {
                    MainMenu.open(player);
                });
                break;
            default:
                // Si no coincide ningún ID, no hacemos nada
                break;
        }
    }
}
