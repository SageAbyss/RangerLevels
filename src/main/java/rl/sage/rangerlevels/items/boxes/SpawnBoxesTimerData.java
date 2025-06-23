// File: rl/sage/rangerlevels/items/boxes/SpawnBoxesTimerData.java
package rl.sage.rangerlevels.items.boxes;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class SpawnBoxesTimerData extends WorldSavedData {
    public static final String DATA_NAME = "rangerlevels_spawn_timer";

    private long lastSpawnCheck = 0;

    public SpawnBoxesTimerData() {
        super(DATA_NAME);
    }

    @Override
    public void load(CompoundNBT nbt) {
        // Leer el último tick guardado (0 si no existía)
        this.lastSpawnCheck = nbt.getLong("LastSpawnCheck");
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        // Guardar el último tick
        nbt.putLong("LastSpawnCheck", this.lastSpawnCheck);
        return nbt;
    }

    /** Obtiene (o crea) la instancia para este mundo */
    public static SpawnBoxesTimerData get(ServerWorld world) {
        DimensionSavedDataManager storage = world.getDataStorage();
        return storage.computeIfAbsent(SpawnBoxesTimerData::new, DATA_NAME);
    }

    public long getLastSpawnCheck() {
        return lastSpawnCheck;
    }

    public void setLastSpawnCheck(long tick) {
        this.lastSpawnCheck = tick;
        setDirty();
    }
}
