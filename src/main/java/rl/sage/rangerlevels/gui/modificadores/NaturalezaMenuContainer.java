// File: rl/sage/rangerlevels/gui/modificadores/NaturalezaMenuContainer.java
package rl.sage.rangerlevels.gui.modificadores;

import com.pixelmonmod.pixelmon.api.pokemon.Nature;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.BaseMenuContainer;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.modificadores.ModificadorNaturaleza;
import rl.sage.rangerlevels.items.modificadores.ModificadorNaturalezaUniversal;

public class NaturalezaMenuContainer extends BaseMenuContainer {
    public NaturalezaMenuContainer(int windowId,
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

        boolean isSpecific = ModificadorNaturaleza.ID.equals(heldId);
        boolean isUniversal = ModificadorNaturalezaUniversal.ID.equals(heldId);

        if (!isSpecific && !isUniversal) {
            player.sendMessage(new StringTextComponent("§cNo tienes un Modificador de Naturaleza válido en mano."), player.getUUID());
            player.closeContainer();
            return;
        }

        // Si es específico, debe tener especie asignada
        if (isSpecific && ModificadorNaturaleza.getSpecies(held) == null) {
            player.sendMessage(new StringTextComponent("§cTu Modificador aún no tiene especie asignada."), player.getUUID());
            player.closeContainer();
            return;
        }

        try {
            Nature nat = Nature.valueOf(buttonId);
            String locName = nat.getLocalizedName();

            if (isSpecific) {
                ModificadorNaturaleza.setNature(held, locName);
            } else {
                ModificadorNaturalezaUniversal.setNature(held, locName);
            }

            player.sendMessage(new StringTextComponent("§aNaturaleza seleccionada: §e" + locName), player.getUUID());
        } catch (IllegalArgumentException ignored) {
            // botón no corresponde a una Nature válida
        } finally {
            player.closeContainer();
        }
    }
}
