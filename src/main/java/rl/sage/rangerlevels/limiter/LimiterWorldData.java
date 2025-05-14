 package rl.sage.rangerlevels.limiter;

 import net.minecraft.nbt.CompoundNBT;
 import net.minecraft.world.server.ServerWorld;
 import net.minecraft.world.storage.WorldSavedData;

 public class LimiterWorldData extends WorldSavedData {
     public static final String DATA_NAME = "rangerlevels_limiter_data";
     private long nextResetTime = 0L;

     public LimiterWorldData() {
         super(DATA_NAME);
     }

     public long getNextResetTime() {
         return nextResetTime;
     }

     public void setNextResetTime(long time) {
         this.nextResetTime = time;
         setDirty(); // Marca los datos como modificados para que se guarden
     }

     public static LimiterWorldData get(ServerWorld world) {
         return world.getChunkSource()
                 .getDataStorage()
                 .computeIfAbsent(LimiterWorldData::new, DATA_NAME);
     }

     @Override
     public void load(CompoundNBT nbt) {
         this.nextResetTime = nbt.getLong("NextResetTime");
     }

     @Override
     public CompoundNBT save(CompoundNBT compound) {
         compound.putLong("NextResetTime", nextResetTime);
         return compound;
     }
 }