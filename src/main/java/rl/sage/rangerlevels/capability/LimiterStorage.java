// LimiterStorage.java
package rl.sage.rangerlevels.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class LimiterStorage implements Capability.IStorage<ILimiter> {

    @Override
    public INBT writeNBT(Capability<ILimiter> capability, ILimiter instance, Direction side) {
        CompoundNBT tag = new CompoundNBT();
        tag.putLong("windowStart", instance.getWindowStart());
        tag.putInt("accumulatedExp", instance.getAccumulatedExp());
        return tag;
    }

    @Override
    public void readNBT(Capability<ILimiter> capability, ILimiter instance, Direction side, INBT inbt) {
        if (!(inbt instanceof CompoundNBT)) return;
        CompoundNBT tag = (CompoundNBT) inbt;

        long ws = tag.getLong("windowStart");
        int ae = tag.getInt("accumulatedExp");

        // Restauramos ambos valores
        instance.setWindowStart(ws);
        instance.setAccumulatedExp(ae);
    }
}
