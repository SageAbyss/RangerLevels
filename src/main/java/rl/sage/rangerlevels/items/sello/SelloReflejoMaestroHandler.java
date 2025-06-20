package rl.sage.rangerlevels.items.sello;

import com.pixelmonmod.pixelmon.init.registry.SoundRegistration;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import rl.sage.rangerlevels.config.ItemsConfig;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class SelloReflejoMaestroHandler {

    private static class BuffData {
        long expireMillis;
        double chance;
    }
    private static final Map<UUID, BuffData> ACTIVE = new ConcurrentHashMap<>();

    /**
     * Detecta uso del ítem Sello Reflejo del Maestro
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem ev) {
        if (ev.getWorld().isClientSide()) return;
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.getPlayer();
        ItemStack stack = ev.getItemStack();
        String id = RangerItemDefinition.getIdFromStack(stack);
        if (id == null) return;

        // Solo si es uno de los sellos:
        boolean isRaro = SelloReflejoMaestroRaro.ID.equals(id);
        boolean isLegend = SelloReflejoMaestroLegendario.ID.equals(id);
        boolean isEstelar = SelloReflejoMaestroEstelar.ID.equals(id);
        if (!isRaro && !isLegend && !isEstelar) return;

        ev.setCanceled(true);
        ev.setCancellationResult(net.minecraft.util.ActionResultType.SUCCESS);

        // Obtener configuración según tier
        ItemsConfig.SelloReflejoConfig cfg = ItemsConfig.get().selloReflejo;
        double chance;
        int durationMin;
        if (isRaro) {
            chance = cfg.chanceRaro;
            durationMin = cfg.durationRaro;
        } else if (isLegend) {
            chance = cfg.chanceLegendario;
            durationMin = cfg.durationLegendario;
        } else {
            chance = cfg.chanceEstelar;
            durationMin = cfg.durationEstelar;
        }
        // Registrar buff
        BuffData data = new BuffData();
        data.chance = chance / 100.0;
        data.expireMillis = System.currentTimeMillis() + durationMin * 60L * 1000L;
        ACTIVE.put(player.getUUID(), data);

        // Consumir el ítem
        Hand hand = ev.getHand();
        stack.shrink(1);
        player.setItemInHand(hand, stack);

        player.sendMessage(new StringTextComponent(
                String.format("§aSello Activado: §echance %.1f%% por %d minutos.", chance, durationMin)
        ), player.getUUID());
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.BEACON_DEACTIVATE,
                SoundCategory.MASTER,
                1.0f, 1.2f
        );
    }

    /**
     * Limpieza de buff expirado en tick
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !(ev.player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.player;
        BuffData data = ACTIVE.get(player.getUUID());
        if (data != null) {
            if (System.currentTimeMillis() >= data.expireMillis) {
                ACTIVE.remove(player.getUUID());
                player.sendMessage(new StringTextComponent("§cEl efecto de Sello Reflejo ha terminado."), player.getUUID());
            }
        }
    }

    /** Chequeo externo en PixelmonEventHandler para duplicar EXP */
    public static double getChanceForPlayer(ServerPlayerEntity player) {
        BuffData data = ACTIVE.get(player.getUUID());
        if (data != null) {
            return data.chance;
        }
        return 0.0;
    }
}
