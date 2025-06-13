package rl.sage.rangerlevels.items;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class InvocationManager {
    private static final List<InvocationSession> SESSIONS = new CopyOnWriteArrayList<>();

    public static List<InvocationSession> getSessions() {
        // Podrías filtrar sólo las que !sess.isDone(), o eliminarlas al completar
        return Collections.unmodifiableList(SESSIONS);
    }
    public static void register(InvocationSession session) {
        SESSIONS.add(session);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END) return;
        for (InvocationSession sess : SESSIONS) {
            sess.tick();
            if (sess.isDone()) {
                SESSIONS.remove(sess);
            }
        }
    }
}
