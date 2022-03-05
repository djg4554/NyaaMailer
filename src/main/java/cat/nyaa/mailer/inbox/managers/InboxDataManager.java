package cat.nyaa.mailer.inbox.managers;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.abstracts.Manager;
import cat.nyaa.mailer.inbox.Inbox;
import cat.nyaa.mailer.chest.exceptions.ChestNotFoundException;
import cat.nyaa.mailer.chest.exceptions.InboxRemovedException;
import cat.nyaa.mailer.chest.exceptions.LocationParseException;
import cat.nyaa.mailer.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@SuppressWarnings("SyntaxError")
public class InboxDataManager extends Manager<NyaaMailer> {

    protected InboxDataManager(NyaaMailer plugin) {
        super(plugin);
    }

    protected void insertOrUpdate(Player player, Inbox inbox) {
        CompletableFuture.runAsync(() -> {
            String serializedLocation = Utils.serializeLocation(inbox.getLocation());

            String query = "INSERT INTO inbox (owner, location) VALUES (?, ?) ON CONFLICT (owner) DO UPDATE SET location = ?";
            try (Connection connection = plugin.getSQLiteManager().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, serializedLocation);
                statement.setString(3, serializedLocation);
                statement.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Failed to add inbox for " + player.getName());
            }

        });

    }

    protected void delete(Player player) {
        CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM inbox WHERE owner = ?;";
            try (Connection connection = plugin.getSQLiteManager().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, player.getUniqueId().toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Failed to delete inbox for " + player.getName());
            }
        });
    }

    public CompletableFuture<Inbox> load(Player owner) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM inbox WHERE owner = ?";

            try (Connection connection = plugin.getSQLiteManager().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, owner.getUniqueId().toString());
                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) {
                    throw new CompletionException(new ChestNotFoundException());
                }

                String serializedLocation = resultSet.getString("location");
                Location location = Utils.parseLocation(serializedLocation);
                if (location == null) {
                    throw new CompletionException(new LocationParseException());
                }

                if (!location.isChunkLoaded()) {
                    location.getChunk().load();
                    location.getChunk().setForceLoaded(true);

                }

                if (location.getBlock().getState() instanceof Chest) {
                    return new Inbox((Chest) location.getBlock().getState(), location);
                } else {
                    delete(owner);
                    throw new CompletionException(new InboxRemovedException());
                }


            } catch (SQLException e) {
                Bukkit.getLogger().severe("Failed to get chest for " + owner.toString());
                throw new CompletionException(e);
            }
        });

    }

    public void unload(Player player, Inbox inbox) {
        insertOrUpdate(player, inbox);
    }


}
