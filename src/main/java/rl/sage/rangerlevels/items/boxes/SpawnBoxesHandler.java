// File: src/main/java/rl/sage/rangerlevels/items/boxes/SpawnBoxesHandler.java
package rl.sage.rangerlevels.items.boxes;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.network.play.server.STitlePacket.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import rl.sage.rangerlevels.config.MysteryBoxesConfig;
import rl.sage.rangerlevels.config.MysteryBoxesConfig.SpawnBoxesConfig;
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpawnBoxesHandler {

    private static final Random RNG = new Random();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (event.phase != ServerTickEvent.Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        SpawnBoxesConfig cfg = MysteryBoxesConfig.get().spawnBoxes;
        ServerWorld primary = server.overworld();
        long now = primary.getGameTime();

        // Por cada dimensión cargada:
        for (ServerWorld world : server.getAllLevels()) {
            UnclaimedBoxesData data = UnclaimedBoxesData.get(world);
            Iterator<UnclaimedBoxesData.Entry> it = data.getEntries().iterator();
            while (it.hasNext()) {
                UnclaimedBoxesData.Entry entry = it.next();
                // Comprobar si coincide la dimensión:
                if (!entry.dimension.equals(world.dimension())) continue;
                if (now - entry.spawnTick < cfg.unclaimedMinutes * 60L * 20L) continue;
                BlockPos pos = entry.pos;
                if (world.isLoaded(pos)) {
                    world.removeBlock(pos, false);
                }
                ServerPlayerEntity owner = server.getPlayerList().getPlayer(entry.ownerUuid);
                if (owner != null) {
                    owner.sendMessage(
                            new StringTextComponent(TextFormatting.RED + "No encontraste la caja a tiempo. Desapareció."),
                            owner.getUUID()
                    );
                    PlayerSoundUtils.playSoundToPlayer(owner, SoundEvents.ITEM_PICKUP, SoundCategory.MASTER, 1.f, 0.8f);
                }
                it.remove();
                data.setDirty();
            }
        }


        // 2) Spawn periódico
        long intervalTicks = cfg.intervalMinutes * 60L * 20L;
        SpawnBoxesTimerData timerData = SpawnBoxesTimerData.get(primary);
        long last = timerData.getLastSpawnCheck();
        if (now - last < intervalTicks) return;
        timerData.setLastSpawnCheck(now);

        // 3) Tirada global
        if (RNG.nextDouble() * 100.0 >= cfg.globalSpawnChance) return;

        // 4) Elegir jugador y mundo
        List<ServerPlayerEntity> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;
        ServerPlayerEntity player = players.get(RNG.nextInt(players.size()));
        ServerWorld world = (ServerWorld) player.level;

        // 5) Elegir tier
        String tierKey = weightedPick(cfg.tierWeights);
        if (tierKey == null) return;

        // 6) Posición aleatoria con bloque solido
        double ang = RNG.nextDouble() * Math.PI * 2;
        double dx  = Math.cos(ang) * cfg.spawnRadius;
        double dz  = Math.sin(ang) * cfg.spawnRadius;
        double x   = player.getX() + dx;
        double z   = player.getZ() + dz;

        int yInt = world.getHeight(Heightmap.Type.MOTION_BLOCKING,
                (int)Math.floor(x),
                (int)Math.floor(z));
        BlockPos pos = new BlockPos(x, yInt, z);

        // 7) Determinar ID y bloque a colocar
        String boxId;
        boolean useEnder;
        switch (tierKey.toLowerCase()) {
            case "comun":
                boxId = MysteryBoxComun.ID;
                useEnder = false;
                break;
            case "raro":
                boxId = MysteryBoxRaro.ID;
                useEnder = false;
                break;
            case "epico":
                boxId = MysteryBoxEpico.ID;
                useEnder = false;
                break;
            case "legendario":
                boxId = MysteryBoxLegendario.ID;
                useEnder = true;
                break;
            case "estelar":
                boxId = MysteryBoxEstelar.ID;
                useEnder = true;
                break;
            case "mitico":
                boxId = MysteryBoxMitico.ID;
                useEnder = true;
                break;
            default:
                return;
        }

        // 8) Colocar y etiquetar
        if (useEnder) {
            world.setBlock(pos, Blocks.ENDER_CHEST.defaultBlockState(), 3);
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof EnderChestTileEntity) {
                CompoundNBT data = ((EnderChestTileEntity) te).getTileData();
                data.putString("RangerBoxID", boxId);
                data.putUUID("RangerBoxOwner", player.getUUID());
            }
        } else {
            world.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof ChestTileEntity) {
                CompoundNBT data = ((ChestTileEntity) te).getTileData();
                data.putString("RangerBoxID", boxId);
                data.putUUID("RangerBoxOwner", player.getUUID());
            }
        }
        MeteorImpactEffects.spawnMeteorImpact(world, pos);

        // Guardamos en el mapa de no reclamados
        UnclaimedBoxesData data = UnclaimedBoxesData.get(world);
        data.addEntry(world.dimension(), pos, now, player.getUUID());

        // 9) Efectos
        world.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE,
                SoundCategory.MASTER, 2.5f, 1.0f);
        // Programar el bloque original de efectos para 40 ticks después (2 segundos)
        DelayedTaskScheduler.schedule(() -> {
            // 9) Efectos (idéntico a antes, sin modificar):
            ItemStack fw = makeFirework(tierKey);
            FireworkRocketEntity rocket = new FireworkRocketEntity(world, x+0.5, yInt+1, z+0.5, fw);
            world.addFreshEntity(rocket);
            world.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundCategory.MASTER, 2.5f, 0.5f);
            PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.ENDER_DRAGON_DEATH, SoundCategory.MASTER, 0.5f, 0.5f);
            PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.GENERIC_EXPLODE, SoundCategory.MASTER, 1.0f, 0.5f);
        }, 20);

        // 10) Broadcast
        IFormattableTextComponent msg = BroadcastMessageUtil.getBroadcast(player, pos);
        server.getPlayerList().broadcastMessage(msg, ChatType.SYSTEM, Util.NIL_UUID);

        // 11) Títulos
        player.connection.send(new STitlePacket(Type.TITLE,
                new StringTextComponent(TextFormatting.GOLD + "¡Caja Misteriosa!"), 10,70,20));
        player.connection.send(new STitlePacket(Type.SUBTITLE,
                new StringTextComponent(TextFormatting.YELLOW + "¡Encuéntrala ahora!"), 10,70,20));
    }
    @Mod.EventBusSubscriber(modid = "rangerlevels")
    public static class DelayedTaskScheduler {
        private static class Task {
            Runnable action;
            int ticksRemaining;
            Task(Runnable a, int t) { action = a; ticksRemaining = t; }
        }
        private static final java.util.List<Task> tasks = new java.util.ArrayList<>();

        /** Programa una acción a ejecutar tras delayTicks ticks */
        public static void schedule(Runnable action, int delayTicks) {
            tasks.add(new Task(action, delayTicks));
        }

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent ev) {
            if (ev.phase != TickEvent.Phase.END) return;
            java.util.Iterator<Task> it = tasks.iterator();
            while (it.hasNext()) {
                Task t = it.next();
                t.ticksRemaining--;
                if (t.ticksRemaining <= 0) {
                    try {
                        t.action.run();
                    } catch (Exception e) {
                        // opcional: log error
                    }
                    it.remove();
                }
            }
        }
    }

    private static ItemStack makeFirework(String tier) {
        ItemStack fw = new ItemStack(Items.FIREWORK_ROCKET, 1);
        CompoundNBT root = new CompoundNBT();
        CompoundNBT fb   = new CompoundNBT();

        int color;
        switch (tier.toLowerCase()) {
            case "comun":      color = 0xE2E2E2; break;
            case "raro":       color = 0x84C1E0; break;
            case "epico":      color = 0xA346FF; break;
            case "legendario": color = 0xFFD700; break;
            case "estelar":    color = 0x6EE3DC; break;
            default:           color = 0x38EF7D; break;
        }

        CompoundNBT effect = new CompoundNBT();
        effect.putIntArray("Colors", new int[]{ color });
        effect.putByte("Type", (byte)1);

        ListNBT explosions = new ListNBT();
        explosions.add(effect);

        fb.put("Explosions", explosions);
        fb.putByte("Flight", (byte)1);
        root.put("Fireworks", fb);
        fw.setTag(root);
        return fw;
    }

    private static String weightedPick(Map<String, Double> weights) {
        double total = 0;
        for (double w : weights.values()) total += w;
        if (total <= 0) return null;
        double roll = RNG.nextDouble() * total, cum = 0;
        for (Map.Entry<String, Double> e : weights.entrySet()) {
            cum += e.getValue();
            if (roll <= cum) return e.getKey();
        }
        return null;
    }
}
