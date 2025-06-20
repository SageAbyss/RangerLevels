// File: rl/sage/rangerlevels/gui/altar/InvocationSelectionContainer.java
package rl.sage.rangerlevels.gui.altar;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.BaseMenuContainer;
import rl.sage.rangerlevels.items.altar.AltarRecipe;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.altar.AltarAlmasUseHandler;
import rl.sage.rangerlevels.items.altar.AltarAlmas;

import java.util.List;
import java.util.UUID;

public class InvocationSelectionContainer extends BaseMenuContainer {
    private final List<AltarRecipe> matches;

    public InvocationSelectionContainer(int windowId,
                                        PlayerInventory playerInv,
                                        Inventory menuInventory,
                                        List<AltarRecipe> matches) {
        super(windowId, playerInv, menuInventory);
        this.matches = matches;
    }

    @Override
    protected void handleButtonClick(String buttonId, ServerPlayerEntity player) {
        UUID uuid = player.getUUID();

        if ("cancel".equals(buttonId)) {
            player.closeContainer();
            // Limpiar contexto pendiente:
            AltarAlmasUseHandler.clearPending(uuid);
            return;
        }
        // Buscar la receta correspondiente:
        AltarRecipe chosen = null;
        for (AltarRecipe r : matches) {
            if (r.getId().getPath().equals(buttonId)) {
                chosen = r;
                break;
            }
        }
        if (chosen == null) {
            // No coincide; ignorar o mensaje:
            player.sendMessage(new StringTextComponent("§cOpción inválida."), uuid);
            player.closeContainer();
            AltarAlmasUseHandler.clearPending(uuid);
            return;
        }

        // Recuperar contexto pendiente:
        AltarAlmasUseHandler.PendingInvocationContext ctx = AltarAlmasUseHandler.getPendingContext(uuid);
        if (ctx == null) {
            player.sendMessage(new StringTextComponent("§cContexto de invocación no encontrado."), uuid);
            player.closeContainer();
            return;
        }

        // Revalidar cooldown:
        long currentTick = ((net.minecraft.world.server.ServerWorld)ctx.world).getGameTime();
        Long nextAllowed = AltarAlmasUseHandler.getCooldown(uuid);
        if (nextAllowed != null && currentTick < nextAllowed) {
            long remainingTicks = nextAllowed - currentTick;
            long seconds = (remainingTicks + 19) / 20;
            player.sendMessage(new StringTextComponent("§cDebes esperar " + seconds + "s para volver a usar el Altar de Almas."), uuid);
            player.closeContainer();
            AltarAlmasUseHandler.clearPending(uuid);
            AltarAlmasUseHandler.syncSlot(player, ctx.hand);
            return;
        }

        // Revalidar presencia de altar en mano:
        ItemStack altarStack = player.getItemInHand(ctx.hand);
        String rid = altarStack.hasTag() && altarStack.getTag().contains(RangerItemDefinition.NBT_ID_KEY)
                ? altarStack.getTag().getString(RangerItemDefinition.NBT_ID_KEY)
                : null;
        if (!AltarAlmas.ID.equals(rid)) {
            player.sendMessage(new StringTextComponent("§cNo tienes el Altar de Almas en la mano."), uuid);
            player.closeContainer();
            AltarAlmasUseHandler.clearPending(uuid);
            return;
        }
        // Revalidar presencia de esencia válida:
        ItemStack essence = AltarAlmasUseHandler.findBoundEssence(player);
        if (essence == null) {
            player.sendMessage(new StringTextComponent("§cNo tienes una Esencia ligada válida en inventario."), uuid);
            player.closeContainer();
            AltarAlmasUseHandler.clearPending(uuid);
            return;
        }
        // Revalidar que recipe siga válida:
        if (!chosen.matches(player)) {
            player.sendMessage(new StringTextComponent("§cYa no cumples los requisitos para esta invocación."), uuid);
            player.closeContainer();
            AltarAlmasUseHandler.clearPending(uuid);
            return;
        }

        AltarAlmasUseHandler.consumeIngredientsAndAltar(player, ctx.hand, chosen);

        // Ejecutar invocación:
        AltarAlmasUseHandler.executeInvocation(player, chosen, ctx.species, ctx.storedId, ctx.center);

        // Registrar cooldown:
        AltarAlmasUseHandler.putCooldown(uuid, currentTick + 200);

        // Cerrar menú y limpiar contexto:
        player.closeContainer();
        AltarAlmasUseHandler.clearPending(uuid);
    }
}
