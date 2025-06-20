package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.util.LazyOptional;

import static rl.sage.rangerlevels.capability.PassCapabilityProvider.PASS_CAP;

/**
 * Utilidad para obtener la capability de pase de un jugador (solo servidor).
 */
public class PassCapabilities {
    /**
     * Obtiene la capability de pase de un jugador (servidor).
     *
     * @param player instancia de ServerPlayerEntity
     * @return IPassCapability adjunta al jugador
     * @throws IllegalStateException si por alguna razón no está presente.
     */
    @Deprecated
    public static IPassCapability get(ServerPlayerEntity player) {
        LazyOptional<IPassCapability> opt = player.getCapability(PASS_CAP);
        return opt.orElseThrow(() ->
                new IllegalStateException("PassCapability no adjunta al jugador: " + player.getName().getString())
        );
    }
    public static LazyOptional<IPassCapability> getOptional(ServerPlayerEntity player) {
        return player.getCapability(PASS_CAP, null);
    }
}
