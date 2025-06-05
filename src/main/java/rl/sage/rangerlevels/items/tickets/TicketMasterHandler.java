package rl.sage.rangerlevels.items.tickets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.IPassCapability;
import rl.sage.rangerlevels.capability.PassCapabilities;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.pass.PassType;
import rl.sage.rangerlevels.pass.PassUtil;

import java.time.Duration;

/**
 * Listener para “Ticket Master” (ID = "ticket_master"):
 *  1) Comprueba que el ItemStack tenga NBT “RangerID” = "ticket_master".
 *  2) Restaura el pase anterior si expiró y notifica.
 *  3) Si no tiene Master, lo otorga por 1 día; si ya tiene, muestra tiempo restante.
 *  4) Cancela cualquier RightClickBlock si el ID coincide.
 */
@Mod.EventBusSubscriber(modid = RangerLevels.MODID)
public class TicketMasterHandler {

    private static final long MASTER_DURATION_MS = Duration.ofDays(1).toMillis();
    private static final String ID_TICKET_MASTER = "ticket_master";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        PlayerEntity player = event.getPlayer();
        World world = event.getWorld();
        Hand hand = event.getHand();

        ItemStack held = player.getItemInHand(hand);
        if (held == null || held.isEmpty()) return;

        // 1) Verificar ID NBT
        String id = RangerItemDefinition.getIdFromStack(held);
        if (!ID_TICKET_MASTER.equals(id)) return;

        // ── BLOQUEAR USO VANILLA ──
        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);

        // ── LÓGICA SOLO EN SERVIDOR ──
        if (world.isClientSide) return;
        if (!(player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        // 2) Verificar/restaurar pase expirado y notificar
        PassType actual = PassUtil.checkAndRestorePass(serverPlayer);

        // 3) Si ya tiene Master (o mayor), mostrar tiempo restante
        if (actual.getTier() >= PassType.MASTER.getTier()) {
            IPassCapability cap = PassCapabilities.get(serverPlayer);
            long expiresAt = cap.getExpiresAt();
            String remainingTime = "Desconocido";
            if (expiresAt != Long.MAX_VALUE) {
                long millisLeft = expiresAt - System.currentTimeMillis();
                if (millisLeft > 0) {
                    Duration duration = Duration.ofMillis(millisLeft);
                    long hours = duration.toHours();
                    long minutes = duration.minusHours(hours).toMinutes();
                    remainingTime = hours + "h " + minutes + "m";
                } else {
                    remainingTime = "Expirado";
                }
            }

            StringTextComponent titulo = new StringTextComponent(TextFormatting.DARK_PURPLE + "✦ Pase Ranger ✦");
            IFormattableTextComponent linea1 = new StringTextComponent(TextFormatting.RED + "❖ Ya tienes el pase ")
                    .append(actual.getGradientDisplayName())
                    .append(new StringTextComponent(TextFormatting.RED + " activo."));
            IFormattableTextComponent linea2 = new StringTextComponent(TextFormatting.GRAY + "❖ Tiempo restante: ")
                    .append(new StringTextComponent(TextFormatting.YELLOW + remainingTime));
            serverPlayer.sendMessage(titulo.copy(), serverPlayer.getUUID());
            serverPlayer.sendMessage(linea1, serverPlayer.getUUID());
            serverPlayer.sendMessage(linea2, serverPlayer.getUUID());
            return;
        }

        // 4) Otorgar Master Pass (1 día), guardando el pase anterior
        IPassCapability cap = PassCapabilities.get(serverPlayer);
        cap.grantPass(PassType.MASTER.getTier(), MASTER_DURATION_MS);
        cap.syncToClient(serverPlayer);

        // 5) Notificar fecha de expiración en chat
        long expiresAt = cap.getExpiresAt();
        String expirationTime;
        if (expiresAt == Long.MAX_VALUE) {
            expirationTime = "Nunca";
        } else {
            expirationTime = java.time.format.DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(java.time.ZoneOffset.UTC)
                    .format(java.time.Instant.ofEpochMilli(expiresAt))
                    + " UTC";
        }

        StringTextComponent titulo = new StringTextComponent(TextFormatting.DARK_PURPLE + "✦ Pase Ranger ✦");
        IFormattableTextComponent linea1 = new StringTextComponent(TextFormatting.GREEN + "❖ ᴀᴄᴛɪᴠᴀᴅᴏ ")
                .append(PassType.MASTER.getGradientDisplayName())
                .append(new StringTextComponent(TextFormatting.GREEN + " por 1 día!"));
        IFormattableTextComponent linea2 = new StringTextComponent(TextFormatting.GRAY + "❖ Expira: ")
                .append(new StringTextComponent(TextFormatting.YELLOW + expirationTime));
        serverPlayer.sendMessage(titulo.copy(), serverPlayer.getUUID());
        serverPlayer.sendMessage(linea1, serverPlayer.getUUID());
        serverPlayer.sendMessage(linea2, serverPlayer.getUUID());

        // 6) Enviar título y subtítulo
        ITextComponent titleText = new StringTextComponent("Pase Ranger").withStyle(TextFormatting.GOLD);
        ITextComponent subTitleText = new StringTextComponent("ᴀᴄᴛɪᴠᴀᴅᴏ ")
                .withStyle(TextFormatting.AQUA)
                .append(PassType.MASTER.getGradientDisplayName());
        STitlePacket packetTitle = new STitlePacket(STitlePacket.Type.TITLE, titleText);
        STitlePacket packetSub = new STitlePacket(STitlePacket.Type.SUBTITLE, subTitleText);
        serverPlayer.connection.send(packetTitle);
        serverPlayer.connection.send(packetSub);

        // 7) Consumir el ticket (si no está en creativo)
        if (!serverPlayer.isCreative()) {
            held.shrink(1);
        }

        // 8) Reproducir sonido de confirmación
        serverPlayer.level.playSound(
                null,
                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.IRON_DOOR_OPEN,
                SoundCategory.MASTER,
                1.0f, 0.5f
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        ItemStack held = player.getItemInHand(event.getHand());
        if (held == null || held.isEmpty()) return;

        String id = RangerItemDefinition.getIdFromStack(held);
        if (ID_TICKET_MASTER.equals(id)) {
            event.setCanceled(true);
            event.setCancellationResult(ActionResultType.SUCCESS);
        }
    }
}
