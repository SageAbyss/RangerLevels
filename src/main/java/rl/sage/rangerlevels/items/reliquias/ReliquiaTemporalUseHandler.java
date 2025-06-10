// File: rl/sage/rangerlevels/items/reliquias/ReliquiaTemporalUseHandler.java
package rl.sage.rangerlevels.items.reliquias;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.network.play.server.STitlePacket.Type;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

/**
 * Handler que detecta el uso (click derecho) de cualquier Reliquia Temporal
 * y dispara el repetidor de EXP, mostrando un TITLE/SUBTITLE.
 */
@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReliquiaTemporalUseHandler {

    @SubscribeEvent
    public static void onUseReliquia(RightClickItem event) {
        if (event.getWorld().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        ItemStack held = event.getItemStack();
        String id = RangerItemDefinition.getIdFromStack(held);
        if (id == null) return;

        boolean isReliquia = ReliquiaTemporalComun.ID.equals(id)
                || ReliquiaTemporalRaro.ID.equals(id)
                || ReliquiaTemporalLegendario.ID.equals(id)
                || ReliquiaTemporalEstelar.ID.equals(id);
        if (!isReliquia) return;

        // Cancelar acción por defecto
        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);

        // Obtener última ganancia y bonus
        int last = LevelProvider.get(player).orElseThrow(
                () -> new IllegalStateException("Capability faltante")).getLastGain();
        double bonus = ExpRepeaterHandler.getBonus(player);
        int toGive = (int) Math.round(last * (1.0 + bonus));

        // Repetir EXP
        LevelProvider.giveExpAndNotify(player, toGive);

        // Sonido de activación
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.BELL_RESONATE,
                SoundCategory.MASTER,
                2.5f,
                0.8f
        );
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.BELL_BLOCK,
                SoundCategory.MASTER,
                1.5f,
                0.5f
        );

        // Enviar TITLE
        StringTextComponent title = new StringTextComponent(TextFormatting.DARK_AQUA + "✦ Reliquia Temporal ✦");
        player.connection.send(new STitlePacket(
                Type.TITLE,
                title,
                10, 70, 20
        ));

        // Enviar SUBTITLE con la EXP repetida
        StringTextComponent subtitle = new StringTextComponent(
                TextFormatting.GREEN + "Retrocediste en el tiempo → +" + toGive + " EXP"
        );
        player.connection.send(new STitlePacket(
                Type.SUBTITLE,
                subtitle,
                10, 70, 20
        ));

        // Consumir el ítem si no está en creativo
        if (!player.isCreative()) {
            held.shrink(1);
        }
    }
}
