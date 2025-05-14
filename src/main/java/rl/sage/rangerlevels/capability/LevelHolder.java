// LevelHolder.java
package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LevelHolder implements ICapabilityProvider, ICapabilitySerializable<CompoundNBT> {
    private final ILevel instance = new LevelCapability();
    private final LazyOptional<ILevel> optional = LazyOptional.of(() -> instance);

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCapability(
            @Nonnull Capability<T> cap, @Nullable Direction side) {
        return LevelProvider.LEVEL_CAP.orEmpty(cap, optional);
    }

    @Override
    public CompoundNBT serializeNBT() {
        return (CompoundNBT) LevelProvider.LEVEL_CAP
                .getStorage()
                .writeNBT(LevelProvider.LEVEL_CAP, instance, null);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        LevelProvider.LEVEL_CAP
                .getStorage()
                .readNBT(LevelProvider.LEVEL_CAP, instance, null, nbt);
    }
}
