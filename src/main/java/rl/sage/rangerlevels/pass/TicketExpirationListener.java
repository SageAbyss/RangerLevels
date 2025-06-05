package rl.sage.rangerlevels.pass;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Cada tick verifica si el pase del jugador expiró. Si expiró,
 * PassUtil.checkAndRestorePass() se encarga de restaurarlo y notificar.
 */
@Mod.EventBusSubscriber(modid = rl.sage.rangerlevels.RangerLevels.MODID)
public class TicketExpirationListener {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        // Solo fase END, para no ejecutarse dos veces por tick
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayerEntity)) return;

        // Restaurar / notificar si expiró
        PassUtil.checkAndRestorePass((ServerPlayerEntity) event.player);
    }
}
