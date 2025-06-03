package rl.sage.rangerlevels.items.gemas;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;

/**
 * Handler para las Gemas de Experiencia:
 *   - Gema Común   (ID = "gema_exp_comun")   +10% EXP por 15 minutos
 *   - Gema Épica   (ID = "gema_exp_epico")   +30% EXP por 30 minutos
 *   - Gema Legendaria (ID = "gema_exp_legendario") +50% EXP por 60 minutos
 *
 * Ahora comprueba el RangerID en lugar del Tier o el material.
 */
@Mod.EventBusSubscriber(modid = RangerLevels.MODID)
public class GemaExpHandler {

    private static final String ID_GEMA_COMUN      = "gema_exp_comun";
    private static final String ID_GEMA_EPICO      = "gema_exp_epico";
    private static final String ID_GEMA_LEGENDARIO = "gema_exp_legendario";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickGem(PlayerInteractEvent.RightClickItem event) {
        PlayerEntity player = event.getPlayer();
        World world = event.getWorld();
        Hand hand = event.getHand();

        ItemStack held = player.getItemInHand(hand);
        if (held == null || held.isEmpty()) return;

        // 1) Obtener el ID NBT “RangerID”
        String id = RangerItemDefinition.getIdFromStack(held);
        if (id == null) return;

        double bonus;
        long duracionMs;
        String nombreTier;

        // 2) Determinar cuál gema es según el ID
        switch (id) {
            case ID_GEMA_COMUN:
                bonus      = 0.10;             // +10%
                duracionMs = 15 * 60_000L;     // 15 minutos
                nombreTier = "Común";
                break;
            case ID_GEMA_EPICO:
                bonus      = 0.30;             // +30%
                duracionMs = 30 * 60_000L;     // 30 minutos
                nombreTier = "Épica";
                break;
            case ID_GEMA_LEGENDARIO:
                bonus      = 0.50;             // +50%
                duracionMs = 60 * 60_000L;     // 60 minutos
                nombreTier = "Legendaria";
                break;
            default:
                // No es ninguna de nuestras gemas
                return;
        }

        // 3) Cancelar uso vanilla
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.util.ActionResultType.SUCCESS);

        // 4) Lógica solo en servidor
        if (world.isClientSide) return;
        if (!(player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        // 5) Activar la gema en ExpGemHandler
        ExpGemHandler.activarGem(serverPlayer, bonus, duracionMs);

        // 6) Consumir la gema (si no está en creativo)
        if (!serverPlayer.isCreative()) {
            held.shrink(1);
        }

        // 7) Notificación al jugador
        String texto = TextFormatting.GREEN
                + "Has activado una Gema de Experiencia " + nombreTier
                + TextFormatting.GREEN + " (+" + (int) (bonus * 100) + "% EXP) "
                + TextFormatting.GRAY + "durante "
                + (duracionMs / 60_000L) + " minutos.";
        serverPlayer.sendMessage(new StringTextComponent(texto), serverPlayer.getUUID());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickGemBlock(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        ItemStack held = player.getItemInHand(event.getHand());
        if (held == null || held.isEmpty()) return;

        String id = RangerItemDefinition.getIdFromStack(held);
        if (ID_GEMA_COMUN.equals(id) ||
                ID_GEMA_EPICO.equals(id) ||
                ID_GEMA_LEGENDARIO.equals(id)) {
            // Cancelar cualquier interacción sobre bloque si es una de nuestras gemas
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.util.ActionResultType.SUCCESS);
        }
    }
}
