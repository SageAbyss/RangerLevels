// src/main/java/rl/sage/rangerlevels/capability/LimiterHolder.java
package rl.sage.rangerlevels.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LimiterHolder implements ICapabilityProvider, ICapabilitySerializable<CompoundNBT> {
    // Ahora instanciamos la clase externa, no la anidada en el provider
    private final ILimiter instance = new LimiterCapability();
    private final LazyOptional<ILimiter> optional = LazyOptional.of(() -> instance);

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCapability(
            @Nonnull Capability<T> cap, @Nullable Direction side) {
        return LimiterProvider.LIMITER_CAP.orEmpty(cap, optional);
    }

    @Override
    public CompoundNBT serializeNBT() {
        return (CompoundNBT) LimiterProvider.LIMITER_CAP
                .getStorage()
                .writeNBT(LimiterProvider.LIMITER_CAP, instance, null);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        LimiterProvider.LIMITER_CAP
                .getStorage()
                .readNBT(LimiterProvider.LIMITER_CAP, instance, null, nbt);
    }
}
