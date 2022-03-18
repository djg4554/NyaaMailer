package cat.nyaa.mailer.command.subcommands;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.abstracts.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Purge extends SubCommand<NyaaMailer> {

    public Purge(NyaaMailer plugin, Player sender, String[] args) {
        super(plugin, sender, args);
    }

    @Override
    public void execute() {

        if (!(sender.isOp() || sender.hasPermission("mailer.admin"))) {
            sender.sendMessage(plugin.getMessage("player-no-permission", "You don't have permission to do this."));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: &7/mailer purge [player]");
            return;
        }

        OfflinePlayer receiver = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (!playerExists(receiver)) {
            sender.sendMessage(plugin.getMessage("player-not-found", "Player not found."));
            return;
        }

        assert receiver != null;
        plugin.getMailManager().purge(receiver);
        sender.sendMessage(plugin.getMessage("purge-success", "Purged all mail from %player%.").replace("%player%", receiver.getName()));
    }
}
