// RewardStorage.java
package rl.sage.rangerlevels.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class RewardStorage implements IStorage<IPlayerRewards> {

    @Override
    public INBT writeNBT(Capability<IPlayerRewards> capability, IPlayerRewards instance, Direction side) {
        CompoundNBT nbt = new CompoundNBT();
        // Serializamos cada entrada del mapa como string
        for (String key : instance.getStatusMap().keySet()) {
            RewardStatus status = instance.getStatusMap().get(key);
            nbt.putString(key, status.name());
        }
        return nbt;
    }

    @Override
    public void readNBT(Capability<IPlayerRewards> capability, IPlayerRewards instance, Direction side, INBT inbt) {
        CompoundNBT nbt = (CompoundNBT) inbt;
        // En Forge 1.16.5 usamos getAllKeys() en lugar de keySet()
        for (String key : nbt.getAllKeys()) {
            String name = nbt.getString(key);
            try {
                RewardStatus status = RewardStatus.valueOf(name);
                instance.setStatus(key, status);
            } catch (IllegalArgumentException e) {
                // En caso de dato corrupto, ignora
            }
        }
    }
}
