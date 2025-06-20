// src/main/java/rl/sage/rangerlevels/capability/LimiterProvider.java
package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import rl.sage.rangerlevels.RangerLevels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rl.sage.rangerlevels.limiter.LimiterManager;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LimiterProvider {
    private static final Logger LOG = LogManager.getLogger();
    private static final ResourceLocation ID = new ResourceLocation(RangerLevels.MODID, "limiter");

    @CapabilityInject(ILimiter.class)
    public static Capability<ILimiter> LIMITER_CAP = null;

    /** Llamar en tu FMLCommonSetupEvent */
    public static void register() {
        CapabilityManager.INSTANCE.register(
                ILimiter.class,
                new LimiterStorage(),
                LimiterCapability::new    // ← ahora usa tu clase externa
        );
        LOG.info("[Limiter] Capability registrada (external)");
    }

    /** Acceso cómodo */
    public static LazyOptional<ILimiter> get(PlayerEntity player) {
        return player.getCapability(LIMITER_CAP, null);
    }

    /** Copiar datos al respawnear */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone ev) {
        if (!ev.isWasDeath()) return;
        get(ev.getOriginal()).ifPresent(oldCap ->
                get(ev.getPlayer()).ifPresent(newCap -> {
                    newCap.setWindowStart(oldCap.getWindowStart());
                    newCap.setAccumulatedExp(oldCap.getAccumulatedExp());
                    newCap.setNotified(oldCap.wasNotified());
                })
        );
    }
    /** Cada tick de servidor chequeamos reseteo global */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent ev) {
        if (ev.phase == TickEvent.Phase.END) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                LimiterManager.checkGlobalReset(server);
            }
        }
    }

    /** Storage NBT */
    public static class LimiterStorage implements Capability.IStorage<ILimiter> {
        @Override
        public INBT writeNBT(Capability<ILimiter> cap, ILimiter inst, Direction side) {
            CompoundNBT tag = new CompoundNBT();
            tag.putLong("windowStart", inst.getWindowStart());
            tag.putInt("accumulatedExp", inst.getAccumulatedExp());
            tag.putBoolean("notified", inst.wasNotified());
            return tag;
        }
        @Override
        public void readNBT(Capability<ILimiter> cap, ILimiter inst,
                            Direction side, INBT inbt) {
            if (!(inbt instanceof CompoundNBT)) return;
            CompoundNBT tag = (CompoundNBT) inbt;
            inst.setWindowStart(tag.getLong("windowStart"));
            inst.setAccumulatedExp(tag.getInt("accumulatedExp"));
            inst.setNotified(tag.getBoolean("notified"));
        }
    }
}
