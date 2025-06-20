// src/main/java/rl/sage/rangerlevels/items/tickets/TicketUltraHandler.java
package rl.sage.rangerlevels.items.tickets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
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
import net.minecraftforge.common.util.LazyOptional;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.IPassCapability;
import rl.sage.rangerlevels.capability.PassCapabilities;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.pass.PassType;
import rl.sage.rangerlevels.pass.PassUtil;

import java.time.Duration;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID)
public class TicketUltraHandler {

    private static final long ULTRA_DURATION_MS = Duration.ofDays(1).toMillis();
    private static final String ID_TICKET_ULTRA = "ticket_ultra";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        PlayerEntity player = event.getPlayer();
        World world = event.getWorld();
        Hand hand = event.getHand();

        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) return;

        String id = RangerItemDefinition.getIdFromStack(held);
        if (!ID_TICKET_ULTRA.equals(id)) return;

        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);

        if (world.isClientSide || !(player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        // NUEVO: obtenemos la capability de forma segura
        LazyOptional<IPassCapability> capOpt = PassCapabilities.getOptional(serverPlayer);
        capOpt.ifPresent(cap -> {
            // 2) Restaurar/notificar
            PassType actual = PassUtil.checkAndRestorePass(serverPlayer, cap);

            // 3) Si ya tiene Ultra o Master, mostramos tiempo restante
            if (actual.getTier() >= PassType.ULTRA.getTier()) {
                long expiresAt = cap.getExpiresAt();
                String remainingTime = "Desconocido";
                if (expiresAt != Long.MAX_VALUE) {
                    long millisLeft = expiresAt - System.currentTimeMillis();
                    if (millisLeft > 0) {
                        long seconds = Duration.ofMillis(millisLeft).getSeconds();
                        remainingTime = seconds + "s";
                    } else {
                        remainingTime = "Expirado";
                    }
                }
                serverPlayer.sendMessage(
                        new StringTextComponent(TextFormatting.DARK_PURPLE + "✦ Pase Ranger ✦"),
                        serverPlayer.getUUID()
                );
                serverPlayer.sendMessage(
                        new StringTextComponent(TextFormatting.RED + "❖ Ya tienes el pase ")
                                .append(actual.getGradientDisplayName())
                                .append(new StringTextComponent(TextFormatting.RED + " activo.")),
                        serverPlayer.getUUID()
                );
                serverPlayer.sendMessage(
                        new StringTextComponent(TextFormatting.GRAY + "❖ Tiempo restante: ")
                                .append(new StringTextComponent(TextFormatting.YELLOW + remainingTime)),
                        serverPlayer.getUUID()
                );
                return;
            }

            // 4) Otorgar Ultra por 1 día
            cap.grantPass(PassType.ULTRA.getTier(), ULTRA_DURATION_MS);
            cap.syncToClient(serverPlayer);

            // 5) Notificar expiración
            long expiresAt = cap.getExpiresAt();
            String expirationTime = expiresAt == Long.MAX_VALUE
                    ? "Nunca"
                    : java.time.format.DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(java.time.ZoneOffset.UTC)
                    .format(java.time.Instant.ofEpochMilli(expiresAt)) + " UTC";

            serverPlayer.sendMessage(
                    new StringTextComponent(TextFormatting.DARK_PURPLE + "✦ Pase Ranger ✦"),
                    serverPlayer.getUUID()
            );
            serverPlayer.sendMessage(
                    new StringTextComponent(TextFormatting.GREEN + "❖ ᴀᴄᴛɪᴠᴀᴅᴏ ")
                            .append(PassType.ULTRA.getGradientDisplayName())
                            .append(new StringTextComponent(TextFormatting.GREEN + " por 1 día!")),
                    serverPlayer.getUUID()
            );
            serverPlayer.sendMessage(
                    new StringTextComponent(TextFormatting.GRAY + "❖ Expira: ")
                            .append(new StringTextComponent(TextFormatting.YELLOW + expirationTime)),
                    serverPlayer.getUUID()
            );

            // 6) Título y subtítulo
            IFormattableTextComponent titleText = new StringTextComponent("Pase Ranger")
                    .withStyle(TextFormatting.GOLD);
            IFormattableTextComponent subTitleText = new StringTextComponent("ᴀᴄᴛɪᴠᴀᴅᴏ ")
                    .withStyle(TextFormatting.AQUA)
                    .append(PassType.ULTRA.getGradientDisplayName());
            serverPlayer.connection.send(new STitlePacket(STitlePacket.Type.TITLE, titleText));
            serverPlayer.connection.send(new STitlePacket(STitlePacket.Type.SUBTITLE, subTitleText));

            // 7) Consumir ticket
            if (!serverPlayer.isCreative()) held.shrink(1);

            // 8) Sonido de confirmación
            serverPlayer.level.playSound(
                    null,
                    serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.IRON_DOOR_OPEN,
                    SoundCategory.MASTER,
                    1.0f, 0.5f
            );
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        ItemStack held = player.getItemInHand(event.getHand());
        if (held.isEmpty()) return;

        String id = RangerItemDefinition.getIdFromStack(held);
        if (ID_TICKET_ULTRA.equals(id)) {
            event.setCanceled(true);
            event.setCancellationResult(ActionResultType.SUCCESS);
        }
    }
}
