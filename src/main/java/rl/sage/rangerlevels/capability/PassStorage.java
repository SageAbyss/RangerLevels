package rl.sage.rangerlevels.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

/**
 * Storage handler for IPassCapability, persiste el entero "PassTier" en NBT.
 */
public class PassStorage implements IStorage<IPassCapability> {

    @Override
    public CompoundNBT writeNBT(Capability<IPassCapability> capability,
                                IPassCapability instance,
                                Direction side) {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("PassTier", instance.getTier());
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
    }
}
