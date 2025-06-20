// File: rl/sage/rangerlevels/config/ShopRotationTicker.java
package rl.sage.rangerlevels.config;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID)
public class ShopRotationTicker {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ShopRotationManager.rotateIfDue();
    }
}
