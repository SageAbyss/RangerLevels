// src/main/java/rl/sage/rangerlevels/pass/TicketExpirationListener.java
package rl.sage.rangerlevels.pass;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.util.LazyOptional;
import rl.sage.rangerlevels.capability.IPassCapability;
import rl.sage.rangerlevels.capability.PassCapabilities;

@Mod.EventBusSubscriber(modid = rl.sage.rangerlevels.RangerLevels.MODID)
public class TicketExpirationListener {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        // Solo fase END y solo en servidor
        if (event.phase != PlayerTickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.player;
        LazyOptional<IPassCapability> capOpt = PassCapabilities.getOptional(player);

        capOpt.ifPresent(cap -> {
            // Aquí solo se ejecuta si la capability existe
            PassUtil.checkAndRestorePass(player, cap);
        });
    }
}
