package rl.sage.rangerlevels.events;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.*;
import com.pixelmonmod.pixelmon.api.events.battles.CatchComboEvent;
import com.pixelmonmod.pixelmon.api.events.legendary.ArceusEvent;
import com.pixelmonmod.pixelmon.api.events.legendary.TimespaceEvent;
import com.pixelmonmod.pixelmon.api.events.raids.StartRaidEvent;
import rl.sage.rangerlevels.capability.PassCapabilities;
import rl.sage.rangerlevels.pass.PassManager;
import com.pixelmonmod.pixelmon.api.events.PokedexEvent;
import com.pixelmonmod.pixelmon.api.pokedex.PokedexRegistrationStatus;
import com.pixelmonmod.pixelmon.api.pokemon.boss.BossTierRegistry;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.config.ConfigLoader;
import rl.sage.rangerlevels.config.EventConfig;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.config.SpecificRangePermissions;
import rl.sage.rangerlevels.limiter.LimiterHelper;
import rl.sage.rangerlevels.multiplier.MultiplierManager;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

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

        // 1. Saca el tier desde la capability y clampéalo
        int tier = PassCapabilities.get(player).getTier();
        PassManager.PassType[] types = PassManager.PassType.values();
        if (tier < 0 || tier >= types.length) tier = 0;
        PassManager.PassType pass = types[tier];

        // 2. Determina el multiplier según el pass
        double passMultiplier;
        switch (pass) {
            case SUPER:  passMultiplier = 1.25; break;
            case ULTRA:  passMultiplier = 1.5;  break;
            case MASTER: passMultiplier = 2.0;  break;
            default:     passMultiplier = 1.0;  break;
        }

        // 3. Devuelve directamente el cálculo redondeado
        return (int) Math.round(baseExp * eventMul * global * personal * passMultiplier);
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

        int base;
        boolean shiny = pkmn.getPokemon().isShiny();
        boolean legendary = pkmn.getPokemon().getSpecies().isLegendary()
                || pkmn.getPokemon().getSpecies().isUltraBeast();
        if (legendary) base = randomInRange(150, 300);
        else if (shiny) base = randomInRange(60, 120);
        else base = randomInRange(cfg.getExpRange()[0], cfg.getExpRange()[1]);

        Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;

        giveExp(player, base, "onCapture");
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
        handleGeneric(ev.getPlayer(), "eggHatch", EventConfig::getExpRange);
    }

    @SubscribeEvent
    public static void onEvolve(EvolveEvent.Post ev) {
        handleGeneric(ev.getPlayer(), "evolve", EventConfig::getExpRange);
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
        EventConfig cfg = getCfg("advancement");
        if (cfg == null || !cfg.isEnable()) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;

        Advancement adv = ev.getAdvancement();
        ResourceLocation id = adv.getId();
        String ns = id.getNamespace();
        if (!"pixelmon".equals(ns) && !"minecraft".equals(ns)) return;

        int base = randomInRange(cfg.getExpRange()[0], cfg.getExpRange()[1]);
        if ("minecraft".equals(ns)) base /= 2;

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
        if (defeated.isBossPokemon())            base = randomInRange(boss[0], boss[1]);
        else if (defeated.getPokemon().isLegendary()
                || defeated.getPokemon().getSpecies().isUltraBeast())
            base = randomInRange(legendary[0], legendary[1]);
        else if (defeated.getPokemon().isShiny()) base = randomInRange(shiny[0], shiny[1]);
        else                                     base = randomInRange(wild[0], wild[1]);

        Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;

        giveExp(player, base, "beatWild");
    }

    @SubscribeEvent
    public static void onBeatTrainer(BeatTrainerEvent ev) {
        ServerPlayerEntity player = ev.player;
        EventConfig cfg = getCfg("beatTrainer");
        if (cfg == null || !cfg.isEnable() || player == null) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;

        boolean isBoss = ev.trainer.getBossTier() != BossTierRegistry.NOT_BOSS;
        String key = isBoss ? "beatBoss" : "beatTrainer";
        handleGeneric(player, key, EventConfig::getExpRange);
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
}
