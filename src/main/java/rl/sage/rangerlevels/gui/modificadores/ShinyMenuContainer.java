// File: rl/sage/rangerlevels/gui/modificadores/ShinyMenuContainer.java
package rl.sage.rangerlevels.gui.modificadores;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.BaseMenuContainer;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.modificadores.ModificadorShiny;
import rl.sage.rangerlevels.items.modificadores.ModificadorShinyUniversal;

public class ShinyMenuContainer extends BaseMenuContainer {
    public ShinyMenuContainer(int windowId,
                              PlayerInventory playerInv,
                              Inventory menuInventory) {
        super(windowId, playerInv, menuInventory);
    }

    @Override
    protected void handleButtonClick(String buttonId, ServerPlayerEntity player) {
        if ("close".equals(buttonId)) {
            player.closeContainer();
            return;
        }

        ItemStack held = player.getMainHandItem();
        String heldId = RangerItemDefinition.getIdFromStack(held);

        boolean isSpecific  = ModificadorShiny.ID.equals(heldId);
        boolean isUniversal = ModificadorShinyUniversal.ID.equals(heldId);

        if (!isSpecific && !isUniversal) {
            player.sendMessage(new StringTextComponent("§cNo tienes un Modificador Shiny válido en mano."), player.getUUID());
            player.closeContainer();
            return;
        }

        // Para la versión específica, debe tener especie asignada
        if (isSpecific && ModificadorShiny.getSpecies(held) == null) {
            player.sendMessage(new StringTextComponent("§cTu Modificador aún no tiene especie asignada."), player.getUUID());
            player.closeContainer();
            return;
        }

        try {
            boolean shiny = "shiny".equals(buttonId);
            if (isSpecific) {
                ModificadorShiny.setShiny(held, shiny);
            } else {
                ModificadorShinyUniversal.setShiny(held, shiny);
            }
            player.sendMessage(new StringTextComponent("§aShiny: §e" + (shiny ? "Sí" : "No")), player.getUUID());
        } catch (Exception ignored) {
            // no action
        } finally {
            player.closeContainer();
        }
    }
}
