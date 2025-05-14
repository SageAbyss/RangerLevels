package rl.sage.rangerlevels.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.config.ExpConfig;

import java.sql.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MySQLManager implements IPlayerDataManager {
    private final RangerLevels plugin;
    private final ExpConfig.Database.MySQL cfg;
    private HikariDataSource ds;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public MySQLManager(RangerLevels plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getExpConfig().getMySQLConfig();
        initDataSource();
        initTables();
    }

    private void initDataSource() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = String.format(
                    "jdbc:mysql://%s:%d/%s?useSSL=%b&serverTimezone=UTC",
                    cfg.host, cfg.port, cfg.database, cfg.useSSL
            );
            HikariConfig hc = new HikariConfig();
            hc.setJdbcUrl(url);
            hc.setUsername(cfg.username);
            hc.setPassword(cfg.password);
            hc.setMaximumPoolSize(cfg.maxPoolSize);
            hc.setMinimumIdle(cfg.minIdle);
            ds = new HikariDataSource(hc);
        } catch (Exception e) {
            plugin.getLogger().error("[MySQL] Error al inicializar datasource", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection(); // Retorna una conexión al datasource Hikari
    }

    @Override
    public void initTables() {
        String ddl = "CREATE TABLE IF NOT EXISTS player_data (" +
                "uuid CHAR(36) PRIMARY KEY," +
                "nickname VARCHAR(36)," +
                "level INT," +
                "exp DOUBLE," +
                "last_update DATETIME," +
                "multiplier FLOAT" +
                ")";
        try (Connection c = ds.getConnection();
             Statement s = c.createStatement()) {
            s.executeUpdate(ddl);
        } catch (SQLException e) {
            plugin.getLogger().error("[MySQL] Error creando tabla player_data", e);
        }
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid, String nickname) {
        // Primero se revisa la caché
        if (cache.containsKey(uuid)) return cache.get(uuid);

        String sql = "SELECT nickname, level, exp, last_update, multiplier FROM player_data WHERE uuid=?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PlayerData pd = new PlayerData(
                        uuid,
                        rs.getString("nickname"),
                        rs.getInt("level"),
                        rs.getDouble("exp"),
                        rs.getFloat("multiplier")
                );
                pd.setLastUpdate(rs.getTimestamp("last_update").toInstant());
                cache.put(uuid, pd);
                return pd;
            }
        } catch (SQLException e) {
            plugin.getLogger().error("[MySQL] Error cargando datos de " + nickname, e);
        }

        // Si no existe en la base de datos, se crea un nuevo PlayerData
        PlayerData pd = new PlayerData(uuid, nickname,
                plugin.getExpConfig().levels.starting.level,
                plugin.getExpConfig().levels.starting.experience,
                plugin.getExpConfig().multipliers.playerDefault
        );
        cache.put(uuid, pd);
        savePlayerData(pd);
        return pd;
    }

    @Override
    public void savePlayerData(PlayerData data) {
        String upsert = "INSERT INTO player_data " +
                "(uuid, nickname, level, exp, last_update, multiplier) VALUES (?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "nickname=VALUES(nickname), level=VALUES(level), exp=VALUES(exp), " +
                "last_update=VALUES(last_update), multiplier=VALUES(multiplier)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(upsert)) {
            ps.setString(1, data.getUuid().toString());
            ps.setString(2, data.getNickname());
            ps.setInt(3, data.getLevel());
            ps.setDouble(4, data.getExp());
            ps.setTimestamp(5, Timestamp.from(Instant.now()));
            ps.setFloat(6, data.getMultiplier());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().error("[MySQL] Error guardando datos de " + data.getNickname(), e);
        }
    }

    @Override
    public void unloadPlayerData(UUID uuid) {
        cache.remove(uuid);
    }

    @Override
    public void close() {
        if (ds != null && !ds.isClosed()) ds.close();
    }

    @Override
    public String getSourceName() {
        return "MySQL";
    }
}
