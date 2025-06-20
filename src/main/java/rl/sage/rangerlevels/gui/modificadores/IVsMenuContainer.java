// File: rl/sage/rangerlevels/gui/modificadores/IVsMenuContainer.java
package rl.sage.rangerlevels.gui.modificadores;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.BaseMenuContainer;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.modificadores.ModificadorIVs;
import rl.sage.rangerlevels.items.modificadores.ModificadorIVsUniversal;

public class IVsMenuContainer extends BaseMenuContainer {
    public IVsMenuContainer(int windowId,
                            PlayerInventory playerInv,
                            Inventory menuInventory) {
        super(windowId, playerInv, menuInventory);
    }

    @Override
    protected void handleButtonClick(String buttonId, ServerPlayerEntity player) {
        ItemStack held = player.getMainHandItem();
        String heldId = RangerItemDefinition.getIdFromStack(held);

        // cerrar
        if ("close".equals(buttonId)) {
            player.closeContainer();
            return;
        }

        // sólo aceptamos específico o universal
        boolean isSpecific = ModificadorIVs.ID.equals(heldId);
        boolean isUniversal = ModificadorIVsUniversal.ID.equals(heldId);
        if (!isSpecific && !isUniversal) {
            player.sendMessage(new StringTextComponent("§cNo tienes un Modificador IVs válido en mano."), player.getUUID());
            player.closeContainer();
            return;
        }

        // si es específico, aseguramos que tenga species asignada
        if (isSpecific && ModificadorIVs.getSpecies(held) == null) {
            player.sendMessage(new StringTextComponent("§cTu Modificador IVs aún no tiene especie asignada."), player.getUUID());
            player.closeContainer();
            return;
        }

        // --------- manejo de acción/stat ---------
        String action, stat;
        if (isSpecific) {
            action = ModificadorIVs.getAction(held);
            stat   = ModificadorIVs.getStat(held);
        } else {
            action = ModificadorIVsUniversal.getAction(held);
            stat   = ModificadorIVsUniversal.getStat(held);
        }

        // si falta acción → seleccionar acción
        if (action == null) {
            if ("up".equals(buttonId) || "down".equals(buttonId)) {
                if (isSpecific)      ModificadorIVs.setAction(held, buttonId);
                else /*universal*/   ModificadorIVsUniversal.setAction(held, buttonId);
                player.sendMessage(new StringTextComponent(
                        "§aAcción seleccionada: §e" + ("up".equals(buttonId) ? "Subir a 31" : "Bajar a 0")
                ), player.getUUID());
            }
        }
        // si hay acción pero falta stat → seleccionar stat
        else if (stat == null) {
            if (isSpecific)      ModificadorIVs.setStat(held, buttonId);
            else /*universal*/   ModificadorIVsUniversal.setStat(held, buttonId);
            player.sendMessage(new StringTextComponent("§aStat seleccionado: §e" + buttonId), player.getUUID());
        }

        player.closeContainer();
    }
}
