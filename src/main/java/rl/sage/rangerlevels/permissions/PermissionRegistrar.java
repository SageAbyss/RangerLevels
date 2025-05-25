package rl.sage.rangerlevels.permissions;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import rl.sage.rangerlevels.RangerLevels;

import java.util.function.Predicate;

public class PermissionRegistrar {

    // Nodos globales
    public static final String ADMIN       = "rangerlevels.command.admin";
    public static final String USER        = "rangerlevels.command.user";

    // Nodos de administración (OP por defecto)
    public static final String RELOAD        = "rangerlevels.command.reload";
    public static final String ADDEXP        = "rangerlevels.command.addexp";
    public static final String SETEXP        = "rangerlevels.command.setexp";
    public static final String REMOVEEXP     = "rangerlevels.command.removeexp";
    public static final String ADDLEVEL      = "rangerlevels.command.addlevel";
    public static final String SETLEVEL      = "rangerlevels.command.setlevel";
    public static final String REMOVELEVEL   = "rangerlevels.command.removelevel";
    public static final String RESET         = "rangerlevels.command.reset";
    public static final String SETMULTIPLIER = "rangerlevels.command.setmultiplier";
    public static final String PASS_SET      = "rangerlevels.command.pass.set";
    public static final String CLICK_EVENTO   = "rangerlevels.command.click_evento_1";

    // Nodos de usuario (ALL por defecto)
    public static final String HELP          = "rangerlevels.command.help";
    public static final String STATS         = "rangerlevels.command.stats";
    public static final String MULTIPLIERS   = "rangerlevels.command.multipliers";
    public static final String PASS_INFO     = "rangerlevels.command.pass.info";
    public static final String PASS_BUY      = "rangerlevels.command.pass.buy";
    public static final String MENU          = "rangerlevels.command.menu";
    public static final String REWARDS       = "rangerlevels.command.rewards";
    public static final String PURGA         = "rangerlevels.command.purga";

    /**
     * Registra todos los nodos de permiso.
     * Llamar en FMLCommonSetupEvent.
     */
    public static void registerAll() {
        // Grupos
        PermissionAPI.registerNode(ADMIN,
                DefaultPermissionLevel.OP,
                "Permite usar TODOS los comandos de administración");
        PermissionAPI.registerNode(USER,
                DefaultPermissionLevel.ALL,
                "Permite usar TODOS los comandos de usuario");

        // Administración
        PermissionAPI.registerNode(RELOAD,
                DefaultPermissionLevel.OP,
                "Permite usar /rlv reload");
        PermissionAPI.registerNode(ADDEXP,
                DefaultPermissionLevel.OP,
                "Permite usar /rlv addexp");
        PermissionAPI.registerNode(SETEXP,
                DefaultPermissionLevel.OP,
                "Permite usar /rlv setexp");
        PermissionAPI.registerNode(REMOVEEXP,
                DefaultPermissionLevel.OP,
                "Permite usar /rlv removeexp");
        PermissionAPI.registerNode(ADDLEVEL,
                DefaultPermissionLevel.OP,
                "Permite usar /rlv addlevel");
        PermissionAPI.registerNode(SETLEVEL,
                DefaultPermissionLevel.OP,
                "Permite usar /rlv setlevel");
        PermissionAPI.registerNode(REMOVELEVEL,
                DefaultPermissionLevel.OP,
                "Permite usar /rlv removelevel");
        PermissionAPI.registerNode(RESET,
                DefaultPermissionLevel.OP,
                "Permite usar /rlv reset");
        PermissionAPI.registerNode(SETMULTIPLIER,
                DefaultPermissionLevel.OP,
                "Permite usar /rlv setmultiplier");
        PermissionAPI.registerNode(PASS_SET,
                DefaultPermissionLevel.OP,
                "Permite usar /rlv pass set");
        PermissionAPI.registerNode(CLICK_EVENTO,
                DefaultPermissionLevel.OP,
                "Permite usar /rlv click_evento_1");

        // Usuario
        PermissionAPI.registerNode(HELP,
                DefaultPermissionLevel.ALL,
                "Permite usar /rlv help");
        PermissionAPI.registerNode(STATS,
                DefaultPermissionLevel.ALL,
                "Permite usar /rlv stats y alias");
        PermissionAPI.registerNode(MULTIPLIERS,
                DefaultPermissionLevel.ALL,
                "Permite usar /rlv multipliers");
        PermissionAPI.registerNode(PASS_INFO,
                DefaultPermissionLevel.ALL,
                "Permite usar /rlv pass info");
        PermissionAPI.registerNode(PASS_BUY,
                DefaultPermissionLevel.ALL,
                "Permite usar /rlv pass buy");
        PermissionAPI.registerNode(MENU,
                DefaultPermissionLevel.ALL,
                "Permite usar /rlv menu");
        PermissionAPI.registerNode(REWARDS,
                DefaultPermissionLevel.ALL,
                "Permite usar /rlv rewards");
        PermissionAPI.registerNode(PURGA,
                DefaultPermissionLevel.ALL,
                "Permite usar /rlv purga");
    }

    /**
     * Comprueba que la fuente sea un ServerPlayerEntity y tenga el permiso dado.
     */
    public static boolean has(CommandSource source, String node) {
        boolean isConsole = source.getEntity() == null;
        boolean isOp = source.hasPermission(2);  // Nivel de OP
        boolean result = false;

        if (!isConsole && !isOp) {
            ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

            // 1. Verificar por Forge (por si se definió default = ALL)
            result = PermissionAPI.hasPermission(player, node);

            // 2. Intentar verificar con Bukkit (LuckPerms)
            try {
                Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
                Object bukkitPlayer = bukkitClass
                        .getMethod("getPlayer", java.util.UUID.class)
                        .invoke(null, player.getUUID());

                if (bukkitPlayer != null) {
                    boolean hasPerm = (boolean) bukkitPlayer
                            .getClass()
                            .getMethod("hasPermission", String.class)
                            .invoke(bukkitPlayer, node);
                    if (hasPerm) {
                        result = true;
                    }
                }
            } catch (Exception e) {
                // Bukkit no está disponible o error al acceder → ignorar
            }
        }

        return isConsole || isOp || result;
    }



    /**
     * Predicado para un solo nodo.
     * Uso en .requires(PermissionRegistrar.require(NODO))
     */
    public static Predicate<CommandSource> require(String node) {
        return src -> has(src, node);
    }

    /**
     * Predicado para múltiples nodos en OR.
     * Uso en .requires(PermissionRegistrar.requireAny(NODO1, NODO2, ...))
     */
    public static Predicate<CommandSource> requireAny(String... nodes) {
        return src -> {
            for (String n : nodes) {
                if (has(src, n)) return true;
            }
            return false;
        };
    }
}
