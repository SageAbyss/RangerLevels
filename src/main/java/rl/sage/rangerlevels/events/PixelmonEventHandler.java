package rl.sage.rangerlevels.events;

import com.pixelmonmod.pixelmon.Pixelmon;

import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.events.*;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.api.events.battles.CatchComboEvent;
import com.pixelmonmod.pixelmon.api.events.legendary.ArceusEvent;
import com.pixelmonmod.pixelmon.api.events.legendary.TimespaceEvent;
import com.pixelmonmod.pixelmon.api.events.raids.StartRaidEvent;
import com.pixelmonmod.pixelmon.api.pokedex.PokedexRegistrationStatus;
import com.pixelmonmod.pixelmon.api.pokemon.boss.BossTierRegistry;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.capability.PassCapabilities;
import rl.sage.rangerlevels.items.randoms.GuantesEntrenadorHandler;
import rl.sage.rangerlevels.items.sacrificios.BossEssenceHelper;
import rl.sage.rangerlevels.items.sacrificios.ConcentradoDeAlmasHandler;
import rl.sage.rangerlevels.items.sacrificios.EspecieEssenceHelper;
import rl.sage.rangerlevels.items.sello.SelloReflejoMaestroHandler;
import rl.sage.rangerlevels.items.totems.fragmentos.*;
import rl.sage.rangerlevels.items.amuletos.ChampionAmulet;
import rl.sage.rangerlevels.items.RangerItemDefinition;

import rl.sage.rangerlevels.items.amuletos.ShinyAmuletHandler;
import rl.sage.rangerlevels.items.boxes.MysteryBoxHelper;
import rl.sage.rangerlevels.items.flag.BattleBannerHandler;
import rl.sage.rangerlevels.items.gemas.ExpGemHandler;
import rl.sage.rangerlevels.config.*;
import rl.sage.rangerlevels.items.manuales.ManualTrainingHandler;
import rl.sage.rangerlevels.items.polvo.PolvoExpHandler;
import rl.sage.rangerlevels.limiter.LimiterHelper;
import rl.sage.rangerlevels.multiplier.MultiplierManager;
import rl.sage.rangerlevels.pass.PassType;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class PixelmonEventHandler {
    private static final Logger LOGGER = LogManager.getLogger(PixelmonEventHandler.class);
    private static final Random RNG = new Random();

    public static void register() {
        Pixelmon.EVENT_BUS.register(PixelmonEventHandler.class);
        MinecraftForge.EVENT_BUS.register(PixelmonEventHandler.class);
    }

    @Nullable
    private static EventConfig getCfg(String key) {
        if (ConfigLoader.CONFIG == null) return null;
        Map<String, EventConfig> map = ConfigLoader.CONFIG.getPixelmonEvents();
        return map == null ? null : map.get(key);
    }

    private static int randomInRange(int min, int max) {
        return min + RNG.nextInt(Math.max(1, max - min + 1));
    }

    private static int applyMultipliers(ServerPlayerEntity player, int baseExp, String eventKey) {
        double eventMul = ExpConfig.get().getEventMultiplier(eventKey);
        double global   = MultiplierManager.instance().getGlobal();
        double personal = MultiplierManager.instance().getPlayer(player);

        int tier = PassCapabilities.get(player).getTier();
        PassType[] types = PassType.values();
        if (tier < 0 || tier >= types.length) tier = 0;
        PassType pass = types[tier];

        double passMultiplier;
        switch (pass) {
            case SUPER:  passMultiplier = 1.25; break;
            case ULTRA:  passMultiplier = 1.5;  break;
            case MASTER: passMultiplier = 2.0;  break;
            default:     passMultiplier = 1.0;  break;
        }

        double resultado = baseExp * eventMul * global * personal * passMultiplier;
        return (int) Math.round(resultado);
    }

    private static void giveExp(ServerPlayerEntity player, int baseExp, String eventKey) {
        int total = applyMultipliers(player, baseExp, eventKey);
        LimiterHelper.giveExpWithLimit(player, total);
    }

    private static boolean isOp(ServerPlayerEntity player) {
        return player.getServer() != null
                && player.getServer().getPlayerList().isOp(player.getGameProfile());
    }

    private static boolean hasAnyPermission(ServerPlayerEntity player, @Nullable java.util.List<String> perms) {
        if (player == null) return false;
        if (isOp(player)) return true;
        if (PermissionAPI.hasPermission(player, "rangerlevels.admin")) return true;
        if (perms != null) {
            for (String perm : perms) {
                if (PermissionAPI.hasPermission(player, perm)) return true;
            }
        }
        return false;
    }

    @Nullable
    private static Integer getVipRangeExp(ServerPlayerEntity player, @Nullable SpecificRangePermissions vip) {
        if (player == null || vip == null || !vip.isEnable()) return null;
        try {
            java.util.List<String> perms = vip.getPermissions();
            if (isOp(player) || PermissionAPI.hasPermission(player, "rangerlevels.admin")) {
                if (!perms.isEmpty()) {
                    String range = perms.get(perms.size() - 1);
                    range = range.substring(range.lastIndexOf('.') + 1);
                    if (range.contains("-")) {
                        String[] nums = range.split("-");
                        return randomInRange(Integer.parseInt(nums[0]), Integer.parseInt(nums[1]));
                    }
                }
                return null;
            }
            for (String perm : perms) {
                if (PermissionAPI.hasPermission(player, perm)) {
                    String range = perm.substring(perm.lastIndexOf('.') + 1);
                    if (range.contains("-")) {
                        String[] nums = range.split("-");
                        return randomInRange(Integer.parseInt(nums[0]), Integer.parseInt(nums[1]));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("VIP range parse error for {}: {}", player.getName().getString(), e.getMessage());
        }
        return null;
    }

    private static void handleGeneric(ServerPlayerEntity player, String key, Function<EventConfig, int[]> extractor) {
        EventConfig cfg = getCfg(key);
        if (cfg == null || !cfg.isEnable() || player == null) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;

        int[] range = extractor.apply(cfg);
        int base = randomInRange(range[0], range[1]);
        Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;

        giveExp(player, base, key);
    }

    @SubscribeEvent
    public static void onStartRaid(StartRaidEvent ev) {
        for (BattleParticipant part : ev.getAllyParticipants()) {
            if (!(part instanceof PlayerParticipant)) continue;
            ServerPlayerEntity player = ((PlayerParticipant) part).player;
            handleGeneric(player, "raidParticipation", EventConfig::getExpRange);
            ServerWorld world = (ServerWorld) player.level;
            BlockPos pos      = player.blockPosition();
            MysteryBoxHelper.tryDropOneOnEvent(
                    player,
                    MysteryBoxHelper.EventType.RAID,
                    world,
                    pos,
                    MysteryBoxesConfig.get().mysteryBox.comun
            );
        }
    }

    @SubscribeEvent
    public static void onCapture(CaptureEvent.SuccessfulCapture ev) {
        ServerPlayerEntity player = ev.getPlayer();
        PixelmonEntity pkmn = ev.getPokemon();
        if (player == null || pkmn == null) return;

        EventConfig cfg = getCfg("onCapture");
        if (cfg == null || !cfg.isEnable()) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;
        // ── BLOQUE AMULETO SHINY (delegado a ShinyAmuletHandler) ──
        ShinyAmuletHandler.tryUse(player, pkmn);
        // ─────────────────────────────────────────────────────────

        // ── Lógica normal de cálculo de EXP ──

        int base;
        boolean shiny = pkmn.getPokemon().isShiny();
        boolean legendary = pkmn.getPokemon().getSpecies().isLegendary()
                || pkmn.getPokemon().getSpecies().isUltraBeast();

        if (legendary) base = randomInRange(150, 300);
        else if (shiny) base = randomInRange(60, 120);
        else base = randomInRange(cfg.getExpRange()[0], cfg.getExpRange()[1]);

        Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;

        base = FragmentoCorazonGaiaHandler.applyBonusIfApplicable(player, pkmn, base);
        base = TotemRaizPrimordialHandler.applyExpBonusIfApplicable(player, pkmn, base, true);
        base = FragmentoIraAncestralHandler.applyBonusIfApplicable(player, pkmn, base);
        base = TotemLamentoDiosesHandler.applyExpBonusIfApplicable(player, pkmn, base, true);
        base = FragmentoRealidadAlternaHandler.applyBonusIfApplicable(player, pkmn, base);
        base = TotemAbismoGlacialHandler.applyExpBonusIfApplicable(player, pkmn, base, true);
        int totalSinGema = applyMultipliers(player, base, "onCapture");
        double gemBonus = ExpGemHandler.getBonus(player);
        int totalConGema = (int) Math.round(totalSinGema * (1.0 + gemBonus));
        // ── **Bonus de Concentrado de Almas** ──
        double xpMul = ConcentradoDeAlmasHandler.getXpMultiplier(player, legendary);
        int totalConAlmas = (int) Math.round(totalConGema * xpMul);
        LimiterHelper.giveExpWithLimit(player, totalConAlmas);
    }


    @SubscribeEvent
    public static void onLevelUp(LevelUpEvent.Post ev) {
        handleGeneric(ev.getPlayer(), "levelUp", EventConfig::getExpRange);
    }

    @SubscribeEvent
    public static void onRareCandy(RareCandyEvent ev) {
        handleGeneric(ev.getPlayer(), "rareCandy", EventConfig::getExpRange);
    }

    @SubscribeEvent
    public static void onEggHatch(EggHatchEvent.Post ev) {
        ServerPlayerEntity player = ev.getPlayer();
        EventConfig cfg = getCfg("eggHatch");
        if (cfg == null || !cfg.isEnable() || player == null) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;
        // 1) EXP base aleatoria
        int base = randomInRange(cfg.getExpRange()[0], cfg.getExpRange()[1]);
        // 2) VIP override si aplica
        Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;
        // 3) Aplicar sólo el bonus del polvo ancestral
        int totalConPolvo = PolvoExpHandler.applyBonus(player, base);
        // 4) Dar EXP con límite
        LimiterHelper.giveExpWithLimit(player, totalConPolvo);
    }

    @SubscribeEvent
    public static void onEvolve(EvolveEvent.Post ev) {
        ServerPlayerEntity player = ev.getPlayer();
        EventConfig cfg = getCfg("evolve");
        if (cfg == null || !cfg.isEnable() || player == null) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;
        // 1) EXP base aleatoria
        int base = randomInRange(cfg.getExpRange()[0], cfg.getExpRange()[1]);
        // 2) VIP override si aplica
        Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;
        // 3) Aplicar sólo el bonus del polvo ancestral
        int totalConPolvo = PolvoExpHandler.applyBonus(player, base);
        // 4) Dar EXP con límite
        LimiterHelper.giveExpWithLimit(player, totalConPolvo);
    }

    @SubscribeEvent
    public static void onPokeStop(PokeStopEvent.Drops.Post ev) {
        ServerPlayerEntity player = ev.getPlayer() instanceof ServerPlayerEntity
                ? (ServerPlayerEntity) ev.getPlayer() : null;
        handleGeneric(player, "pokeStop", EventConfig::getExpRange);
    }

    @SubscribeEvent
    public static void onApricornPick(ApricornEvent.Pick ev) {
        handleGeneric(ev.getPlayer(), "apricornPick", EventConfig::getExpRange);
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent ev) {
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.getPlayer();
        Advancement adv = ev.getAdvancement();
        ResourceLocation id = adv.getId();

        // 1) Ignorar avances sin display (recetas u otros triggers internos)
        if (adv.getDisplay() == null) {
            return;
        }

        // 2) Opcionalmente, filtrar rutas concretas de Minecraft que no quieras:
        if ("minecraft".equals(id.getNamespace())) {
            String path = id.getPath();
            // Por si acaso: descartar recetas
            if (path.startsWith("recipes/")) {
                return;
            }
            // Puedes añadir más filtros si hay otros avances que no te interesen
        }
        // 3) Para Pixelmon, asume que los avances que importan tienen display también.
        //    Si quieres filtrar algún namespace o ruta de Pixelmon en particular, hazlo aquí.
        //    Por ejemplo:
        // if ("pixelmon".equals(id.getNamespace())) {
        //     String path = id.getPath();
        //     if (path.startsWith("some_unwanted_path")) return;
        // }

        // 4) Lógica de exp para avances válidos
        EventConfig cfg = getCfg("advancement");
        if (cfg == null || !cfg.isEnable()) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;

        int[] range = cfg.getExpRange();
        int base = randomInRange(range[0], range[1]);
        if ("minecraft".equals(id.getNamespace())) {
            base /= 2; // tu lógica original para minecraft
        }
        Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;

        giveExp(player, base, "advancement");
    }


    @SubscribeEvent
    public static void onPokedexEntry(PokedexEvent.Post ev) {
        ServerPlayerEntity player = ev.getPlayer();
        EventConfig cfg = getCfg("pokedexEntry");
        if (cfg == null || !cfg.isEnable() || player == null
                || !ev.isCausedByCapture() || ev.getOldStatus() == PokedexRegistrationStatus.CAUGHT) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;
        handleGeneric(player, "pokedexEntry", EventConfig::getExpRange);
    }

    @SubscribeEvent
    public static void onCatchComboBonus(CatchComboEvent.ComboExperienceBonus ev) {
        EventConfig cfg = getCfg("comboBonus");
        if (cfg == null || !cfg.isEnable()) return;
        float current = ev.getExperienceModifier();
        ev.setExperienceModifier(current + 0.1f + RNG.nextFloat() * 0.4f);
    }

    @SubscribeEvent
    public static void onBeatWild(BeatWildPixelmonEvent ev) {
        ServerPlayerEntity player = ev.player;
        EventConfig cfg = getCfg("beatWild");
        if (cfg == null || !cfg.isEnable() || player == null) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;

        PixelmonEntity defeated = (PixelmonEntity) ev.wpp.getEntity();
        int[] wild      = cfg.getExpRangeWild();
        int[] shiny     = cfg.getExpRangeShiny();
        int[] legendary = cfg.getExpRangeLegendary();
        int[] boss      = cfg.getExpRangeBoss();
        int base;
        if (BossEssenceHelper.isBoss(defeated)) {
            BossEssenceHelper.tryGiveBossEssenceOnDefeat(player, defeated);
            base = randomInRange(boss[0], boss[1]);
        }
        else if (defeated.getPokemon().isLegendary()
                || defeated.getPokemon().getSpecies().isUltraBeast()) {
            EspecieEssenceHelper.giveEssence(player, defeated);
            base = randomInRange(legendary[0], legendary[1]);
            if (RNG.nextDouble() < 0.05) {
                ServerWorld world = (ServerWorld) player.level;
                BlockPos pos      = player.blockPosition();
                MysteryBoxHelper.tryDropOneOnEvent(
                        player,
                        MysteryBoxHelper.EventType.BEAT_BOSS,
                        world,
                        pos,
                        MysteryBoxesConfig.get().mysteryBox.comun
                );
            }
        }
        else if (defeated.getPokemon().isShiny()) {
            base = randomInRange(shiny[0], shiny[1]);
            if (RNG.nextDouble() < 0.05) {
                ServerWorld world = (ServerWorld) player.level;
                BlockPos pos      = player.blockPosition();
                MysteryBoxHelper.tryDropOneOnEvent(
                        player,
                        MysteryBoxHelper.EventType.BEAT_BOSS,
                        world,
                        pos,
                        MysteryBoxesConfig.get().mysteryBox.comun
                );
            }
        }
        else {
            base = randomInRange(wild[0], wild[1]);
        }

        Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;

        // ── BLOQUE AMULETO DE CAMPEÓN ──
        boolean hasAmulet = player.inventory.items.stream()
                .filter(Objects::nonNull).anyMatch(stack ->
                        ChampionAmulet.ID.equals(RangerItemDefinition.getIdFromStack(stack))
                );
        if (hasAmulet) {
            ItemsConfig.ChampionAmuletConfig amCfg = ItemsConfig.get().championAmulet;
            for (MysteryBoxesConfig.MysteryBoxConfig.CommandEntry entry : amCfg.commands) {
                // ahora entry.chancePercent y entry.command existen
                if (RNG.nextDouble() * 100 < entry.chancePercent) {
                    String cmdToRun = entry.command.replace("%player%", player.getName().getString());
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server != null) {
                        server.getCommands().performCommand(
                                server.createCommandSourceStack(),
                                cmdToRun
                        );
                    }
                }
            }
        }
        // ---- SELLO REFLEJO DEL MAISTRO
        double chance = SelloReflejoMaestroHandler.getChanceForPlayer(player);
        if (chance > 0) {
            if (new Random().nextDouble() < chance) {
                base *= 2;
            }
        }
        // ——— AÑADIDO: bonus de Bandera de Batalla ———
        if (BattleBannerHandler.isInControlledArea(player)) {
            base = (int) Math.round(base * 1.5);
            player.sendMessage(new StringTextComponent("§a+50% EXP de Bandera de Batalla!"), player.getUUID());
        }
        base = FragmentoIraAncestralHandler.applyBonusIfApplicable(player, defeated, base);
        base = FragmentoCorazonGaiaHandler.applyBonusIfApplicable(player, defeated, base);
        base = FragmentoRealidadAlternaHandler.applyBonusIfApplicable(player, defeated, base);
        base = TotemLamentoDiosesHandler.applyExpBonusIfApplicable(player, defeated, base, false);
        base = TotemRaizPrimordialHandler.applyExpBonusIfApplicable(player, defeated, base, false);
        base = TotemAbismoGlacialHandler.applyExpBonusIfApplicable(player, defeated, base, false);
        // ── Cálculo normal (sin gema) ──
        int totalSinGema = applyMultipliers(player, base, "beatWild");

        double glovesBonus = GuantesEntrenadorHandler.getBonus(player, defeated);
        int totalWithGloves = (int) Math.round(totalSinGema * (1.0 + glovesBonus));
        double gemBonus = ExpGemHandler.getBonus(player); // 0.10, 0.30, 0.50 o 0.0
        int totalConGema = (int) Math.round(totalWithGloves * (1.0 + gemBonus));
        int totalConManual = ManualTrainingHandler.applyBonus(player, totalConGema);
        LimiterHelper.giveExpWithLimit(player, totalConManual);
    }

    @SubscribeEvent
    public static void onBeatTrainer(BeatTrainerEvent ev) {
        ServerPlayerEntity player = ev.player;
        EventConfig cfg = getCfg("beatTrainer");
        if (cfg == null || !cfg.isEnable() || player == null) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;

        boolean isBoss = ev.trainer.getBossTier() != BossTierRegistry.NOT_BOSS;
        String key = isBoss ? "beatBoss" : "beatTrainer";

        // ── calculamos baseExp antes del amuleto ──
        int[] range = Objects.requireNonNull(getCfg(key)).getExpRange();
        int base = randomInRange(range[0], range[1]);
        Integer vip = getVipRangeExp(player, Objects.requireNonNull(getCfg(key)).getSpecificRangePermissions());
        if (vip != null) base = vip;

        // ── BLOQUE AMULETO DE CAMPEÓN ──
        boolean hasAmulet = player.inventory.items.stream()
                .filter(Objects::nonNull)
                .anyMatch(stack -> ChampionAmulet.ID.equals(RangerItemDefinition.getIdFromStack(stack)));
        if (hasAmulet) {
            ItemsConfig.ChampionAmuletConfig amCfg = ItemsConfig.get().championAmulet;
            // 1) bonus de EXP
            double xpPercent = amCfg.xpPercent;
            int bonusXp = (int) Math.floor(base * (xpPercent / 100.0));
            base += bonusXp;
            // 2) comandos configurados
            for (MysteryBoxesConfig.MysteryBoxConfig.CommandEntry entry : amCfg.commands) {
                if (RNG.nextDouble() * 100 < entry.chancePercent) {
                    String cmd = entry.command.replace("%player%", player.getName().getString());
                    MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
                    if (srv != null) {
                        srv.getCommands().performCommand(srv.createCommandSourceStack(), cmd);
                    }
                }
            }
        }
        // ---- SELLO REFLEJO DEL MAISTRO
        double chance = SelloReflejoMaestroHandler.getChanceForPlayer(player);
        if (chance > 0) {
            if (new Random().nextDouble() < chance) {
                base *= 2;
            }
        }
        // ────────────────────────────────────
        // ——— AÑADIDO: bonus de Bandera de Batalla ———
        if (BattleBannerHandler.isInControlledArea(player)) {
            base = (int) Math.round(base * 1.5);
            player.sendMessage(new StringTextComponent("§a+50% EXP de Bandera de Batalla!"), player.getUUID());
        }
        // ── Cálculo normal (sin gema) ──
        int total = applyMultipliers(player, base, key);
        int totalConManual = ManualTrainingHandler.applyBonus(player, total);
        LimiterHelper.giveExpWithLimit(player, totalConManual);
    }



    @SubscribeEvent
    public static void onArceusPlayFlute(ArceusEvent.PlayFlute ev) {
        ServerPlayerEntity player = ev.getPlayer();
        EventConfig cfg = getCfg("arceusPlayFlute");
        if (cfg == null || !cfg.isEnable() || player == null) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;

        int[] r = cfg.getExpRange();
        int base = randomInRange(r[0], r[1]);
        Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;
        if (TotemAbismoGlacialHandler.hasTotem(player)) {
            base = (int) Math.round(base * 1.30);
        }
        giveExp(player, base, "arceusPlayFlute");
    }

    @SubscribeEvent
    public static void onPlayerActivateShrine(PlayerActivateShrineEvent ev) {
        ServerPlayerEntity player = ev.getPlayer();
        EventConfig cfg = getCfg("playerActivateShrine");
        if (cfg == null || !cfg.isEnable() || player == null) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;
        if (!ev.canEncounter()) return;

        int[] r = cfg.getExpRange();
        int base = randomInRange(r[0], r[1]);
        Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;
        if (TotemLamentoDiosesHandler.hasTotem(player)) {
            base = (int) Math.round(base * 1.30);
        }
        giveExp(player, base, "playerActivateShrine");
    }

    @SubscribeEvent
    public static void onTimespacePlaceOrb(TimespaceEvent.PlaceOrb ev) {
        ServerPlayerEntity player = (ServerPlayerEntity) ev.getPlayer();
        EventConfig cfg = getCfg("timespaceAltarSpawn");
        if (cfg == null || !cfg.isEnable() || player == null) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;

        ItemStack stack = ev.getStack();
        String orb = Objects.requireNonNull(stack.getItem().getRegistryName()).getPath();
        if (!orb.equals("lustrous_orb") && !orb.equals("adamant_orb") && !orb.equals("griseous_orb")) return;

        int[] r = cfg.getExpRange();
        int base = randomInRange(r[0], r[1]);
        Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;
        // Totem de Raíz Primordial
        if (TotemRaizPrimordialHandler.hasTotem(player)) {
            base = (int) Math.round(base * 1.30);
        }
        giveExp(player, base, "timespaceAltarSpawn");
    }

    @SubscribeEvent
    public static void onPokeLootDrop(PokeLootEvent.Drop ev) {
        ServerPlayerEntity player = ev.player;
        EventConfig cfg = getCfg("pokeLootDrop");
        if (cfg == null || !cfg.isEnable() || player == null) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;

        int[] r = cfg.getExpRange();
        int base = randomInRange(r[0], r[1]);
        Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;

        giveExp(player, base, "pokeLootDrop");
    }
    @SubscribeEvent
    public static void onBeatPlayer(BattleEndEvent ev) {
        // Solo batallas 1 vs 1 de jugadores
        List<ServerPlayerEntity> players = ev.getPlayers();
        if (players.size() != 2) return;

        EventConfig cfg = getCfg("beatPlayer");
        if (cfg == null || !cfg.isEnable()) return;

        for (ServerPlayerEntity player : players) {
            Optional<BattleResults> resultOpt = ev.getResult(player);
            if (!resultOpt.isPresent()) continue;
            // Solo premiamos al ganador
            if (resultOpt.get() != BattleResults.VICTORY) continue;

            if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) continue;

            // Calcular exp base
            int[] range = cfg.getExpRange();
            int base = randomInRange(range[0], range[1]);

            // VIP override
            Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
            if (vip != null) base = vip;

            // Amuleto de campeón
            boolean hasAmulet = player.inventory.items.stream()
                    .anyMatch(stack -> ChampionAmulet.ID.equals(RangerItemDefinition.getIdFromStack(stack)));
            if (hasAmulet) {
                ItemsConfig.ChampionAmuletConfig amCfg = ItemsConfig.get().championAmulet;
                int bonusXp = (int) Math.floor(base * (amCfg.xpPercent / 100.0));
                base += bonusXp;
                for (MysteryBoxesConfig.MysteryBoxConfig.CommandEntry entry : amCfg.commands) {
                    if (RNG.nextDouble() * 100 < entry.chancePercent) {
                        String cmd = entry.command.replace("%player%", player.getName().getString());
                        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
                        if (srv != null) {
                            srv.getCommands().performCommand(srv.createCommandSourceStack(), cmd);
                        }
                    }
                }
            }
            // ---- SELLO REFLEJO DEL MAISTRO
            double chance = SelloReflejoMaestroHandler.getChanceForPlayer(player);
            if (chance > 0) {
                if (new Random().nextDouble() < chance) {
                    base *= 2;
                }
            }

            base = FragmentoIraAncestralHandler.applyBonusIfApplicable(player, null, base);

            // ——— AÑADIDO: bonus de Bandera de Batalla ———
            if (BattleBannerHandler.isInControlledArea(player)) {
                base = (int) Math.round(base * 1.5);
            }
            // Multiplicadores (SIN GEMAS)
            int total = applyMultipliers(player, base, "beatPlayer");
            int totalConManual = ManualTrainingHandler.applyBonus(player, total);
            // Dar exp con límite
            LimiterHelper.giveExpWithLimit(player, totalConManual);
            // Intento de drop de caja de misterio
            ServerWorld world = (ServerWorld) player.level;
            BlockPos pos      = player.blockPosition();
            MysteryBoxHelper.tryDropOneOnEvent(
                    player,
                    MysteryBoxHelper.EventType.BEAT_BOSS,
                    world,
                    pos,
                    MysteryBoxesConfig.get().mysteryBox.comun
            );
        }
    }
}
