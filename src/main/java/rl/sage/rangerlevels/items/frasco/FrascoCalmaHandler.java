package rl.sage.rangerlevels.items.frasco;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.UUID;

/**
 * Manejador para los Frascos de Calma:
 *  - Persistencia del porcentaje y expiración en NBT de jugador (player.getPersistentData()).
 *  - NBT Keys bajo: "RangerCalma" → sub-tag con "Calma_Pct" y "Calma_ExpiresAt".
 *  - Evita reactivar si ya tiene uno activo, informa tiempo restante (en minutos sin símbolo de “±”).
 */
@Mod.EventBusSubscriber(modid = RangerLevels.MODID)
public class FrascoCalmaHandler {

    private static final String NBT_KEY          = "RangerCalma";
    private static final String NBT_PCT          = "Calma_Pct";        // double
    private static final String NBT_EXPIRES_AT   = "Calma_ExpiresAt";  // long

    /**
     * Devuelve el porcentaje activo para el jugador (0.0 si no hay ninguno o expiró).
     * Si expiró, limpia NBT y devuelve 0.
     */
    public static double getReduccionActivo(ServerPlayerEntity player) {
        CompoundNBT persist = player.getPersistentData();
        if (!persist.contains(NBT_KEY)) return 0.0;

        CompoundNBT calmaTag = persist.getCompound(NBT_KEY);
        if (!calmaTag.contains(NBT_PCT) || !calmaTag.contains(NBT_EXPIRES_AT)) {
            persist.remove(NBT_KEY);
            return 0.0;
        }

        long expiresAt = calmaTag.getLong(NBT_EXPIRES_AT);
        long now = System.currentTimeMillis();
        if (now >= expiresAt) {
            // expiró → limpieza
            persist.remove(NBT_KEY);
            return 0.0;
        }

        return calmaTag.getDouble(NBT_PCT);
    }

    /**
     * Al activar un frasco, se guarda en NBT del jugador:
     *  - porcentaje  (double)
     *  - expiresAt   (long)
     */
    private static void activarFrascoEnNBT(ServerPlayerEntity player, double porcentaje, long duracionMs) {
        long now = System.currentTimeMillis();
        long expiresAt = now + duracionMs;

        CompoundNBT persist = player.getPersistentData();
        CompoundNBT calmaTag = new CompoundNBT();
        calmaTag.putDouble(NBT_PCT, porcentaje);
        calmaTag.putLong(NBT_EXPIRES_AT, expiresAt);
        persist.put(NBT_KEY, calmaTag);

        // (Opcional) sincronizar NBT con cliente si lo necesitas
    }

    /**
     * Listener para RightClickItem. Si el ítem en mano es un Frasco de Calma,
     * activa el efecto (si no hay uno vigente) o informa tiempo restante.
     */
    @SubscribeEvent
    public static void onRightClickFrasco(PlayerInteractEvent.RightClickItem event) {
        if (event.getWorld().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        ItemStack held = player.getItemInHand(event.getHand());
        if (held == null || held.isEmpty()) return;

        // 1) Obtener ID NBT “RangerID”
        String id = RangerItemDefinition.getIdFromStack(held);
        if (id == null) return;

        double porcentaje;
        long duracionMs;
        String nombreTier;

        // 2) Determinar qué Frasco es según el ID
        switch (id) {
            case FrascoCalmaRaro.ID:
                porcentaje = FrascoCalmaRaro.getReduccion();    // 0.15
                duracionMs = FrascoCalmaRaro.getDuracionMs();   // 15 min
                nombreTier = "Raro";
                break;
            case FrascoCalmaEpico.ID:
                porcentaje = FrascoCalmaEpico.getReduccion();  // 0.30
                duracionMs = FrascoCalmaEpico.getDuracionMs();// 30 min
                nombreTier = "Épico";
                break;
            case FrascoCalmaEstelar.ID:
                porcentaje = FrascoCalmaEstelar.getReduccion(); // 0.50
                duracionMs = FrascoCalmaEstelar.getDuracionMs(); // 60 min
                nombreTier = "Estelar";
                break;
            default:
                return;
        }

        // 3) Cancelar uso vanilla
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.util.ActionResultType.SUCCESS);

        // 4) Verificar si ya hay frasco activo
        double existingPct = getReduccionActivo(player);
        if (existingPct > 0.0) {
            long remainingMs = getRemainingDurationMs(player);
            long minutos = (remainingMs + 59_999L) / 60_000L; // redondeo hacia arriba sin símbolo
            String txt = TextFormatting.RED
                    + "Ya tienes un Frasco de Calma " + TextFormatting.BLUE + nombreTier + TextFormatting.RED
                    + " activo. Tiempo restante: " + TextFormatting.AQUA + minutos + " min";
            player.sendMessage(new StringTextComponent(txt), player.getUUID());

            // Sonido de error (no se pudo activar porque ya hay uno)
            PlayerSoundUtils.playSoundToPlayer(
                    player,
                    SoundEvents.UI_BUTTON_CLICK,
                    SoundCategory.PLAYERS,
                    0.7f, 1.0f
            );
            return;
        }

        // 5) Activar y guardar en NBT
        activarFrascoEnNBT(player, porcentaje, duracionMs);

        // 6) Consumir el ítem (si no está en creativo)
        if (!player.isCreative()) {
            held.shrink(1);
        }

        // 7) Reproducir sonido de pócima
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.GENERIC_DRINK,
                SoundCategory.PLAYERS,
                1.0f, 0.5f
        );

        // 8) Mensaje decorativo de “ᴀᴄᴛɪᴠᴀᴅᴏ”
        TextFormatting itemColor;
        switch (nombreTier) {
            case "Raro":    itemColor = TextFormatting.BLUE;       break;
            case "Épico":   itemColor = TextFormatting.AQUA;       break;
            case "Estelar": itemColor = TextFormatting.DARK_AQUA;  break;
            default:        itemColor = TextFormatting.WHITE;      break;
        }

        String titulo = TextFormatting.DARK_AQUA
                + "✦ " + itemColor + "Frasco de Calma " + nombreTier + " ᴀᴄᴛɪᴠᴀᴅᴏ" + TextFormatting.DARK_AQUA + " ✦";
        String linea1 = TextFormatting.GREEN
                + "❖ Reducción: " + TextFormatting.GOLD + "–" + (int) (porcentaje * 100) + "%";
        String linea2 = TextFormatting.GREEN
                + "❖ Duración: " + TextFormatting.AQUA + (duracionMs / 60_000L) + " min";

        String mensaje = titulo + "\n" + linea1 + "\n" + linea2;
        player.sendMessage(new StringTextComponent(mensaje), player.getUUID());
    }

    /**
     * Devuelve el tiempo restante en ms del frasco activo (0 si no hay).
     * Si expiró, limpia el NBT.
     */
    private static long getRemainingDurationMs(ServerPlayerEntity player) {
        CompoundNBT persist = player.getPersistentData();
        if (!persist.contains(NBT_KEY)) return 0L;

        CompoundNBT calmaTag = persist.getCompound(NBT_KEY);
        if (!calmaTag.contains(NBT_EXPIRES_AT)) {
            persist.remove(NBT_KEY);
            return 0L;
        }

        long expiresAt = calmaTag.getLong(NBT_EXPIRES_AT);
        long now = System.currentTimeMillis();
        long remaining = expiresAt - now;
        if (remaining <= 0L) {
            persist.remove(NBT_KEY);
            return 0L;
        }
        return remaining;
    }
}
