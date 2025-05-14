// CapabilityAttacher.java
package rl.sage.rangerlevels.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityAttacher {

    @SubscribeEvent
    public static void attachPlayerCaps(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof PlayerEntity)) {
            return;
        }

        // Nivel
        event.addCapability(
                new ResourceLocation(RangerLevels.MODID, "level"),
                new LevelHolder()
        );

        // Limiter
        event.addCapability(
                new ResourceLocation(RangerLevels.MODID, "limiter"),
                new LimiterHolder()
        );
    }
}
