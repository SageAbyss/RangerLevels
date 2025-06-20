// File: rl/sage/rangerlevels/gui/modificadores/TamanoMenuContainer.java
package rl.sage.rangerlevels.gui.modificadores;

import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.BaseMenuContainer;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.modificadores.ModificadorTamano;
import rl.sage.rangerlevels.items.modificadores.ModificadorTamanoUniversal;

public class TamanoMenuContainer extends BaseMenuContainer {
    public TamanoMenuContainer(int windowId,
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

        boolean isSpecific = ModificadorTamano.ID.equals(heldId);
        boolean isUniversal = ModificadorTamanoUniversal.ID.equals(heldId);

        if (!isSpecific && !isUniversal) {
            player.sendMessage(new StringTextComponent("§cNo tienes un Modificador de Tamaño en mano."), player.getUUID());
            player.closeContainer();
            return;
        }

        // Si es específico, validar que tenga especie asignada
        if (isSpecific && ModificadorTamano.getSpecies(held) == null) {
            player.sendMessage(new StringTextComponent("§cTu Modificador aún no tiene especie asignada."), player.getUUID());
            player.closeContainer();
            return;
        }

        try {
            EnumGrowth growth = EnumGrowth.valueOf(buttonId);
            String locName = growth.getLocalizedName();
            if (isSpecific) {
                ModificadorTamano.setSize(held, locName);
            } else {
                ModificadorTamanoUniversal.setSize(held, locName);
            }
            player.sendMessage(new StringTextComponent("§aTamaño seleccionado: §e" + locName), player.getUUID());
        } catch (IllegalArgumentException e) {
            // botón no corresponde a un EnumGrowth válido
        } finally {
            player.closeContainer();
        }
    }
}
