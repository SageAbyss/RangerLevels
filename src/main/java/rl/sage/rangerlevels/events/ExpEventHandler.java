package rl.sage.rangerlevels.events;

import com.pixelmonmod.pixelmon.api.registries.PixelmonBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.Logger;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.config.ConfigLoader;
import rl.sage.rangerlevels.config.EventConfig;
import rl.sage.rangerlevels.config.ExpConfig;
import org.apache.logging.log4j.LogManager;
import rl.sage.rangerlevels.config.SpecificRangePermissions;
import rl.sage.rangerlevels.limiter.LimiterHelper;
import rl.sage.rangerlevels.multiplier.MultiplierManager;
import rl.sage.rangerlevels.pass.PassManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExpEventHandler {

    private static final Set<BlockPos> PLACED = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Random RNG = new Random();

    public static void register() {
        MinecraftForge.EVENT_BUS.register(ExpEventHandler.class);
    }

    private static int randomInRange(int min, int max) {
        return min + RNG.nextInt(max - min + 1);
    }

    private static int applyMultipliers(ServerPlayerEntity player, int baseExp, String eventKey) {
        double eventMul = ExpConfig.get().getEventMultiplier(eventKey);
        double global   = MultiplierManager.instance().getGlobal();
        double personal = MultiplierManager.instance().getPlayer(player);

        // Nuevo: multiplicador por pase
        double passMultiplier;
        switch (PassManager.getPass(player)) {
            case SUPER:  passMultiplier = 1.25; break;
            case ULTRA:  passMultiplier = 1.5;  break;
            case MASTER: passMultiplier = 2.0;  break;
            default:     passMultiplier = 1.0;
        }

        return (int) Math.round(baseExp * eventMul * global * personal * passMultiplier);
    }


    private static boolean isOp(ServerPlayerEntity player) {
        return player.getServer() != null
                && player.getServer().getPlayerList().isOp(player.getGameProfile());
    }

    private static boolean hasAnyPermission(ServerPlayerEntity player, List<String> perms) {
        if (player == null) return false;
        if (isOp(player)) return true;
        if (PermissionAPI.hasPermission(player, "rangerlevels.admin")) return true;
        if (perms != null) {
            for (String perm : perms) {
                if (PermissionAPI.hasPermission(player, perm)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Integer getVipRangeExp(ServerPlayerEntity player, SpecificRangePermissions vip) {
        if (player == null || vip == null || !vip.isEnable()) return null;
        try {
            if (isOp(player) || PermissionAPI.hasPermission(player, "rangerlevels.admin")) {
                List<String> list = vip.getPermissions();
                if (!list.isEmpty()) {
                    String last = list.get(list.size() - 1);
                    String range = last.substring(last.lastIndexOf('.') + 1);
                    if (range.contains("-")) {
                        String[] nums = range.split("-");
                        return randomInRange(Integer.parseInt(nums[0]), Integer.parseInt(nums[1]));
                    }
                }
                return null;
            }
            for (String perm : vip.getPermissions()) {
                if (PermissionAPI.hasPermission(player, perm)) {
                    String range = perm.substring(perm.lastIndexOf('.') + 1);
                    if (range.contains("-")) {
                        String[] nums = range.split("-");
                        return randomInRange(Integer.parseInt(nums[0]), Integer.parseInt(nums[1]));
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String getWorldName(ServerWorld world) {
        return world.dimension().location().getPath();
    }

    @SubscribeEvent
    public static void onBlockPlace(EntityPlaceEvent ev) {
        if (ev.getEntity() instanceof ServerPlayerEntity) {
            PLACED.add(ev.getPos());
        }
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent ev) {
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.getPlayer();
        ServerWorld world = (ServerWorld) player.level;
        String worldName = getWorldName(world);
        if (!ExpConfig.isWorldAllowed(worldName)) return;

        EventConfig cfg = ConfigLoader.CONFIG.getMinecraftEvents().get("itemFished");
        if (cfg == null || !cfg.isEnable()) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;

        for (ItemStack stack : ev.getDrops()) {
            String name = Objects.requireNonNull(stack.getItem().getRegistryName()).getPath();
            if ("salmon".equals(name) || "pufferfish".equals(name) || "enchanted_book".equals(name)
                    || "nautilus_shell".equals(name) || "bowl".equals(name) || "saddle".equals(name)
                    || "name_tag".equals(name) || "tropical_fish".equals(name)) {

                int[] range = cfg.getExpRange();
                int base = randomInRange(range[0], range[1]);
                Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
                if (vip != null) base = vip;

                LimiterHelper.giveExpWithLimit(
                        player,
                        applyMultipliers(player, base, "itemFished")
                );
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent ev) {
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.getPlayer();
        ServerWorld world = (ServerWorld) player.level;
        String worldName = getWorldName(world);
        if (!ExpConfig.isWorldAllowed(worldName)) return;

        BlockPos pos = ev.getPos();
        if (PLACED.remove(pos)) return;

        Block block = ev.getState().getBlock();
        String key;
        if (block instanceof CropsBlock) {
            CropsBlock cb = (CropsBlock) block;
            if (ev.getState().getValue(cb.getAgeProperty()) < cb.getMaxAge()) return;
            key = "cropBreak";
        } else if (block == Blocks.SPAWNER) {
            key = "spawnerBreak";
        } else if (ev.getState().is(BlockTags.LOGS)) {
            key = "logBreak";
        } else if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
            key = "melonPumpkinBreak";
        } else if (block == Blocks.COAL_ORE) {
            key = "coalOreBreak";
        } else if (block == Blocks.IRON_ORE) {
            key = "ironOreBreak";
        } else if (block == Blocks.DIAMOND_ORE || block == Blocks.EMERALD_ORE) {
            key = "diamondEmeraldBreak";
            // --- Pixelmon gem ores ---
        } else if (block == PixelmonBlocks.ruby_ore
                || block == PixelmonBlocks.sapphire_ore
                || block == PixelmonBlocks.amethyst_ore
                || block == PixelmonBlocks.crystal_ore
                || block == PixelmonBlocks.silicon_ore) {
            key = "pixelmonGemOreBreak";

// --- Pixelmon metal ores ---
        } else if (block == PixelmonBlocks.bauxite_ore
                || block == PixelmonBlocks.silver_ore
                || block == PixelmonBlocks.platinum_ore) {
            key = "pixelmonMetalOreBreak";

// --- Pixelmon evolution stone ores ---
        } else if (block == PixelmonBlocks.moon_stone_ore
                || block == PixelmonBlocks.fire_stone_ore
                || block == PixelmonBlocks.water_stone_ore
                || block == PixelmonBlocks.thunder_stone_ore
                || block == PixelmonBlocks.leaf_stone_ore
                || block == PixelmonBlocks.shiny_stone_ore
                || block == PixelmonBlocks.sun_stone_ore
                || block == PixelmonBlocks.ice_stone_ore
                || block == PixelmonBlocks.dusk_stone_ore
                || block == PixelmonBlocks.dawn_stone_ore) {
            key = "pixelmonEvolutionOreBreak";
        } else {
            return;
        }

        EventConfig cfg = ConfigLoader.CONFIG.getMinecraftEvents().get(key);
        if (cfg == null || !cfg.isEnable()) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(player, cfg.getPermissions())) return;

        int[] range = cfg.getExpRange();
        int base = randomInRange(range[0], range[1]);
        Integer vip = getVipRangeExp(player, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;

        LimiterHelper.giveExpWithLimit(
                player,
                applyMultipliers(player, base, key)
        );
    }

    @SubscribeEvent
    public static void onPlayerKill(LivingDeathEvent ev) {
        Entity src = ev.getSource().getEntity();
        if (!(src instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity killer = (ServerPlayerEntity) src;
        ServerWorld world = (ServerWorld) killer.level;
        String worldName = getWorldName(world);
        if (!ExpConfig.isWorldAllowed(worldName)) return;

        String key = "playerKill";
        EventConfig cfg = ConfigLoader.CONFIG.getMinecraftEvents().get(key);
        if (cfg == null || !cfg.isEnable()) return;
        if (cfg.isRequiresPermission() && !hasAnyPermission(killer, cfg.getPermissions())) return;

        int[] range = cfg.getExpRange();
        int base = randomInRange(range[0], range[1]);
        Integer vip = getVipRangeExp(killer, cfg.getSpecificRangePermissions());
        if (vip != null) base = vip;

        LimiterHelper.giveExpWithLimit(
                killer,
                applyMultipliers(killer, base, key)
        );
    }

    // Puedes repetir el mismo patrón en otros métodos de este handler
}
