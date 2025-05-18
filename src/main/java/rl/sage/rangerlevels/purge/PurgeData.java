package rl.sage.rangerlevels.purge;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class PurgeData extends WorldSavedData {
    public static final String DATA_NAME = "rangerlevels_purge";

    private long remainingSeconds;
    private boolean reminderSent;
    private boolean purgeEnded;
    private long configTotalSeconds;

    public PurgeData() {
        super(DATA_NAME);
    }

    // Constructor adicional (1.16.5)
    public PurgeData(String name) {
        super(name);
    }

    @Override
    public void load(CompoundNBT nbt) {
        this.remainingSeconds   = nbt.getLong("remainingSeconds");
        this.reminderSent       = nbt.getBoolean("reminderSent");
        this.purgeEnded         = nbt.getBoolean("purgeEnded");
        this.configTotalSeconds = nbt.getLong("configTotalSeconds");
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putLong("remainingSeconds",   remainingSeconds);
        nbt.putBoolean("reminderSent",    reminderSent);
        nbt.putBoolean("purgeEnded",      purgeEnded);
        nbt.putLong("configTotalSeconds", configTotalSeconds);
        return nbt;
    }

    public long getRemainingSeconds()     { return remainingSeconds; }
    public void setRemainingSeconds(long s) { this.remainingSeconds = s; setDirty(); }

    public boolean isReminderSent()       { return reminderSent; }
    public void setReminderSent(boolean b) { this.reminderSent = b; setDirty(); }

    public boolean hasPurgeEnded()        { return purgeEnded; }
    public void setPurgeEnded(boolean b)  { this.purgeEnded = b; setDirty(); }

    public long getConfigTotalSeconds()   { return configTotalSeconds; }
    public void setConfigTotalSeconds(long s) { this.configTotalSeconds = s; setDirty(); }

    public static PurgeData get(ServerWorld world) {
        return world.getDataStorage().computeIfAbsent(PurgeData::new, DATA_NAME);
    }
}