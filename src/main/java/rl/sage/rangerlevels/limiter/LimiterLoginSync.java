package rl.sage.rangerlevels.limiter;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.capability.LimiterProvider;
import rl.sage.rangerlevels.RangerLevels;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LimiterLoginSync {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent ev) {
        // reinicia silenciosamente la ventana para que esté “a tiempo”
        long now = System.currentTimeMillis() / 1_000L;
        LimiterProvider.get(ev.getPlayer()).ifPresent(limiter -> {
            limiter.setWindowStart(now);
            limiter.setAccumulatedExp(0);
            limiter.setNotified(false);
        });
    }
}
