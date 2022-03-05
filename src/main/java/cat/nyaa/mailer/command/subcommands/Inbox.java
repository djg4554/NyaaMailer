package cat.nyaa.mailer.command.subcommands;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.abstracts.SubCommand;
import org.bukkit.entity.Player;

public class Inbox extends SubCommand<NyaaMailer> {

    public Inbox(NyaaMailer plugin, Player player, String[] args) {
        super(plugin, player, args);


    }

    @Override
    public void execute() {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        String subCommand = args[1];
        switch (subCommand) {
            case "set":
                plugin.getSelectorManager().processInboxSelection(sender);
                break;
            case "remove":
                sender.sendMessage(plugin.getMessage("inbox-removed", "&aYour inbox has been removed."));
                plugin.getInboxManager().removeInbox(sender);
                break;
            default:
                sendUsage(sender);
                break;

        }

    }

    private void sendUsage(Player sender) {
        sender.sendMessage(plugin.componentColor("&7/mailer inbox [set|remove]&7- &r&cManage your inbox"));
        sender.sendMessage(plugin.componentColor("&a&l[set] &7- &r&cSet your inbox to a chest."));
        sender.sendMessage(plugin.componentColor("&a&l[remove] &7- &r&cRemove your inbox"));

    }
}
