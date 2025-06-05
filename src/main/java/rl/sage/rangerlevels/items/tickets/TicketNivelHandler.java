package rl.sage.rangerlevels.items.tickets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.config.LevelsConfig;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.rewards.RewardManager;
import rl.sage.rangerlevels.util.GradientText;

import java.util.UUID;

/**
 * Handler para el “Ticket de Nivel”. Comprueba que el ItemStack tenga
 * NBT "RangerID" = "ticket_nivel" y sube exactamente un nivel.
 */
@Mod.EventBusSubscriber(modid = RangerLevels.MODID)
public class TicketNivelHandler {

    private static final String ID_TICKET_NIVEL = "ticket_nivel";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        PlayerEntity player = event.getPlayer();
        World world = event.getWorld();
        Hand hand = event.getHand();

        ItemStack held = player.getItemInHand(hand);
        if (held == null || held.isEmpty()) return;

        // 1) Verificar el ID NBT "ticket_nivel"
        String id = RangerItemDefinition.getIdFromStack(held);
        if (!ID_TICKET_NIVEL.equals(id)) return;

        // (Opcional) comprobar que su Tier sea RARO
        Tier tier = RangerItemDefinition.getTierFromStack(held);
        if (tier != Tier.RARO) return;

        // 2) Cancelar uso vanilla
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.util.ActionResultType.SUCCESS);

        // 3) Lógica solo en servidor
        if (world.isClientSide) return;
        if (!(player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        // 4) Obtener capability de nivel
        serverPlayer.getCapability(LevelProvider.LEVEL_CAP).ifPresent(cap -> {
            int currentLevel = cap.getLevel();
            ExpConfig cfg = ExpConfig.get();
            int maxLevel = cfg.getMaxLevel();
            LevelsConfig levelsCfg = cfg.getLevels();

            // 5) Si ya está en nivel máximo
            if (currentLevel >= maxLevel) {
                StringTextComponent titulo = new StringTextComponent(TextFormatting.DARK_BLUE + "✦ Ticket de Nivel ✦");
                StringTextComponent linea = new StringTextComponent(TextFormatting.RED + "Ya has alcanzado el nivel máximo (" + maxLevel + ").");
                serverPlayer.sendMessage(titulo.copy(), serverPlayer.getUUID());
                serverPlayer.sendMessage(linea, serverPlayer.getUUID());
                return;
            }

            // 6) Subir exactamente 1 nivel
            for (int lvl : cap.addLevel(1)) {
                // 6.1) Manejar recompensas de ese nivel
                RewardManager.handleLevelUp(serverPlayer, lvl);

                // 6.2) Mostrar separación en gradiente + título
                IFormattableTextComponent sep = GradientText.of(
                        "                                                                      ",
                        "#FF0000", "#FF7F00", "#FFFF00",
                        "#00FF00", "#0000FF", "#4B0082", "#9400D3"
                ).withStyle(TextFormatting.STRIKETHROUGH);

                IFormattableTextComponent title = new StringTextComponent(
                        TextFormatting.GOLD +
                                (lvl >= maxLevel
                                        ? "¡Alcanzaste el Nivel Máximo! ¡Felicidades!"
                                        : "Subiste a Nivel §7(§f" + lvl + "§7)")
                );

                serverPlayer.displayClientMessage(sep, false);
                serverPlayer.displayClientMessage(title, false);
                serverPlayer.displayClientMessage(sep, false);

                // 6.3) Mensaje flotante en action bar con borde Unicode
                String actionBar = TextFormatting.YELLOW + "⇧ "
                        + TextFormatting.DARK_BLUE + "Nivel Ranger " + lvl;
                serverPlayer.displayClientMessage(
                        new StringTextComponent(actionBar),
                        true
                );

                // 6.4) Reproducir sonido de level-up
                serverPlayer.level.playSound(
                        null,
                        serverPlayer.blockPosition(),
                        SoundEvents.PLAYER_LEVELUP,
                        SoundCategory.PLAYERS,
                        1.0f, 1.0f
                );
            }

            // 7) Consumir el ticket (si no está en creativo)
            if (!serverPlayer.isCreative()) {
                held.shrink(1);
            }

            // 8) Sonido de activación de “ticket”
            serverPlayer.level.playSound(
                    null,
                    serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.IRON_DOOR_OPEN,
                    SoundCategory.MASTER,
                    1.0f, 0.5f
            );

            // 9) Mensaje final indicando activado
            StringTextComponent msgTitulo = new StringTextComponent(TextFormatting.DARK_GREEN + "✦ ᴀᴄᴛɪᴠᴀᴅᴏ Ticket de Nivel ✦");
            serverPlayer.sendMessage(msgTitulo, serverPlayer.getUUID());
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        ItemStack held = player.getItemInHand(event.getHand());
        if (held == null || held.isEmpty()) return;

        // Solo cancelamos la acción si es “ticket_nivel”
        String id = RangerItemDefinition.getIdFromStack(held);
        if (ID_TICKET_NIVEL.equals(id)) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.util.ActionResultType.SUCCESS);
        }
    }
}
