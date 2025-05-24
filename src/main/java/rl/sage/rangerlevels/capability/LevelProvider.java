package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.broadcast.BroadcastUtil;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.config.ExpConfig.SoundConfig;
import rl.sage.rangerlevels.config.ExpConfig.MaxLevelBroadcastConfig;
import rl.sage.rangerlevels.rewards.RewardManager;
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.TextColorUtil;
import rl.sage.rangerlevels.util.TextFormatterUtil;
import rl.sage.rangerlevels.util.WorldUtils;

import java.util.List;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelProvider {

    @CapabilityInject(ILevel.class)
    public static Capability<ILevel> LEVEL_CAP = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(
                ILevel.class,
                new LevelStorage(),
                LevelCapability::new
        );
    }

    public static LazyOptional<ILevel> get(PlayerEntity player) {
        return player.getCapability(LEVEL_CAP, null);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        LazyOptional<ILevel> oldOpt = event.getOriginal().getCapability(LEVEL_CAP, null);
        LazyOptional<ILevel> newOpt = event.getPlayer().getCapability(LEVEL_CAP, null);
        if (oldOpt.isPresent() && newOpt.isPresent()) {
            ILevel oldData = oldOpt.orElseThrow(IllegalStateException::new);
            ILevel newData = newOpt.orElseThrow(IllegalStateException::new);
            newData.setLevel(oldData.getLevel());
            newData.setExp(oldData.getExp());
            newData.setPlayerMultiplier(oldData.getPlayerMultiplier());
        }
    }

    public static void giveExpAndNotify(ServerPlayerEntity player, int amount) {
        if (!WorldUtils.isWorldAllowed(player)) {
            // Mensaje simple sin colores avanzados
            player.displayClientMessage(
                    new StringTextComponent("§cMundo inhabilitado"),
                    true
            );
            return;
        }

        ExpConfig cfg = ExpConfig.get();

        // 1) Preparar sonidos
        SoundConfig normalCfg = cfg.levelUpSound;
        SoundEvent normalSound = ForgeRegistries.SOUND_EVENTS
                .getValue(new ResourceLocation(normalCfg.soundEvent));
        if (normalSound == null) normalSound = SoundEvents.PLAYER_LEVELUP;

        MaxLevelBroadcastConfig bc = cfg.maxLevelBroadcast;
        SoundEvent maxSound = ForgeRegistries.SOUND_EVENTS
                .getValue(new ResourceLocation(bc.soundEvent));
        if (maxSound == null) maxSound = SoundEvents.UI_TOAST_CHALLENGE_COMPLETE;

        // 2) Obtener capability
        LazyOptional<ILevel> capOpt = player.getCapability(LEVEL_CAP, null);
        if (!capOpt.isPresent()) return;
        ILevel cap = capOpt.orElseThrow(IllegalStateException::new);

        int oldLvl = cap.getLevel();
        int maxLvl = cfg.getMaxLevel();
        if (oldLvl >= maxLvl) return;

        // 3) Mostrar EXP ganada (legacy)
        player.displayClientMessage(
                new StringTextComponent("§b+" + amount + " EXP"),
                true
        );

        // 4) Sumar EXP y obtener niveles subidos
        List<Integer> niveles = cap.addExp(amount);

        // 5) Procesar cada nivel subido
        for (int lvl : niveles) {
            if (lvl > maxLvl) break;
            RewardManager.handleLevelUp(player, lvl);

            // 5.1) Separador degradado
            IFormattableTextComponent sep = GradientText.of(
                            "                                                                      ",
                            "#FF0000", "#FF7F00", "#FFFF00",
                            "#00FF00", "#0000FF", "#4B0082", "#9400D3"
                    )
                    // Lo pintamos de blanco
                    .setStyle(Style.EMPTY.withColor(TextColorUtil.fromHex("#FFFFFF")));
            player.displayClientMessage(sep, false);

            // 5.2) Título (usa legacy directo, no necesita TextFormatterUtil)
            String titleRaw = (lvl >= maxLvl)
                    ? "§6¡Alcanzaste el Nivel Máximo! ¡Felicidades!"
                    : "§6Subiste a Nivel §7(§f" + lvl + "§7)";
            player.displayClientMessage(
                    new StringTextComponent(titleRaw),
                    false
            );
            player.displayClientMessage(sep, false);

            // 5.3) Action bar
            player.displayClientMessage(
                    new StringTextComponent("§e⇧ §3Nivel Ranger " + lvl),
                    true
            );

            // 5.4) Sonido local
            if (lvl < maxLvl) {
                player.connection.send(new SPlaySoundEffectPacket(
                        normalSound, SoundCategory.PLAYERS,
                        player.getX(), player.getY(), player.getZ(),
                        normalCfg.volume, normalCfg.pitch
                ));
            }

            // 5.5) Broadcast nivel máximo
            if (lvl == maxLvl && bc.enable) {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    // Usa BroadcastUtil para reemplazar variables y parsear colores/gradientes
                    BroadcastUtil.broadcastMaxLevel(
                            server,
                            bc.message,
                            player.getName().getString(),
                            lvl
                    );

                    // Luego enviamos el sonido a cada jugador
                    for (ServerPlayerEntity pl : server.getPlayerList().getPlayers()) {
                        pl.connection.send(new SPlaySoundEffectPacket(
                                maxSound, SoundCategory.PLAYERS,
                                pl.getX(), pl.getY(), pl.getZ(),
                                bc.volume, bc.pitch
                        ));
                    }
                }
            }
        }
    }
}
