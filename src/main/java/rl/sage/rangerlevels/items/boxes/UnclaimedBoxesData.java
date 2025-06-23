// File: rl/sage/rangerlevels/items/boxes/UnclaimedBoxesData.java
package rl.sage.rangerlevels.items.boxes;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnclaimedBoxesData extends WorldSavedData {
    public static final String DATA_NAME = "rangerlevels_unclaimed_boxes";

    // Entrada de caja no reclamada
    public static class Entry {
        public final RegistryKey<World> dimension;
        public final BlockPos pos;
        public final long spawnTick;
        public final UUID ownerUuid;
        public Entry(RegistryKey<World> dim, BlockPos pos, long spawnTick, UUID owner) {
            this.dimension = dim;
            this.pos = pos;
            this.spawnTick = spawnTick;
            this.ownerUuid = owner;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    public UnclaimedBoxesData() {
        super(DATA_NAME);
    }

    @Override
    public void load(CompoundNBT nbt) {
        entries.clear();
        ListNBT list = nbt.getList("Boxes", 10); // compound list
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT be = list.getCompound(i);
            String dimName = be.getString("Dimension");
            ResourceLocation dimRL = new ResourceLocation(dimName);
            // Asumimos que coincide con nombre de dimensión (como minecraft:overworld, etc.)
            @SuppressWarnings("unchecked")
            RegistryKey<World> dimKey = RegistryKey.create(RegistryKey.createRegistryKey(new ResourceLocation("world")), dimRL);
            // Nota: en práctica hay que resolver bien el RegistryKey<World> usando ServerWorld registry;
            // sin embargo en tick obtendremos ServerWorld por nombre con server.getLevel(...)
            int x = be.getInt("X");
            int y = be.getInt("Y");
            int z = be.getInt("Z");
            long spawnTick = be.getLong("SpawnTick");
            UUID owner = be.hasUUID("Owner") ? be.getUUID("Owner") : null;
            if (owner != null) {
                entries.add(new Entry(dimKey, new BlockPos(x, y, z), spawnTick, owner));
            }
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        ListNBT list = new ListNBT();
        for (Entry e : entries) {
            CompoundNBT be = new CompoundNBT();
            be.putString("Dimension", String.valueOf(e.dimension.getRegistryName()));
            be.putInt("X", e.pos.getX());
            be.putInt("Y", e.pos.getY());
            be.putInt("Z", e.pos.getZ());
            be.putLong("SpawnTick", e.spawnTick);
            be.putUUID("Owner", e.ownerUuid);
            list.add(be);
        }
        nbt.put("Boxes", list);
        return nbt;
    }

    public static UnclaimedBoxesData get(ServerWorld world) {
        DimensionSavedDataManager storage = world.getDataStorage();
        return storage.computeIfAbsent(UnclaimedBoxesData::new, DATA_NAME);
    }

    public List<Entry> getEntries() {
        return entries;
    }
    public void addEntry(RegistryKey<World> dim, BlockPos pos, long spawnTick, UUID owner) {
        entries.add(new Entry(dim, pos, spawnTick, owner));
        setDirty();
    }
    public void removeEntry(Entry e) {
        entries.remove(e);
        setDirty();
    }
}
