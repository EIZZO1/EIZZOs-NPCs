package com.eizzo.npcs.managers;
import com.eizzo.npcs.EizzoNPCs;
import com.eizzo.npcs.models.NPC;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public class DatabaseManager {
    private final EizzoNPCs plugin;
    private HikariDataSource dataSource;
    public DatabaseManager(EizzoNPCs plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        String type = plugin.getConfig().getString("database.type", "sqlite");
        HikariConfig config = new HikariConfig();
        if (type.equalsIgnoreCase("sqlite")) {
            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder() + "/npcs.db");
            config.setPoolName("EizzoNPCSQLite");
            config.setConnectionInitSql("PRAGMA foreign_keys = ON;");
        } else {
            String host = plugin.getConfig().getString("database.host", "localhost");
            int port = plugin.getConfig().getInt("database.port", 3306);
            String database = plugin.getConfig().getString("database.database", "minecraft");
            String username = plugin.getConfig().getString("database.username", "root");
            String password = plugin.getConfig().getString("database.password", "");
            config.setDriverClassName("org.mariadb.jdbc.Driver");
            config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(plugin.getConfig().getInt("database.pool-size", 10));
            config.setPoolName("EizzoNPCMPool");
        }
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        try {
            dataSource = new HikariDataSource(config);
            createTables();
            plugin.getLogger().info("Connected to database successfully!");
        } catch (Exception e) {
            plugin.getLogger().severe("Could not connect to database! " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Connection conn = getConnection()) {
            // Main NPCs table
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS npcs (" +
                        "id VARCHAR(64) PRIMARY KEY, " +
                        "name TEXT, " +
                        "type VARCHAR(64), " +
                        "world VARCHAR(128), " +
                        "x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, " +
                        "run_as_op BOOLEAN, run_as_console BOOLEAN, show_cape BOOLEAN, " +
                        "collidable BOOLEAN, npc_collision BOOLEAN, flying BOOLEAN, hostile BOOLEAN, " +
                        "return_to_spawn BOOLEAN, nametag_visible BOOLEAN, " +
                        "tracking_mode VARCHAR(32), tracking_range DOUBLE, " +
                        "skin_name TEXT, skin_value TEXT, skin_signature TEXT" +
                        ");");
                // Schema Migration: Add interact_sound if it doesn't exist
                try {
                    stmt.execute("ALTER TABLE npcs ADD COLUMN interact_sound TEXT;");
                    plugin.getLogger().info("Migrated database schema: Added interact_sound column.");
                } catch (SQLException ignored) {} // Column already exists
                // Schema Migration: Add hostile if it doesn't exist
                try {
                    stmt.execute("ALTER TABLE npcs ADD COLUMN hostile BOOLEAN DEFAULT 0;");
                    plugin.getLogger().info("Migrated database schema: Added hostile column.");
                } catch (SQLException ignored) {} // Column already exists
                try {
                    stmt.execute("ALTER TABLE npcs ADD COLUMN god_mode BOOLEAN DEFAULT 1;");
                    plugin.getLogger().info("Migrated database schema: Added god_mode column.");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE npcs ADD COLUMN respawn_delay INT DEFAULT 5;");
                    plugin.getLogger().info("Migrated database schema: Added respawn_delay column.");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE npcs ADD COLUMN max_health DOUBLE DEFAULT 20.0;");
                    plugin.getLogger().info("Migrated database schema: Added max_health column.");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE npcs ADD COLUMN current_health DOUBLE DEFAULT 20.0;");
                    plugin.getLogger().info("Migrated database schema: Added current_health column.");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE npcs ADD COLUMN show_health_bar BOOLEAN DEFAULT 1;");
                    plugin.getLogger().info("Migrated database schema: Added show_health_bar column.");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE npcs ADD COLUMN vault_reward DOUBLE DEFAULT 0.0;");
                    plugin.getLogger().info("Migrated database schema: Added vault_reward column.");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE npcs ADD COLUMN token_reward DOUBLE DEFAULT 0.0;");
                    plugin.getLogger().info("Migrated database schema: Added token_reward column.");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE npcs ADD COLUMN reward_token_id TEXT DEFAULT 'tokens';");
                    plugin.getLogger().info("Migrated database schema: Added reward_token_id column.");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE npcs ADD COLUMN dialogue_once BOOLEAN DEFAULT 0;");
                    plugin.getLogger().info("Migrated database schema: Added dialogue_once column.");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE npcs ADD COLUMN reward_limit INT DEFAULT 0;");
                    plugin.getLogger().info("Migrated database schema: Added reward_limit column.");
                } catch (SQLException ignored) {}
                stmt.execute("CREATE TABLE IF NOT EXISTS player_seen_nodes (" +
                        "uuid VARCHAR(36), " +
                        "npc_id VARCHAR(64), " +
                        "node_name VARCHAR(64), " +
                        "PRIMARY KEY (uuid, npc_id, node_name), " +
                        "FOREIGN KEY (npc_id) REFERENCES npcs(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                        ");");
                stmt.execute("CREATE TABLE IF NOT EXISTS player_reward_counts (" +
                        "uuid VARCHAR(36), " +
                        "npc_id VARCHAR(64), " +
                        "count INT DEFAULT 0, " +
                        "PRIMARY KEY (uuid, npc_id), " +
                        "FOREIGN KEY (npc_id) REFERENCES npcs(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                        ");");
                stmt.execute("CREATE TABLE IF NOT EXISTS npc_commands (" +
                        "npc_id VARCHAR(64), " +
                        "command TEXT, " +
                        "FOREIGN KEY (npc_id) REFERENCES npcs(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                        ");");
                stmt.execute("CREATE TABLE IF NOT EXISTS npc_equipment (" +
                        "npc_id VARCHAR(64), " +
                        "slot VARCHAR(32), " +
                        "item_stack TEXT, " +
                        "PRIMARY KEY (npc_id, slot), " +
                        "FOREIGN KEY (npc_id) REFERENCES npcs(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                        ");");
                stmt.execute("CREATE TABLE IF NOT EXISTS npc_dialogues (" +
                        "npc_id VARCHAR(64), " +
                        "node_name VARCHAR(64), " +
                        "sequence TEXT, " +
                        "PRIMARY KEY (npc_id, node_name), " +
                        "FOREIGN KEY (npc_id) REFERENCES npcs(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                        ");");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markNodeSeen(UUID uuid, String npcId, String nodeName) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO player_seen_nodes (uuid, npc_id, node_name) VALUES (?, ?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npcId);
            ps.setString(3, nodeName);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasSeenNode(UUID uuid, String npcId, String nodeName) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM player_seen_nodes WHERE uuid=? AND npc_id=? AND node_name=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npcId);
            ps.setString(3, nodeName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void renameNPC(String oldId, String newId) {
        try (Connection conn = getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // Temporarily disable FKs to allow updating parent and children manually
                if (isSQLite()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("PRAGMA foreign_keys = OFF;");
                    }
                }

                // 1. Update children tables first
                String[] childTables = {"npc_commands", "npc_equipment", "npc_dialogues", "player_seen_nodes", "player_reward_counts"};
                for (String table : childTables) {
                    try (PreparedStatement ps = conn.prepareStatement("UPDATE " + table + " SET npc_id = ? WHERE npc_id = ?")) {
                        ps.setString(1, newId);
                        ps.setString(2, oldId);
                        ps.executeUpdate();
                    }
                }

                // 2. Update parent table
                try (PreparedStatement ps = conn.prepareStatement("UPDATE npcs SET id = ? WHERE id = ?")) {
                    ps.setString(1, newId);
                    ps.setString(2, oldId);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                if (isSQLite()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("PRAGMA foreign_keys = ON;");
                    }
                }
                conn.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) throw new SQLException("Database not connected.");
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) dataSource.close();
    }

    public void saveNPC(NPC npc) {
        plugin.getLogger().info("[Debug] Saving NPC '" + npc.getId() + "': GodMode=" + npc.isGodMode() + ", Health=" + npc.getCurrentHealth() + "/" + npc.getMaxHealth());
        try (Connection conn = getConnection()) {
            String query;
            if (isSQLite()) {
                query = "INSERT INTO npcs (id, name, type, world, x, y, z, yaw, pitch, run_as_op, run_as_console, show_cape, " +
                        "collidable, npc_collision, flying, hostile, return_to_spawn, nametag_visible, tracking_mode, tracking_range, " +
                        "skin_name, skin_value, skin_signature, interact_sound, god_mode, respawn_delay, max_health, current_health, show_health_bar, vault_reward, token_reward, reward_token_id, dialogue_once, reward_limit) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                        "ON CONFLICT(id) DO UPDATE SET name=excluded.name, type=excluded.type, world=excluded.world, x=excluded.x, y=excluded.y, z=excluded.z, " +
                        "yaw=excluded.yaw, pitch=excluded.pitch, run_as_op=excluded.run_as_op, run_as_console=excluded.run_as_console, show_cape=excluded.show_cape, " +
                        "collidable=excluded.collidable, npc_collision=excluded.npc_collision, flying=excluded.flying, hostile=excluded.hostile, return_to_spawn=excluded.return_to_spawn, " +
                        "nametag_visible=excluded.nametag_visible, tracking_mode=excluded.tracking_mode, tracking_range=excluded.tracking_range, " +
                        "skin_name=excluded.skin_name, skin_value=excluded.skin_value, skin_signature=excluded.skin_signature, interact_sound=excluded.interact_sound, " +
                        "god_mode=excluded.god_mode, respawn_delay=excluded.respawn_delay, max_health=excluded.max_health, current_health=excluded.current_health, " +
                        "show_health_bar=excluded.show_health_bar, vault_reward=excluded.vault_reward, token_reward=excluded.token_reward, reward_token_id=excluded.reward_token_id, " +
                        "dialogue_once=excluded.dialogue_once, reward_limit=excluded.reward_limit";
            } else {
                query = "INSERT INTO npcs (id, name, type, world, x, y, z, yaw, pitch, run_as_op, run_as_console, show_cape, " +
                        "collidable, npc_collision, flying, hostile, return_to_spawn, nametag_visible, tracking_mode, tracking_range, " +
                        "skin_name, skin_value, skin_signature, interact_sound, god_mode, respawn_delay, max_health, current_health, show_health_bar, vault_reward, token_reward, reward_token_id, dialogue_once, reward_limit) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE name=VALUES(name), type=VALUES(type), world=VALUES(world), x=VALUES(x), y=VALUES(y), z=VALUES(z), " +
                        "yaw=VALUES(yaw), pitch=VALUES(pitch), run_as_op=VALUES(run_as_op), run_as_console=VALUES(run_as_console), show_cape=VALUES(show_cape), " +
                        "collidable=VALUES(collidable), npc_collision=VALUES(npc_collision), flying=VALUES(flying), hostile=VALUES(hostile), return_to_spawn=VALUES(return_to_spawn), " +
                        "nametag_visible=VALUES(nametag_visible), tracking_mode=VALUES(tracking_mode), tracking_range=VALUES(tracking_range), " +
                        "skin_name=VALUES(skin_name), skin_value=VALUES(skin_value), skin_signature=VALUES(skin_signature), interact_sound=VALUES(interact_sound), " +
                        "god_mode=VALUES(god_mode), respawn_delay=VALUES(respawn_delay), max_health=VALUES(max_health), current_health=VALUES(current_health), " +
                        "show_health_bar=VALUES(show_health_bar), vault_reward=VALUES(vault_reward), token_reward=VALUES(token_reward), reward_token_id=VALUES(reward_token_id), " +
                        "dialogue_once=VALUES(dialogue_once), reward_limit=VALUES(reward_limit)";
            }
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, npc.getId());
                ps.setString(2, npc.getName());
                ps.setString(3, npc.getType().name());
                ps.setString(4, npc.getLocation().getWorld().getName());
                ps.setDouble(5, npc.getLocation().getX());
                ps.setDouble(6, npc.getLocation().getY());
                ps.setDouble(7, npc.getLocation().getZ());
                ps.setFloat(8, npc.getLocation().getYaw());
                ps.setFloat(9, npc.getLocation().getPitch());
                ps.setBoolean(10, npc.isRunAsOp());
                ps.setBoolean(11, npc.isRunAsConsole());
                ps.setBoolean(12, npc.isShowCape());
                ps.setBoolean(13, npc.isCollidable());
                ps.setBoolean(14, npc.isNpcCollision());
                ps.setBoolean(15, npc.isFlying());
                ps.setBoolean(16, npc.isHostile());
                ps.setBoolean(17, npc.isReturnToSpawn());
                ps.setBoolean(18, npc.isNametagVisible());
                ps.setString(19, npc.getTrackingMode().name());
                ps.setDouble(20, npc.getTrackingRange());
                ps.setString(21, npc.getSkinName());
                ps.setString(22, npc.getSkinValue());
                ps.setString(23, npc.getSkinSignature());
                ps.setString(24, npc.getInteractSound());
                ps.setBoolean(25, npc.isGodMode());
                ps.setInt(26, npc.getRespawnDelay());
                ps.setDouble(27, npc.getMaxHealth());
                ps.setDouble(28, npc.getCurrentHealth());
                ps.setBoolean(29, npc.isShowHealthBar());
                ps.setDouble(30, npc.getVaultReward());
                ps.setDouble(31, npc.getTokenReward());
                ps.setString(32, npc.getRewardTokenId());
                ps.setBoolean(33, npc.isDialogueOnce());
                ps.setInt(34, npc.getRewardLimit());
                ps.executeUpdate();
            }
            // Commands
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM npc_commands WHERE npc_id = ?")) {
                ps.setString(1, npc.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO npc_commands (npc_id, command) VALUES (?,?)")) {
                for (String cmd : npc.getCommands()) {
                    ps.setString(1, npc.getId());
                    ps.setString(2, cmd);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            plugin.getLogger().info("Successfully saved NPC '" + npc.getId() + "' commands.");
            // Equipment
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM npc_equipment WHERE npc_id = ?")) {
                ps.setString(1, npc.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO npc_equipment (npc_id, slot, item_stack) VALUES (?,?,?)")) {
                for (Map.Entry<EquipmentSlot, ItemStack> entry : npc.getEquipment().entrySet()) {
                    if (entry.getValue() == null) continue;
                    ps.setString(1, npc.getId());
                    ps.setString(2, entry.getKey().name());
                    ps.setString(3, serializeItem(entry.getValue()));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            // Dialogues
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM npc_dialogues WHERE npc_id = ?")) {
                ps.setString(1, npc.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO npc_dialogues (npc_id, node_name, sequence) VALUES (?,?,?)")) {
                for (Map.Entry<String, List<String>> entry : npc.getDialogues().entrySet()) {
                    ps.setString(1, npc.getId());
                    ps.setString(2, entry.getKey());
                    ps.setString(3, String.join("\n", entry.getValue()));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            plugin.getLogger().info("Successfully saved NPC '" + npc.getId() + "' dialogues.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incrementRewardCount(UUID uuid, String npcId) {
        try (Connection conn = getConnection()) {
            String query;
            if (isSQLite()) {
                query = "INSERT INTO player_reward_counts (uuid, npc_id, count) VALUES (?, ?, 1) " +
                        "ON CONFLICT(uuid, npc_id) DO UPDATE SET count = count + 1";
            } else {
                query = "INSERT INTO player_reward_counts (uuid, npc_id, count) VALUES (?, ?, 1) " +
                        "ON DUPLICATE KEY UPDATE count = count + 1";
            }
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, npcId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getRewardCount(UUID uuid, String npcId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT count FROM player_reward_counts WHERE uuid=? AND npc_id=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npcId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean isSQLite() {
        return plugin.getConfig().getString("database.type", "sqlite").equalsIgnoreCase("sqlite");
    }

    public List<NPC> loadNPCs() {
        List<NPC> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM npcs")) {
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                EntityType type = EntityType.valueOf(rs.getString("type"));
                Location loc = new Location(
                        Bukkit.getWorld(rs.getString("world")),
                        rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                        rs.getFloat("yaw"), rs.getFloat("pitch")
                );
                NPC npc = new NPC(id, name, type, loc);
                npc.setRunAsOp(rs.getBoolean("run_as_op"));
                npc.setRunAsConsole(rs.getBoolean("run_as_console"));
                npc.setShowCape(rs.getBoolean("show_cape"));
                npc.setCollidable(rs.getBoolean("collidable"));
                npc.setNpcCollision(rs.getBoolean("npc_collision"));
                npc.setFlying(rs.getBoolean("flying"));
                npc.setHostile(rs.getBoolean("hostile"));
                npc.setReturnToSpawn(rs.getBoolean("return_to_spawn"));
                npc.setNametagVisible(rs.getBoolean("nametag_visible"));
                npc.setGodMode(rs.getBoolean("god_mode"));
                npc.setRespawnDelay(rs.getInt("respawn_delay"));
                npc.setMaxHealth(rs.getDouble("max_health"));
                npc.setCurrentHealth(rs.getDouble("current_health"));
                npc.setShowHealthBar(rs.getBoolean("show_health_bar"));
                npc.setVaultReward(rs.getDouble("vault_reward"));
                npc.setTokenReward(rs.getDouble("token_reward"));
                npc.setRewardTokenId(rs.getString("reward_token_id"));
                npc.setDialogueOnce(rs.getBoolean("dialogue_once"));
                npc.setRewardLimit(rs.getInt("reward_limit"));
                npc.setTrackingMode(NPC.TrackingMode.valueOf(rs.getString("tracking_mode")));
                npc.setTrackingRange(rs.getDouble("tracking_range"));
                npc.setSkinName(rs.getString("skin_name"));
                npc.setSkinValue(rs.getString("skin_value"));
                npc.setSkinSignature(rs.getString("skin_signature"));
                npc.setInteractSound(rs.getString("interact_sound"));
                // Load Commands
                try (PreparedStatement psCmd = conn.prepareStatement("SELECT command FROM npc_commands WHERE npc_id = ?")) {
                    psCmd.setString(1, id);
                    try (ResultSet rsCmd = psCmd.executeQuery()) {
                        while (rsCmd.next()) npc.getCommands().add(rsCmd.getString("command"));
                    }
                }
                // Load Equipment
                try (PreparedStatement psEq = conn.prepareStatement("SELECT slot, item_stack FROM npc_equipment WHERE npc_id = ?")) {
                    psEq.setString(1, id);
                    try (ResultSet rsEq = psEq.executeQuery()) {
                        while (rsEq.next()) {
                            EquipmentSlot slot = EquipmentSlot.valueOf(rsEq.getString("slot"));
                            ItemStack item = deserializeItem(rsEq.getString("item_stack"));
                            if (item != null) npc.getEquipment().put(slot, item);
                        }
                    }
                }
                // Load Dialogues
                try (PreparedStatement psDiag = conn.prepareStatement("SELECT node_name, sequence FROM npc_dialogues WHERE npc_id = ?")) {
                    psDiag.setString(1, id);
                    try (ResultSet rsDiag = psDiag.executeQuery()) {
                        while (rsDiag.next()) {
                            String nodeName = rsDiag.getString("node_name");
                            String sequence = rsDiag.getString("sequence");
                            npc.getDialogues().put(nodeName, new ArrayList<>(Arrays.asList(sequence.split("\n"))));
                        }
                    }
                }
                list.add(npc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        plugin.getLogger().info("Loaded " + list.size() + " NPCs from database.");
        return list;
    }

    public void deleteNPC(String id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM npcs WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String serializeItem(ItemStack item) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", item);
        return config.saveToString();
    }

    private ItemStack deserializeItem(String data) {
        if (data == null || data.isEmpty()) return null;
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(data);
            return config.getItemStack("item");
        } catch (Exception e) {
            return null;
        }
    }

}

