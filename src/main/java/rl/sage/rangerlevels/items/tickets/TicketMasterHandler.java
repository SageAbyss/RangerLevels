// src/main/java/rl/sage/rangerlevels/items/tickets/TicketMasterHandler.java
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
import net.minecraftforge.common.util.LazyOptional;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.IPassCapability;
import rl.sage.rangerlevels.capability.PassCapabilities;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.pass.PassType;
import rl.sage.rangerlevels.pass.PassUtil;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.time.Duration;

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
        if (held.isEmpty()) return;

        String id = RangerItemDefinition.getIdFromStack(held);
        if (!ID_TICKET_MASTER.equals(id)) return;

        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);

        if (world.isClientSide || !(player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        LazyOptional<IPassCapability> capOpt = PassCapabilities.getOptional(serverPlayer);
        capOpt.ifPresent(cap -> {
            PassType actual = PassUtil.checkAndRestorePass(serverPlayer, cap);

            if (actual.getTier() >= PassType.MASTER.getTier()) {
                long expiresAt = cap.getExpiresAt();
                String remainingTime = "Desconocido";
                if (expiresAt != Long.MAX_VALUE) {
                    long millisLeft = expiresAt - System.currentTimeMillis();
                    long hours = Duration.ofMillis(millisLeft).toHours();
                    long minutes = Duration.ofMillis(millisLeft).minusHours(hours).toMinutes();
                    remainingTime = hours + "h " + minutes + "m";
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

            cap.grantPass(PassType.MASTER.getTier(), MASTER_DURATION_MS);
            cap.syncToClient(serverPlayer);

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
                            .append(PassType.MASTER.getGradientDisplayName())
                            .append(new StringTextComponent(TextFormatting.GREEN + " por 1 día!")),
                    serverPlayer.getUUID()
            );
            serverPlayer.sendMessage(
                    new StringTextComponent(TextFormatting.GRAY + "❖ Expira: ")
                            .append(new StringTextComponent(TextFormatting.YELLOW + expirationTime)),
                    serverPlayer.getUUID()
            );

            ITextComponent titleText = new StringTextComponent("Pase Ranger").withStyle(TextFormatting.GOLD);
            ITextComponent subTitleText = new StringTextComponent("ᴀᴄᴛɪᴠᴀᴅᴏ ")
                    .withStyle(TextFormatting.AQUA)
                    .append(PassType.MASTER.getGradientDisplayName());
            serverPlayer.connection.send(new STitlePacket(STitlePacket.Type.TITLE, titleText));
            serverPlayer.connection.send(new STitlePacket(STitlePacket.Type.SUBTITLE, subTitleText));

            held.shrink(1);

            PlayerSoundUtils.playSoundToPlayer((ServerPlayerEntity)
                    player, SoundEvents.BEACON_DEACTIVATE, SoundCategory.MASTER, 1f, 1f);
            PlayerSoundUtils.playSoundToPlayer((ServerPlayerEntity)
                    player, SoundEvents.END_PORTAL_SPAWN, SoundCategory.MASTER, 1f, 0.8f);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        ItemStack held = player.getItemInHand(event.getHand());
        if (held.isEmpty()) return;

        String id = RangerItemDefinition.getIdFromStack(held);
        if (ID_TICKET_MASTER.equals(id)) {
            event.setCanceled(true);
            event.setCancellationResult(ActionResultType.SUCCESS);
        }
    }
}
