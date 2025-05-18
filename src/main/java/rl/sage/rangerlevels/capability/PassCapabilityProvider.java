package rl.sage.rangerlevels.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;

/**
 * Adjunta la capability de pase a cada ServerPlayerEntity.
 */
@Mod.EventBusSubscriber(modid = RangerLevels.MODID)
public class PassCapabilityProvider {
    @CapabilityInject(IPassCapability.class)
    public static Capability<IPassCapability> PASS_CAP = null;

    private static final ResourceLocation ID = new ResourceLocation(RangerLevels.MODID, "pass");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof ServerPlayerEntity)) {
            return;
        }
        Provider provider = new Provider();
        event.addCapability(ID, provider);
        event.addListener(provider::invalidate);
    }

    private static class Provider implements ICapabilitySerializable<CompoundNBT> {
        private final LazyOptional<IPassCapability> instance =
                LazyOptional.of(() -> PASS_CAP.getDefaultInstance());

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap == PASS_CAP ? instance.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundNBT serializeNBT() {
            // Guarda el estado en NBT usando el storage de la capability
            return (CompoundNBT) PASS_CAP.getStorage()
                    .writeNBT(PASS_CAP, instance.orElseThrow(() ->
                            new IllegalStateException("PassCapability ausente al serializar")), null);
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            // Restaura el estado desde NBT
            PASS_CAP.getStorage()
                    .readNBT(PASS_CAP, instance.orElseThrow(() ->
                            new IllegalStateException("PassCapability ausente al deserializar")), null, nbt);
        }

        public void invalidate() {
            instance.invalidate();
        }
    }
}
