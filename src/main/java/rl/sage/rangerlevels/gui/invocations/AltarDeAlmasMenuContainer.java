package rl.sage.rangerlevels.gui.invocations;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import rl.sage.rangerlevels.gui.BaseMenuContainer6;
import java.util.Objects;

public class AltarDeAlmasMenuContainer extends BaseMenuContainer6 {

    public AltarDeAlmasMenuContainer(int windowId,
                                     PlayerInventory playerInv,
                                     Inventory menuInventory) {
        super(windowId, playerInv, menuInventory);
    }

    @Override
    protected void handleButtonClick(String buttonId, ServerPlayerEntity player) {
        switch (buttonId) {
            case "back":
                player.closeContainer();
                Objects.requireNonNull(player.getServer(), "MinecraftServer es nulo").execute(() ->
                        InvocationsMenu.open(player)
                );
                break;

            default:
                break;
        }
    }
}
