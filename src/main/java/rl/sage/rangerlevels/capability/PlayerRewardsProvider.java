// PlayerRewardsProvider.java
package rl.sage.rangerlevels.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlayerRewardsProvider implements ICapabilitySerializable<INBT> {

    // Injectamos la instancia de la capability
    @CapabilityInject(IPlayerRewards.class)
    public static Capability<IPlayerRewards> REWARDS_CAP = null;

    // LazyOptional que expone nuestra implementación
    private final LazyOptional<IPlayerRewards> instance = LazyOptional.of(PlayerRewardsData::new);

    /** Registra la capability (llámalo desde tu setup en FMLCommonSetupEvent) */
    public static void register() {
        CapabilityManager.INSTANCE.register(
                IPlayerRewards.class,
                new RewardStorage(),
                PlayerRewardsData::new
        );
    }

    /** Adjunta el provider a cada PlayerEntity */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof PlayerEntity)) return;
        event.addCapability(
                new ResourceLocation("rangerlevels", "player_rewards"),
                new PlayerRewardsProvider()
        );
    }


    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == REWARDS_CAP) {
            return instance.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        // Delegamos al storage registrado
        return REWARDS_CAP.getStorage().writeNBT(REWARDS_CAP, instance.orElseThrow(
                () -> new IllegalStateException("IPlayerRewards not present")
        ), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        REWARDS_CAP.getStorage().readNBT(REWARDS_CAP, instance.orElseThrow(
                () -> new IllegalStateException("IPlayerRewards not present")
        ), null, nbt);
    }
}
