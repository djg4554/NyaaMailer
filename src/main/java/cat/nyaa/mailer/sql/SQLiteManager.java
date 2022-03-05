package cat.nyaa.mailer.sql;

import cat.nyaa.mailer.NyaaMailer;
import org.bukkit.Bukkit;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class SQLiteManager {
    private final NyaaMailer plugin;
    private File dbFile;

    private SQLiteDataSource dataSource;


    public SQLiteManager(NyaaMailer plugin) {
        this.plugin = plugin;
        loadFile();
        initDataSource();


    }

    private void initDataSource() {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        dataSource.setDatabaseName("leaderboards");
    }

    private void loadFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        File file = new File(plugin.getDataFolder(), plugin.getConfig().getString("database", "nyamailer.db"));
        if (!file.exists()) {
            try {
                file.createNewFile();
                dbFile = file;
            } catch (Exception e) {
                Bukkit.getLogger().severe("Could not create database file!");
            }
        } else {
            dbFile = file;
        }
    }

    public CompletableFuture<Void> loadTables() {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                conn.createStatement().execute("create table IF NOT EXISTS player(\n" +
                        "    uuid char(36) not null,\n" +
                        "     name varchar(20),\n" +
                        "      primary key(uuid, name)\n" +
                        ");");
                conn.createStatement().execute("create table if not exists item(\n" +
                        "    encoded_item varchar(128) not null,\n" +
                        "    sender char(36) not null,\n" +
                        "    receiver char(36) not null,\n" +
                        "    mail_date date not null default current_date,\n" +
                        "    qty int default 1,\n" +
                        "     primary key(encoded_item, sender, receiver)\n" +
                        ");");
                conn.createStatement().execute("create table if not exists inbox(\n" +
                        "    owner char(36) not null primary key ,\n" +
                        "    location varchar(128) not null\n" +
                        ");");
            } catch (Exception e) {
                Bukkit.getLogger().severe("Could not create tables!");
            }
        });
    }

    public SQLiteDataSource getDataSource() {
        return dataSource;
    }


    public Connection getConection() throws SQLException {
        return dataSource.getConnection();
    }
}
