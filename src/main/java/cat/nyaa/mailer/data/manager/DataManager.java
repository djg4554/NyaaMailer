package cat.nyaa.mailer.data.manager;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.utils.FeeType;
import cat.nyaa.mailer.utils.ItemManager;
import cat.nyaa.mailer.mail.Mail;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"DuplicatedCode", "SyntaxError"})
public class DataManager {

    private final NyaaMailer plugin;

    public DataManager(NyaaMailer plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Boolean> hasAlredySentItem(String item, UUID sender, UUID receiver) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT * FROM `item` WHERE `encoded_item` = ? AND `sender` = ? AND `receiver` = ?";
            try (Connection connection = plugin.getSQLiteManager().getConection(); PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, item);
                statement.setString(2, sender.toString());
                statement.setString(3, receiver.toString());
                ResultSet rs = statement.executeQuery();
                return rs.next();

            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to check if item has already been sent");
                throw new CompletionException(e);
            }
        });

    }

    public CompletableFuture<Void> sendItem(String item, UUID sender, UUID receiver) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO `item` (`encoded_item`, `sender`, `receiver`) VALUES (?, ?, ?) ON CONFLICT(`encoded_item`, `sender`, `receiver`) DO UPDATE SET qty = qty + 1";
            try (Connection connection = plugin.getSQLiteManager().getConection(); PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, item);
                statement.setString(2, sender.toString());
                statement.setString(3, receiver.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }


    public void purge(UUID uniqueId) {
        CompletableFuture.runAsync(() -> {
            String query = "DElETE FROM `item` WHERE `sender` = ? OR `receiver` = ?";
            try (Connection connection = plugin.getSQLiteManager().getConection(); PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uniqueId.toString());
                statement.setString(2, uniqueId.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to purge player");
            }
        });
    }


    public CompletableFuture<Integer> checkMail(UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            int count = 0;
            String query = "SELECT sum(qty) AS total FROM `item` WHERE `receiver` = ?";
            try (Connection connection = plugin.getSQLiteManager().getConection(); PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uniqueId.toString());
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to check mail");
            }
            return 0;
        });
    }

    public CompletableFuture<ArrayList<Mail>> retrieveAllMails(UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT * FROM `item` WHERE `receiver` = ?;";
            ArrayList<Mail> mailList = new ArrayList<>();
            try (Connection connection = plugin.getSQLiteManager().getConection(); PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uniqueId.toString());
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    ItemStack item = ItemManager.decodeItem(resultSet.getString("encoded_item"));
                    int qty = resultSet.getInt("qty");
                    OfflinePlayer sender = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("sender")));
                    OfflinePlayer receiver = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("receiver")));
                    Date date = Date.valueOf(resultSet.getString("mail_date"));
                    mailList.add(new Mail(sender, receiver, item, date, qty));
                }

            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to retrieve all items");
                throw new CompletionException(e);
            }
            return mailList;
        });
    }

    public void deletePlayerReceivedMails(UUID uniqueId) {
        CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM `item` WHERE `receiver` = ?";
            try (Connection connection = plugin.getSQLiteManager().getConection(); PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uniqueId.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to delete player received mail");
            }
        });
    }


    public CompletionStage<Mail> retrieveLatestItem(UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT * FROM `item` WHERE `receiver` = ? ORDER BY `mail_date` DESC LIMIT 1";
            try (Connection connection = plugin.getSQLiteManager().getConection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uniqueId.toString());
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    ItemStack item = ItemManager.decodeItem(resultSet.getString("encoded_item"));
                    int qty = resultSet.getInt("qty");
                    OfflinePlayer sender = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("sender")));
                    OfflinePlayer receiver = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("receiver")));
                    Date date = Date.valueOf(resultSet.getString("mail_date"));
                    return new Mail(sender, receiver, item, date, qty);
                } else {
                    return null;
                }
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to retrieve latest item for player");
                throw new CompletionException(e);
            }
        });
    }

    public void updateMail(Mail mail) {
        CompletableFuture.runAsync(() -> {
            String query = "UPDATE `item` SET `qty` = ? WHERE `sender` = ? AND `receiver` = ? AND `encoded_item` = ?";
            try (Connection connection = plugin.getSQLiteManager().getConection(); PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, mail.getQty());
                statement.setString(2, mail.getSender().getUniqueId().toString());
                statement.setString(3, mail.getReceiver().getUniqueId().toString());
                statement.setString(4, ItemManager.encodeItem(mail.getItemStack()));
                statement.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to update mail");
            }

        });
    }

    public void deleteMail(Mail mail) {
        CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM `item` WHERE `sender` = ? AND `receiver` = ? AND `encoded_item` = ?";
            try (Connection connection = plugin.getSQLiteManager().getConection(); PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, mail.getSender().getUniqueId().toString());
                statement.setString(2, mail.getReceiver().getUniqueId().toString());
                statement.setString(3, ItemManager.encodeItem(mail.getItemStack()));
                statement.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to delete mail");
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> updateOldMail(Mail mail, ItemStack itemStack) {
        return CompletableFuture.runAsync(() -> {
            String query = "UPDATE `item` SET `encoded_item` = ? WHERE `sender` = ? AND `receiver` = ? AND `encoded_item` = ? ON CONFLICT(`sender`, `receiver`, `encoded_item`) DO UPDATE SET `qty` = qty +1";
            try (Connection connection = plugin.getSQLiteManager().getConection(); PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, ItemManager.encodeItem(itemStack));
                statement.setString(2, mail.getSender().getUniqueId().toString());
                statement.setString(3, mail.getReceiver().getUniqueId().toString());
                statement.setString(4, ItemManager.encodeItem(mail.getItemStack()));
                statement.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to update old mail");
                throw new CompletionException(e);

            }
        });
    }

    public void saveMails(UUID uniqueId, ArrayList<Mail> sent) {
        CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO `item` (`sender`, `receiver`, `encoded_item`, `qty`, mail_date) VALUES (?, ?, ?, ?, ?)";
            try (Connection connection = plugin.getSQLiteManager().getConection(); PreparedStatement statement = connection.prepareStatement(query)) {
                for (Mail mail : sent) {
                    statement.setString(1, uniqueId.toString());
                    statement.setString(2, mail.getReceiver().getUniqueId().toString());
                    statement.setString(3, ItemManager.encodeItem(mail.getItemStack()));
                    statement.setInt(4, mail.getQty());
                    statement.setDate(5, mail.getDate());
                    statement.executeUpdate();

                }
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to save mails");
            }

        });
    }

    public void sendMail(Mail currentMail) {
        CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO `item` (`sender`, `receiver`, `encoded_item`, `qty`, mail_date) VALUES (?, ?, ?, ?, ?)";
            try (Connection connection = plugin.getSQLiteManager().getConection(); PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, currentMail.getSender().getUniqueId().toString());
                statement.setString(2, currentMail.getReceiver().getUniqueId().toString());
                statement.setString(3, ItemManager.encodeItem(currentMail.getItemStack()));
                statement.setInt(4, currentMail.getQty());
                statement.setDate(5, currentMail.getDate());
                statement.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to send mail");
            }
        });
    }

    public CompletableFuture<ConcurrentHashMap<OfflinePlayer, Long>> getPlayersOutsideFreePeriod() {
        return CompletableFuture.supplyAsync(() -> {
            ConcurrentHashMap<OfflinePlayer, Long> players = new ConcurrentHashMap<>();

            String query = "select receiver, sum(qty) as tot_qty\n" +
                    "from item\n" +
                    "where date(mail_date, '+? day') > date('now')\n" +
                    "group by receiver;";
            try (Connection connection = plugin.getSQLiteManager().getConection(); PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, plugin.getConfig().getInt("storage-free-days", 7));
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("receiver")));
                    if (player != null) {
                        players.put(player, resultSet.getLong("tot_qty"));
                    }
                }
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to get players outside free period");
            }
            return players;
        });
    }
}
