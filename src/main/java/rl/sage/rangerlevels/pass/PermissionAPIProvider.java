package rl.sage.rangerlevels.pass;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.server.permission.PermissionAPI;

/**
 * Proveedor de permisos basado en Forge PermissionAPI.
 * Implementa IPermissionProvider para desacoplar la lógica de permisos.
 */
public class PermissionAPIProvider implements IPermissionProvider {

    /**
     * Comprueba si el jugador tiene el permiso especificado,
     * delegando en PermissionAPI.hasPermission().
     *
     * @param player el jugador en el servidor
     * @param node   la cadena de permiso (e.g. "rangerlevels.pass.super")
     * @return true si tiene el permiso, false en caso contrario
     */
    @Override
    public boolean hasPermission(ServerPlayerEntity player, String node) {
        return PermissionAPI.hasPermission(player, node);
    }
}
