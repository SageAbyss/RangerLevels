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
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
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

    // Map de cofres no reclamados → info con tick de spawn y dueño
    private static final Map<WorldPos, BoxInfo> unclaimed = new HashMap<>();

    // Último tick en que comprobamos spawn periódico
    private static long lastSpawnCheck = 0;

    // Clase auxiliar para dimensión + posición
    private static class WorldPos {
        final RegistryKey<World> dimension;
        final BlockPos pos;
        WorldPos(RegistryKey<World> dim, BlockPos pos) {
            this.dimension = dim;
            this.pos = pos;
        }
        @Override public int hashCode() { return Objects.hash(dimension, pos); }
        @Override public boolean equals(Object o) {
            if (!(o instanceof WorldPos)) return false;
            WorldPos w = (WorldPos) o;
            return dimension.equals(w.dimension) && pos.equals(w.pos);
        }
    }

    private static class BoxInfo {
        final long spawnTick;
        final UUID ownerUuid;
        BoxInfo(long spawnTick, UUID ownerUuid) {
            this.spawnTick = spawnTick;
            this.ownerUuid  = ownerUuid;
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (event.phase != ServerTickEvent.Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        SpawnBoxesConfig cfg = MysteryBoxesConfig.get().spawnBoxes;
        long now = server.getTickCount();

        // 1) Expirar cofres no reclamados en TODOS los mundos
        long timeoutTicks = cfg.unclaimedMinutes * 60L * 20L;
        Iterator<Map.Entry<WorldPos, BoxInfo>> it = unclaimed.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<WorldPos, BoxInfo> entry = it.next();
            WorldPos wp = entry.getKey();
            BoxInfo info = entry.getValue();
            if (now - info.spawnTick < timeoutTicks) continue;

            ServerWorld sw = server.getLevel(wp.dimension);
            if (sw != null && sw.isLoaded(wp.pos)) {
                sw.removeBlock(wp.pos, false);
            }
            ServerPlayerEntity owner = server.getPlayerList().getPlayer(info.ownerUuid);
            if (owner != null) {
                owner.sendMessage(
                        new StringTextComponent(TextFormatting.RED
                                + "No encontraste la caja a tiempo. Desapareció."),
                        owner.getUUID()
                );
                PlayerSoundUtils.playSoundToPlayer(owner, SoundEvents.ITEM_PICKUP, SoundCategory.MASTER, 1.f, 0.8f);

            }
            it.remove();
        }

        // 2) Spawn periódico
        long intervalTicks = cfg.intervalMinutes * 60L * 20L;
        if (now - lastSpawnCheck < intervalTicks) return;
        lastSpawnCheck = now;

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

        // Guardamos en el mapa de no reclamados
        unclaimed.put(new WorldPos(world.dimension(), pos),
                new BoxInfo(now, player.getUUID()));


        // 9) Efectos
        ItemStack fw = makeFirework(tierKey);
        FireworkRocketEntity rocket = new FireworkRocketEntity(world, x+0.5, yInt+1, z+0.5, fw);
        world.addFreshEntity(rocket);
        world.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundCategory.MASTER, 2.5f, 0.5f);
        PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.ENDER_DRAGON_DEATH, SoundCategory.MASTER, 0.5f, 0.5f);
        PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.GENERIC_EXPLODE, SoundCategory.MASTER, 1.0f, 0.5f);

        // 10) Broadcast
        String sep = TextFormatting.DARK_GRAY + "" + TextFormatting.STRIKETHROUGH
                + "                                                                      \n";
        IFormattableTextComponent msg = new StringTextComponent("")
                .append(new StringTextComponent(sep))
                .append(GradientText.of("¡Se estrelló una Caja Misteriosa cerca de "
                        + player.getName().getString() + "!\n", "#FF7F50", "#FFD700"))
                .append(new StringTextComponent("\n"))
                .append(GradientText.of("                          ["+
                        pos.getX()+","+pos.getY()+","+pos.getZ()+"]\n","#FF5151","#FF5151"))
                .append(new StringTextComponent(sep));
        server.getPlayerList().broadcastMessage(msg, ChatType.SYSTEM, Util.NIL_UUID);

        // 11) Títulos
        player.connection.send(new STitlePacket(Type.TITLE,
                new StringTextComponent(TextFormatting.GOLD + "¡Caja Misteriosa!"), 10,70,20));
        player.connection.send(new STitlePacket(Type.SUBTITLE,
                new StringTextComponent(TextFormatting.YELLOW + "¡Encuéntrala ahora!"), 10,70,20));
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
