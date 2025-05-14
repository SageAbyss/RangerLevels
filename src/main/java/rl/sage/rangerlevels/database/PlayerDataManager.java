package rl.sage.rangerlevels.database;

import rl.sage.rangerlevels.RangerLevels;

import java.sql.*;
import java.util.UUID;

/**
 * Implementación principal del gestor de datos que se comunica con la base de datos MySQL.
 */
public class PlayerDataManager implements IPlayerDataManager {
    private final RangerLevels plugin;
    private final MySQLManager mysql;
    private final String table;

    public PlayerDataManager(RangerLevels plugin) {
        this.plugin = plugin;
        this.mysql = plugin.getMySQL();
        this.table = "player_data"; // Aseguramos que la tabla esté bien definida
    }

    /**
     * Inicializa la tabla si no existe.
     */
    @Override
    public void initTables() {
        String create = "CREATE TABLE IF NOT EXISTS `" + table + "` (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "nickname VARCHAR(32)," +
                "level INT DEFAULT 1," +
                "xp DOUBLE DEFAULT 0," +
                "player_multiplier FLOAT DEFAULT 1," +
                "last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
        try (Connection conn = mysql.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(create);
            plugin.getLogger().info("[MySQL] Tabla '" + table + "' verificada o creada con éxito.");
        } catch (SQLException e) {
            plugin.getLogger().error("[MySQL] Error al inicializar tabla '" + table + "'", e);
        }
    }

    /**
     * Carga los datos del jugador desde la base de datos, o crea una entrada nueva si no existe.
     */
    @Override
    public PlayerData loadPlayerData(UUID uuid, String nickname) {
        String select = "SELECT level, xp, player_multiplier FROM `" + table + "` WHERE uuid = ?";
        try (Connection conn = mysql.getConnection();
             PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PlayerData(
                            uuid,
                            nickname,
                            rs.getInt("level"),
                            rs.getDouble("xp"),
                            rs.getFloat("player_multiplier")
                    );
                }
            }

            // Si no existe, lo insertamos con valores por defecto
            String insert = "INSERT INTO `" + table + "` (uuid, nickname) VALUES (?,?)";
            try (PreparedStatement ps2 = conn.prepareStatement(insert)) {
                ps2.setString(1, uuid.toString());
                ps2.setString(2, nickname);
                ps2.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().error("[MySQL] Error al cargar datos de " + nickname, e);
        }

        // Retornar valores por defecto si no hay datos previos
        return new PlayerData(uuid, nickname, 1, 0.0, 1.0f);
    }

    /**
     * Guarda o actualiza los datos del jugador en la base de datos.
     */
    @Override
    public void savePlayerData(PlayerData data) {
        String update = "UPDATE `" + table + "` SET " +
                "nickname = ?, level = ?, xp = ?, player_multiplier = ?, last_seen = CURRENT_TIMESTAMP " +
                "WHERE uuid = ?";
        try (Connection conn = mysql.getConnection();
             PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setString(1, data.getNickname());
            ps.setInt(2, data.getLevel());
            ps.setDouble(3, data.getExp());
            ps.setFloat(4, data.getMultiplier());
            ps.setString(5, data.getUuid().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().error("[MySQL] Error al guardar datos de " + data.getNickname(), e);
        }
    }

    /**
     * Limpia los datos del jugador de la memoria (p. ej. al desconectarse).
     */
    @Override
    public void unloadPlayerData(UUID uuid) {
        // Aquí puedes agregar lógica si necesitas limpiar algún dato en memoria.
    }

    /**
     * Cierra las conexiones de base de datos si aplica.
     */
    @Override
    public void close() {
        // Aquí puedes agregar lógica si necesitas cerrar recursos adicionales.
    }

    /**
     * Retorna el nombre de la fuente de datos (MySQL).
     */
    @Override
    public String getSourceName() {
        return "MySQL";
    }
}
