// File: rl/sage/rangerlevels/items/gemas/GemaExpHandler.java
package rl.sage.rangerlevels.items.gemas;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.IFormattableTextComponent;
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
import rl.sage.rangerlevels.util.PlayerSoundUtils;

/**
 * Handler para las Gemas de Experiencia:
 *   - Gema Común      (ID = "gema_exp_comun")      +10% EXP por 15 minutos
 *   - Gema Épica      (ID = "gema_exp_epico")      +30% EXP por 30 minutos
 *   - Gema Legendaria (ID = "gema_exp_legendario") +50% EXP por 60 minutos
 *
 * Ahora comprueba el RangerID y evita reactivar gemas ya activas.
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
        Tier tier;

        // 2) Determinar cuál gema es según el ID
        switch (id) {
            case ID_GEMA_COMUN:
                bonus      = 0.10;             // +10%
                duracionMs = 15 * 60_000L;     // 15 minutos
                tier       = Tier.COMUN;
                break;
            case ID_GEMA_EPICO:
                bonus      = 0.30;             // +30%
                duracionMs = 30 * 60_000L;     // 30 minutos
                tier       = Tier.EPICO;
                break;
            case ID_GEMA_LEGENDARIO:
                bonus      = 0.50;             // +50%
                duracionMs = 60 * 60_000L;     // 60 minutos
                tier       = Tier.LEGENDARIO;
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

        // 5) Si la gema ya está activa, informamos tiempo restante y cancelamos
        double bonusActual = ExpGemHandler.getBonus(serverPlayer);
        if (bonusActual > 0.0) {
            long remainingMs = ExpGemHandler.getRemainingDurationMs(serverPlayer);
            long minutos = (remainingMs + 59_999L) / 60_000L; // redondeo hacia arriba
            String msg = TextFormatting.RED
                    + "Ya tienes una Gema de Experiencia "
                    + TextFormatting.GOLD + tier.getDisplayName() + TextFormatting.RED
                    + " activa. Tiempo restante: "
                    + TextFormatting.AQUA + minutos + "min.";
            serverPlayer.sendMessage(new StringTextComponent(msg), serverPlayer.getUUID());
            // Sonido de “error” o “no permitido”
            PlayerSoundUtils.playSoundToPlayer(
                    serverPlayer,
                    SoundEvents.UI_BUTTON_CLICK, // clic de botón como “no permitido”
                    SoundCategory.PLAYERS,
                    0.7f, 1.0f
            );
            return;
        }

        // 6) Activar la gema en ExpGemHandler
        ExpGemHandler.activarGem(serverPlayer, bonus, duracionMs);

        // 7) Consumir la gema (si no está en creativo)
        if (!serverPlayer.isCreative()) {
            held.shrink(1);
        }

        // 8) Reproducir sonido de poción al activarse
        PlayerSoundUtils.playSoundToPlayer(
                serverPlayer,
                SoundEvents.GENERIC_DRINK, // sonido de beber poción
                SoundCategory.PLAYERS,
                1.0f, 1.0f
        );

        // 9) Notificación al jugador con mensaje decorativo y “Activado” usando degradado
        //    9.1) Construir título: "✦ Gema de Experiencia <Tier> ᴀᴄᴛɪᴠᴀᴅᴏ ✦"
        IFormattableTextComponent title = new StringTextComponent("")
                .append(new StringTextComponent("✦ ").withStyle(TextFormatting.DARK_PURPLE))
                .append(new StringTextComponent("Gema de Experiencia ").withStyle(TextFormatting.WHITE))
                // Insertamos el nombre del Tier en degradado pastel:
                .append(tier.getColor())
                .append(new StringTextComponent(" ᴀᴄᴛɪᴠᴀᴅᴏ").withStyle(TextFormatting.WHITE))
                .append(new StringTextComponent(" ✦").withStyle(TextFormatting.DARK_PURPLE));

        //    9.2) Crear líneas de bonus y duración como texto normal
        IFormattableTextComponent lineBonus = new StringTextComponent("")
                .append(new StringTextComponent("❖ Bonus de EXP: ").withStyle(TextFormatting.GREEN))
                .append(new StringTextComponent("+" + (int)(bonus * 100) + "%").withStyle(TextFormatting.GOLD));

        IFormattableTextComponent lineDuracion = new StringTextComponent("")
                .append(new StringTextComponent("❖ Duración: ").withStyle(TextFormatting.GREEN))
                .append(new StringTextComponent((duracionMs / 60_000L) + "min").withStyle(TextFormatting.AQUA));

        //    9.3) Enviar mensaje en tres líneas
        serverPlayer.sendMessage(title, serverPlayer.getUUID());
        serverPlayer.sendMessage(lineBonus, serverPlayer.getUUID());
        serverPlayer.sendMessage(lineDuracion, serverPlayer.getUUID());
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
            // Cancelar cualquier interacción sobre bloque si es una gema
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.util.ActionResultType.SUCCESS);
        }
    }
}
