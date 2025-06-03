package rl.sage.rangerlevels.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Storage handler para IPassCapability: persiste cuantos datos necesitamos (tier + expiración).
 */
public class PassStorage implements Capability.IStorage<IPassCapability> {

    @Override
    public CompoundNBT writeNBT(Capability<IPassCapability> capability,
                                IPassCapability instance,
                                Direction side) {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("PassTier", instance.getTier());
        tag.putLong("PassExpiresAt", instance.getExpiresAt());
        return tag;
    }

    @Override
    public void readNBT(Capability<IPassCapability> capability,
                        IPassCapability instance,
                        Direction side,
                        INBT nbt) {
        if (!(nbt instanceof CompoundNBT)) {
            return;
        }
        CompoundNBT tag = (CompoundNBT) nbt;
        instance.setTier(tag.getInt("PassTier"));
        instance.setExpiresAt(tag.getLong("PassExpiresAt"));
    }
}
