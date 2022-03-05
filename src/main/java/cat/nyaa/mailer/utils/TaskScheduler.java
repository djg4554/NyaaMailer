package cat.nyaa.mailer.utils;

import cat.nyaa.mailer.NyaaMailer;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TaskScheduler {

    private final NyaaMailer plugin;

    public TaskScheduler(NyaaMailer plugin) {
        this.plugin = plugin;
    }

    public void startPlayerFeesTask() {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {

                plugin.getDataManager().getPlayersOutsideFreePeriod().whenComplete((players, throwable) -> {
                    if (throwable != null) {
                        plugin.getLogger().warning("Failed to get players outside free period: ");
                        return;
                    }

                    if (players.isEmpty()) {
                        return;
                    }
                    players.forEach((player, items) -> {
                        plugin.getLogger().info("Player " + player.getName() + " has " + items + " items outside free period.");
                        if (!plugin.getPlayerManager().canPayFee(player, items)) {
                            plugin.getDataManager().deletePlayerReceivedMails(player.getUniqueId());
                        } else {
                            plugin.getPlayerManager().payFee(player, items);
                        }

                    });

                });

            }
        },  Duration.between(LocalDateTime.now(), LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)).toMillis(), 20 * 60 * 60 * 24);
    }


}
