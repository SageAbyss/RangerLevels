// LevelProvider.java
package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.rewards.RewardManager;
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.WorldUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelProvider {

    @CapabilityInject(ILevel.class)
    public static Capability<ILevel> LEVEL_CAP = null;

    /** Registra tu capability (debes llamarlo desde tu setup) */
    public static void register() {
        CapabilityManager.INSTANCE.register(
                ILevel.class,
                new LevelStorage(),
                LevelCapability::new
        );
    }

    /** Obtiene el capability */
    public static LazyOptional<ILevel> get(PlayerEntity player) {
        return player.getCapability(LEVEL_CAP, null);
    }

    /** Transfiere al revivir */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        event.getOriginal().getCapability(LEVEL_CAP).ifPresent(oldData ->
                event.getPlayer().getCapability(LEVEL_CAP).ifPresent(newData -> {
                    newData.setLevel(oldData.getLevel());
                    newData.setExp(oldData.getExp());
                    newData.setPlayerMultiplier(oldData.getPlayerMultiplier());
                })
        );
    }

    /** Lógica de sumar EXP y notificar */
    public static void giveExpAndNotify(ServerPlayerEntity player, int amount) {
        if (!WorldUtils.isWorldAllowed(player)) {
            player.displayClientMessage(
                    new StringTextComponent(TextFormatting.RED + "Mundo inhabilitado"),
                    true
            );
            return;
        }

        player.getCapability(LEVEL_CAP).ifPresent(cap -> {
            int oldLvl = cap.getLevel();
            int maxLvl = ExpConfig.get().getMaxLevel();
            if (oldLvl >= maxLvl) return;

            boolean up = cap.addExp(amount);
            int newLvl = cap.getLevel();

            player.displayClientMessage(
                    new StringTextComponent(TextFormatting.AQUA + "+" + amount + " EXP"),
                    true
            );

            if (up) {
                RewardManager.handleLevelUp(player, newLvl);

                IFormattableTextComponent sep = GradientText.of(
                        "                                                                      ",
                        "#FF0000","#FF7F00","#FFFF00","#00FF00","#0000FF","#4B0082","#9400D3"
                ).withStyle(TextFormatting.STRIKETHROUGH);

                IFormattableTextComponent title = new StringTextComponent(
                        TextFormatting.GOLD +
                                (newLvl >= maxLvl
                                        ? "¡Alcanzaste el Nivel Máximo! ¡Felicidades!"
                                        : "Subiste de Nivel §7(§f" + newLvl + "§7)")
                );

                player.displayClientMessage(sep, false);
                player.displayClientMessage(title, false);
                player.displayClientMessage(sep, false);

                player.displayClientMessage(
                        new StringTextComponent("§e⇧ §3Nivel Ranger " + newLvl),
                        true
                );

                player.level.playSound(
                        null, player.blockPosition(),
                        SoundEvents.PLAYER_LEVELUP,
                        SoundCategory.PLAYERS,
                        1.0f,1.0f
                );
            }
        });
    }
}
