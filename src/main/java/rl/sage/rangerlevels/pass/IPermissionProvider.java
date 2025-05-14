package rl.sage.rangerlevels.pass;

import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * Interfaz que abstrae el sistema de permisos para RangerLevels.
 * Implementaciones concretas (p. ej. usando PermissionAPI) deberán
 * proporcionar la lógica de comprobación de permisos aquí.
 */
public interface IPermissionProvider {

    /**
     * Comprueba si el jugador tiene el permiso indicado.
     *
     * @param player  el jugador en el servidor
     * @param node    la cadena de permiso (p.ej. "rangerlevels.pass.super")
     * @return        true si el jugador tiene ese permiso, false en caso contrario
     */
    boolean hasPermission(ServerPlayerEntity player, String node);
}
