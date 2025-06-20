// File: src/main/java/rl/sage/rangerlevels/items/randoms/MaletinMentorHandler.java
package rl.sage.rangerlevels.items.randoms;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.util.LazyOptional;
import rl.sage.rangerlevels.capability.ILevel;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.limiter.LimiterHelper;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class MaletinMentorHandler {

    @SubscribeEvent
    public static void onEntityInteract(EntityInteractSpecific ev) {
        // Solo servidor y apuntando a otro jugador
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        if (!(ev.getTarget() instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity source = (ServerPlayerEntity) ev.getPlayer();
        ServerPlayerEntity target = (ServerPlayerEntity) ev.getTarget();
        ItemStack stack = ev.getItemStack();

        // Comprueba que sea nuestro Maletín
        if (stack.isEmpty()
                || !MaletinMentor.ID.equals(RangerItemDefinition.getIdFromStack(stack))) {
            return;
        }

        // Cancela la interacción vanilla
        ev.setCanceled(true);
        ev.setCancellationResult(ActionResultType.SUCCESS);

        // 1) Obtén capability ILevel del source
        LazyOptional<ILevel> srcOpt = LevelProvider.get(source);
        if (!srcOpt.isPresent()) {
            source.sendMessage(
                    new StringTextComponent("§cError interno: no pude acceder a tu EXP."),
                    source.getUUID()
            );
            return;
        }

        ILevel srcCap = srcOpt.orElseThrow(IllegalStateException::new);
        int expToTransfer = srcCap.getExp();
        if (expToTransfer <= 0) {
            source.sendMessage(
                    new StringTextComponent("§cNo tienes EXP para transferir."),
                    source.getUUID()
            );
            return;
        }

        // 2) Resetea la EXP del que envía a 0
        srcCap.setExp(0);
        // (Opcional) podrías notificar al cliente con un packet si quieres refrescar UI

        // 3) Otorga la EXP al receptor, respetando tu limitador diario
        LimiterHelper.giveExpWithLimit(target, expToTransfer);

        // 4) Consume el maletín
        stack.shrink(1);

        // 5) Mensajes de confirmación
        source.sendMessage(
                new StringTextComponent(
                        "§aHas transferido §6" + expToTransfer + "§a EXP a " + target.getName().getString() + "."
                ),
                source.getUUID()
        );
        PlayerSoundUtils.playSoundToPlayer(
                source,
                SoundEvents.BEACON_ACTIVATE,
                SoundCategory.MASTER,
                1.0f, 1.0f
        );
        target.sendMessage(
                new StringTextComponent(
                        "§aHas recibido §6" + expToTransfer + "§a EXP de " + source.getName().getString() + "."
                ),
                target.getUUID()
        );
        PlayerSoundUtils.playSoundToPlayer(
                target,
                SoundEvents.BEACON_DEACTIVATE,
                SoundCategory.MASTER,
                1.0f, 1.0f
        );
    }
}
