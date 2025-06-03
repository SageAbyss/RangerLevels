package rl.sage.rangerlevels.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;

import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.database.FlatFilePlayerDataManager;
import rl.sage.rangerlevels.database.PlayerData;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Comando /rlv seen <playerName>: muestra todos los datos del jugador.
 *
 * - IP → si está online, se obtiene en tiempo real; si no, se lee de ipMap.
 *         Si nunca se obtuvo, “Desconocido”.
 * - Última vez conectado →
 *       • Si está online: “En línea”
 *       • Si no: la fecha guardada en lastLoginMap formateada como "yyyy-MM-dd HH:mm:ss".
 *         Si no existe, “Desconocido”.
 * - Nivel → (desde PlayerData)
 * - Exp → <actual> / <siguiente> (<%>)
 * - ProgressBar → 20 bloques
 */
public class CommandSeen {

    // ---------------------------------------------------
    // Variables estáticas para almacenar IP y última conexión
    // ---------------------------------------------------
    /** Mapa nickname → última IP conocida */
    private static final Map<String, String> ipMap = new HashMap<>();
    /** Mapa nickname → timestamp (ms) de la última vez que estuvo online cuando se ejecutó /rlv seen */
    private static final Map<String, Long> lastLoginMap = new HashMap<>();
    // ---------------------------------------------------

    public static int seen(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        String targetName = StringArgumentType.getString(ctx, "playerName");
        CommandSource src = ctx.getSource();



        // 1) Cargamos el JSON más reciente
        FlatFilePlayerDataManager manager =
                (FlatFilePlayerDataManager) RangerLevels.INSTANCE.getDataManager();
        manager.loadAll();

        // 2) Buscamos en Data.json el PlayerData cuyo nickname coincida (ignora mayúsculas)
        Optional<PlayerData> dataOpt = manager.getAllData().stream()
                .filter(pd -> pd.getNickname().equalsIgnoreCase(targetName))
                .findFirst();

        if (!dataOpt.isPresent()) {
            src.sendFailure(new StringTextComponent(
                    TextFormatting.RED + "No se encontró al jugador: " + targetName));
            return 0;
        }

        PlayerData pd = dataOpt.get();
        int lvl = pd.getLevel();
        int exp = (int) pd.getExp();

        // 3) Calculamos EXP necesaria para siguiente nivel y porcentaje de progreso
        int nextExp = ExpConfig.get().getLevels().getExpForLevel(lvl + 1);
        int perc = (int) (exp * 100.0 / nextExp);
        perc = MathHelper.clamp(perc, 0, 100);

        // 4) Obtenemos IP:
        //    - Si está online, obtenemos en tiempo real y actualizamos ipMap.
        //    - Si no, intentamos leer ipMap.get(targetName); si no existe, devolvemos "Desconocido".
        String ip = fetchPlayerIp(targetName, src.getServer());

        // 5) Determinamos “Última vez conectado”:
        //    - Si está online, devolvemos “En línea” y registramos timestamp en lastLoginMap.
        //    - Si no está online, intentamos leer lastLoginMap.get(targetName) y formatearlo;
        //      si no existe, devolvemos “Desconocido”.
        String lastConnectionLine = fetchLastConnectionLine(targetName, src.getServer());

        // 6) Construimos la progress bar de 20 bloques
        int barrasTotales = 20;
        int llenas = (perc * barrasTotales) / 100;
        int vacias = barrasTotales - llenas;

        StringBuilder sbBar = new StringBuilder();
        sbBar.append(TextFormatting.GREEN);
        for (int i = 0; i < llenas; i++) sbBar.append("█");
        sbBar.append(TextFormatting.DARK_GRAY);
        for (int i = 0; i < vacias; i++) sbBar.append("░");
        sbBar.append(TextFormatting.RESET);

        String progressBarLine = TextFormatting.AQUA
                + "ProgressBar → "
                + sbBar.toString();

        // 7) Enviamos cada línea al chat (sin símbolos Unicode de caja)
        // 7a) Título con flecha y nombre
        src.sendSuccess(new StringTextComponent(
                TextFormatting.GOLD + "➤ Acerca de "
                        + TextFormatting.AQUA + pd.getNickname()), false);

        // 7b) IP
        src.sendSuccess(new StringTextComponent(
                TextFormatting.AQUA + "IP → "
                        + TextFormatting.WHITE + ip), false);

        // 7c) Última vez conectado
        src.sendSuccess(new StringTextComponent(lastConnectionLine), false);

        // 7d) Nivel
        src.sendSuccess(new StringTextComponent(
                TextFormatting.AQUA + "Nivel → "
                        + TextFormatting.WHITE + lvl), false);

        // 7e) Exp (entero, exp requerida y porcentaje)
        String expLine = TextFormatting.AQUA + "Exp → "
                + TextFormatting.WHITE
                + exp + " / " + nextExp + " (" + perc + "%)";
        src.sendSuccess(new StringTextComponent(expLine), false);

        // 7f) ProgressBar
        src.sendSuccess(new StringTextComponent(progressBarLine), false);

        return 1;
    }

    /**
     * Intenta obtener la IP del jugador si está en línea.
     * - Si lo está, extrae IP y la guarda en ipMap.
     * - Si no, devuelve ipMap.get(playerName) o "Desconocido" si nunca existió.
     *
     * En Forge 1.16.5, ServerPlayerEntity.connection (ServerPlayNetHandler)
     * tiene un campo 'connection' (NetworkManager) con método getRemoteAddress().
     */
    private static String fetchPlayerIp(String playerName, MinecraftServer server) {
        ServerPlayerEntity onlinePlayer =
                server.getPlayerList().getPlayerByName(playerName);

        if (onlinePlayer != null) {
            try {
                // 'onlinePlayer.connection' es un ServerPlayNetHandler.
                // Ese handler tiene un campo 'connection' (type NetworkManager).
                // Obtenemos su dirección remota y la casteamos a InetSocketAddress.
                Object netHandler = onlinePlayer.connection;
                java.lang.reflect.Field fm = netHandler.getClass().getDeclaredField("connection");
                fm.setAccessible(true);
                Object netMgr = fm.get(netHandler);
                if (netMgr != null) {
                    java.lang.reflect.Method getRemote =
                            netMgr.getClass().getMethod("getRemoteAddress");
                    Object addrObj = getRemote.invoke(netMgr);
                    if (addrObj instanceof SocketAddress) {
                        InetSocketAddress inet = (InetSocketAddress) addrObj;
                        String ipReal = inet.getAddress().getHostAddress();
                        // Guardamos la IP en ipMap para uso futuro
                        ipMap.put(playerName.toLowerCase(), ipReal);
                        return ipReal;
                    }
                }

            } catch (Exception e) {
                // Si algo falla, caemos abajo para intentar leer ipMap
            }
        }

        // Si llegamos aquí, el jugador NO está online o hubo un error al leer IP en tiempo real.
        // Intentamos devolver la IP almacenada:
        String guardada = ipMap.get(playerName.toLowerCase());
        return (guardada != null) ? guardada : "Desconocido";
    }

    /**
     * Construye la línea “Última vez conectado → ...”:
     * - Si el jugador está online, devolvemos "En línea" y guardamos timestamp en lastLoginMap.
     * - Si NO está online, leemos lastLoginMap.get(playerName) y lo formateamos;
     *   si no existe, devolvemos "Desconocido".
     */
    private static String fetchLastConnectionLine(String playerName, MinecraftServer server) {
        ServerPlayerEntity onlinePlayer =
                server.getPlayerList().getPlayerByName(playerName);

        if (onlinePlayer != null) {
            // Si está online, actualizamos la marca de la última conexión:
            long ahora = System.currentTimeMillis();
            lastLoginMap.put(playerName.toLowerCase(), ahora);

            return TextFormatting.AQUA
                    + "Última vez conectado → "
                    + TextFormatting.GREEN + "En línea";
        } else {
            // El jugador NO está online: buscamos in lastLoginMap
            Long ts = lastLoginMap.get(playerName.toLowerCase());
            if (ts != null) {
                // Formateamos el timestamp: "yyyy-MM-dd HH:mm:ss"
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String fecha = sdf.format(new Date(ts));
                return TextFormatting.AQUA
                        + "Última vez conectado → "
                        + TextFormatting.GRAY + fecha;
            } else {
                // Nunca se conectó (o nunca ejecutó /rlv seen en línea)
                return TextFormatting.AQUA
                        + "Última vez conectado → "
                        + TextFormatting.GRAY + "Desconocido";
            }
        }
    }


}
