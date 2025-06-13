package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.server.ServerWorld;
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
import rl.sage.rangerlevels.config.ItemsConfig;
import rl.sage.rangerlevels.config.MysteryBoxesConfig;
import rl.sage.rangerlevels.items.amuletos.ChampionAmulet;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.boxes.MysteryBoxHelper;
import rl.sage.rangerlevels.rewards.RewardManager;
import rl.sage.rangerlevels.util.*;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelProvider {

    @CapabilityInject(ILevel.class)
    public static Capability<ILevel> LEVEL_CAP = null;

    private static final Random RANDOM = new Random();

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
        // =========================
        // 1) ¿Lleva Amuleto de Campeón?
        // =========================
        boolean hasAmulet = false;
        for (int slot = 0; slot < player.inventory.getContainerSize(); slot++) {
            net.minecraft.item.ItemStack stack = player.inventory.getItem(slot);
            if (stack != null && !stack.isEmpty()) {
                String id = RangerItemDefinition.getIdFromStack(stack);
                if (ChampionAmulet.ID.equals(id)) {
                    hasAmulet = true;
                    break;
                }
            }
        }

        if (hasAmulet) {
            // 1.1) Leemos configuración de MysteryBoxesConfig.yml
            ItemsConfig.ChampionAmuletConfig amCfg = ItemsConfig.get().championAmulet;

            // 1.2) Aplicar bonus de EXP
            double xpPercent = amCfg.xpPercent; // ej. 15.0
            int bonusXp = (int) Math.floor(amount * (xpPercent / 100.0));
            amount += bonusXp;
        }

        if (!WorldUtils.isWorldAllowed(player)) {
            // Mensaje simple sin colores avanzados
            player.displayClientMessage(
                    new StringTextComponent("§cMundo inhabilitado"),
                    true
            );
            return;
        }

        ExpConfig cfg = ExpConfig.get();

        // 2) Preparar sonidos
        SoundConfig normalCfg = cfg.levelUpSound;
        SoundEvent normalSound = ForgeRegistries.SOUND_EVENTS
                .getValue(new ResourceLocation(normalCfg.soundEvent));
        if (normalSound == null) normalSound = SoundEvents.PLAYER_LEVELUP;

        MaxLevelBroadcastConfig bc = cfg.maxLevelBroadcast;
        SoundEvent maxSound = ForgeRegistries.SOUND_EVENTS
                .getValue(new ResourceLocation(bc.soundEvent));
        if (maxSound == null) maxSound = SoundEvents.UI_TOAST_CHALLENGE_COMPLETE;

        // 3) Obtener capability
        LazyOptional<ILevel> capOpt = player.getCapability(LEVEL_CAP, null);
        if (!capOpt.isPresent()) return;
        ILevel cap = capOpt.orElseThrow(IllegalStateException::new);

        int oldLvl = cap.getLevel();
        int maxLvl = cfg.getMaxLevel();
        if (oldLvl >= maxLvl) return;

        // 4) Mostrar EXP ganada (legacy)
        player.displayClientMessage(
                new StringTextComponent("§b+" + amount + " EXP"),
                true
        );

        // 5) Sumar EXP y obtener niveles subidos
        List<Integer> niveles = cap.addExp(amount);

        // 6) Procesar cada nivel subido
        for (int lvl : niveles) {
            if (lvl > maxLvl) break;
            RewardManager.handleLevelUp(player, lvl);
            ServerWorld world = (ServerWorld) player.level;
            BlockPos pos      = player.blockPosition();
            MysteryBoxHelper.tryDropOneOnEvent(
                    player,
                    MysteryBoxHelper.EventType.LEVEL_UP,
                    world,
                    pos,
                    MysteryBoxesConfig.get().mysteryBox.comun
            );
            // 6.1) Separador degradado
            IFormattableTextComponent sep = GradientText.of(
                            "                                                                      ",
                            "#FF0000", "#FF7F00", "#FFFF00",
                            "#00FF00", "#0000FF", "#4B0082", "#9400D3"
                    )
                    // Lo pintamos de blanco
                    .setStyle(Style.EMPTY.withColor(TextColorUtil.fromHex("#FFFFFF")));
            player.displayClientMessage(sep, false);

            // 6.2) Título (usa legacy directo, no necesita TextFormatterUtil)
            String titleRaw = (lvl >= maxLvl)
                    ? "§6¡Alcanzaste el Nivel Máximo! ¡Felicidades!"
                    : "§6Subiste a Nivel §7(§f" + lvl + "§7)";
            player.displayClientMessage(
                    new StringTextComponent(titleRaw),
                    false
            );
            player.displayClientMessage(sep, false);

            // 6.3) Crear Title y Subtitle con gradient, manteniendo el mismo texto:
            IFormattableTextComponent titleGradient;
            IFormattableTextComponent subtitleGradient;
            if (lvl >= maxLvl) {
                // Si es nivel máximo, degradado dorado→rojo
                titleGradient = GradientText.of(
                        "¡Nivel Máximo!",
                        "#FFD700", "#FFA500", "#FF4500", "#FF0000"
                ).setStyle(Style.EMPTY.withBold(true));
                subtitleGradient = GradientText.of(
                        "¡Gracias por llegar tan lejos, Ranger!",
                        "#FFFFFF", "#AAAAAA", "#888888"
                );
            } else {
                // Si es nivel normal, degradado aqua→azul
                titleGradient = GradientText.of(
                        " ",
                        "#FFAA00", "#FFAA00", "#FFAA00"
                ).setStyle(Style.EMPTY.withBold(true));
                subtitleGradient = GradientText.of(
                        "Subiste a Nivel " + lvl,
                        "#FFFF00", "#FFDD00", "#FFAA00"
                );
            }

            // 6.4) Enviamos el TITLE (fadeIn = 10, stay = 70, fadeOut = 20)
            player.connection.send(new STitlePacket(
                    STitlePacket.Type.TITLE,
                    titleGradient,
                    10, 70, 20
            ));

            // 6.5) Enviamos el SUBTITLE (mismos tiempos)
            player.connection.send(new STitlePacket(
                    STitlePacket.Type.SUBTITLE,
                    subtitleGradient,
                    10, 70, 20
            ));

            // 6.6) Action bar
            player.displayClientMessage(
                    new StringTextComponent("§e⇧ §3Nivel Ranger " + lvl),
                    true
            );

            // 6.7) Sonido local
            if (lvl < maxLvl) {
                player.connection.send(new SPlaySoundEffectPacket(
                        normalSound, SoundCategory.PLAYERS,
                        player.getX(), player.getY(), player.getZ(),
                        normalCfg.volume, normalCfg.pitch
                ));
            }

            // 6.8) Broadcast nivel máximo
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
