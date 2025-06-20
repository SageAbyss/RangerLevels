// File: rl/sage/rangerlevels/items/randoms/DestinoVinculadoHandler.java
package rl.sage.rangerlevels.items.randoms;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.multiplier.MultiplierManager;
import rl.sage.rangerlevels.util.PlayerSoundUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class DestinoVinculadoHandler {

    @SubscribeEvent
    public static void onEntityInteract(EntityInteractSpecific ev) {
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        if (!(ev.getTarget() instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity source = (ServerPlayerEntity) ev.getPlayer();
        ServerPlayerEntity target = (ServerPlayerEntity) ev.getTarget();
        ItemStack stack = ev.getItemStack();

        // Verificar si es uno de los Destino Vinculado
        String id = RangerItemDefinition.getIdFromStack(stack);
        if (id == null) return;

        double bonus;
        long durationTicks;
        String tierName;
        switch (id) {
            case DestinoVinculadoRaro.ID:
                bonus = 0.2;
                durationTicks = 30L * 60L * 20L; // 30 min * 60 s * 20 ticks
                tierName = "Raro";
                break;
            case DestinoVinculadoEpico.ID:
                bonus = 0.5;
                durationTicks = 60L * 60L * 20L; // 1 h
                tierName = "Épico";
                break;
            case DestinoVinculadoLegendario.ID:
                bonus = 1.0;
                durationTicks = 3L * 60L * 60L * 20L; // 3 h
                tierName = "Legendario";
                break;
            default:
                return;
        }

        // Cancelar la interacción vanilla
        ev.setCanceled(true);
        ev.setCancellationResult(ActionResultType.SUCCESS);

        // Si la interacción es sobre uno mismo, ignorar o notificar
        if (source.getUUID().equals(target.getUUID())) {
            source.sendMessage(new StringTextComponent("§cNo puedes usar Destino Vinculado sobre ti mismo."), source.getUUID());
            return;
        }

        // Convertir ticks a segundos (MultiplierManager usa segundos)
        long durationSeconds = durationTicks / 20L;

        MultiplierManager mgr = MultiplierManager.instance();

        String srcName = source.getName().getString();
        String tgtName = target.getName().getString();

        double value = 1.0 + bonus; // p.e. 1.2, 1.5 o 2.0

        // Aplicar a ambos: establecemos el mismo valor y expiración
        mgr.setPlayer(srcName, value, durationSeconds);
        mgr.setPlayer(tgtName, value, durationSeconds);

        // Consumir el ítem
        stack.shrink(1);

        // Mensajes de confirmación
        StringTextComponent msgSource = new StringTextComponent(
                "§aHas vinculado tu EXP con " + tgtName +
                        ". Multiplicador privado: §e+" + (int)(bonus*100) + "%§a por " +
                        formatMinutes(durationSeconds) + "."
        );
        source.sendMessage(msgSource, source.getUUID());
        PlayerSoundUtils.playSoundToPlayer(source, SoundEvents.BEACON_ACTIVATE, SoundCategory.MASTER, 1.0f, 1.0f);

        StringTextComponent msgTarget = new StringTextComponent(
                "§aHas sido vinculado con " + srcName +
                        ". Multiplicador privado: §e+" + (int)(bonus*100) + "%§a por " +
                        formatMinutes(durationSeconds) + "."
        );
        target.sendMessage(msgTarget, target.getUUID());
        PlayerSoundUtils.playSoundToPlayer(target, SoundEvents.BEACON_DEACTIVATE, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    /** Formatea duración en minutos/hora para mostrar en texto, a partir de segundos. */
    private static String formatMinutes(long seconds) {
        if (seconds % 3600 == 0) {
            long hours = seconds / 3600;
            return hours + " hora" + (hours > 1 ? "s" : "");
        } else if (seconds % 60 == 0) {
            long mins = seconds / 60;
            return mins + " minuto" + (mins > 1 ? "s" : "");
        } else {
            // caso raro, mostrar segundos
            return seconds + " segundos";
        }
    }
}
