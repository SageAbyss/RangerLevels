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
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.pass.PassType;

import java.time.Duration;

/**
 * Listener para “Ticket Master” (ID = "ticket_master"):
 *  1) Comprueba que el ItemStack tenga NBT “RangerID” = "ticket_master".
 *  2) Otorga el pase MASTER si no está activo, envía mensajes/título, consume el ítem.
 *  3) Cancela cualquier RightClickBlock si el ID coincide.
 */
@Mod.EventBusSubscriber(modid = RangerLevels.MODID)
public class TicketMasterHandler {

    // Duración del pase MASTER: 1 día
    private static final long MASTER_DURATION_MS = Duration.ofDays(1).toMillis();
    private static final String ID_TICKET_MASTER = "ticket_master";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        PlayerEntity player = event.getPlayer();
        World world = event.getWorld();
        Hand hand = event.getHand();

        ItemStack held = player.getItemInHand(hand);
        if (held == null || held.isEmpty()) {
            return;
        }

        // 1) Verificar el ID NBT “ticket_master”
        String id = RangerItemDefinition.getIdFromStack(held);
        if (!ID_TICKET_MASTER.equals(id)) {
            return;
        }

        // ── BLOQUEAR USO VANILLA ──
        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);

        // ── LÓGICA SOLO EN SERVIDOR ──
        if (world.isClientSide) {
            return;
        }
        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        // 2) Chequear pase actual
        PassType actual = PassManager.getCurrentPass(serverPlayer);
        if (actual.getTier() >= PassType.MASTER.getTier()) {
            serverPlayer.sendMessage(
                    new StringTextComponent(
                            TextFormatting.RED +
                                    "Ya tienes el pase “" + actual + "” activo §7(igual o mayor)§r."
                    ),
                    serverPlayer.getUUID()
            );
            return;
        }

        // 3) Otorgar Master Pass (1 día)
        IPassCapability cap = PassCapabilities.get(serverPlayer);
        cap.grantPass(PassType.MASTER.getTier(), MASTER_DURATION_MS);
        cap.syncToClient(serverPlayer);

        // 4) Notificar fecha de expiración en chat
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
        IFormattableTextComponent msg = new StringTextComponent("")
                .append(new StringTextComponent("¡Has activado el pase ").withStyle(TextFormatting.GREEN))
                .append(PassType.MASTER.getGradientDisplayName())
                .append(new StringTextComponent(" por 1 día! ").withStyle(TextFormatting.GREEN))
                .append(new StringTextComponent("Expira: ").withStyle(TextFormatting.GRAY))
                .append(new StringTextComponent(expirationTime).withStyle(TextFormatting.YELLOW));

        serverPlayer.sendMessage(msg, serverPlayer.getUUID());

        // 5) Enviar título y subtítulo
        ITextComponent titleText = new StringTextComponent("Pase Ranger").withStyle(TextFormatting.GOLD);
        ITextComponent subTitleText = new StringTextComponent("Ahora tienes el ")
                .withStyle(TextFormatting.AQUA)
                .append(PassType.MASTER.getGradientDisplayName());
        STitlePacket packetTitle = new STitlePacket(STitlePacket.Type.TITLE, titleText);
        STitlePacket packetSub = new STitlePacket(STitlePacket.Type.SUBTITLE, subTitleText);
        serverPlayer.connection.send(packetTitle);
        serverPlayer.connection.send(packetSub);

        // 6) Consumir el ticket (si no está en creativo)
        if (!serverPlayer.isCreative()) {
            held.shrink(1);
        }

        // 7) Reproducir sonido de confirmación
        serverPlayer.level.playSound(
                null,
                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.NOTE_BLOCK_CHIME,
                SoundCategory.MASTER,
                1.0f, 0.8f
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        ItemStack held = player.getItemInHand(event.getHand());
        if (held == null || held.isEmpty()) {
            return;
        }

        // 8) Cancelamos la interacción con bloques solo si es “ticket_master”
        String id = RangerItemDefinition.getIdFromStack(held);
        if (ID_TICKET_MASTER.equals(id)) {
            event.setCanceled(true);
            event.setCancellationResult(ActionResultType.SUCCESS);
        }
    }
}
