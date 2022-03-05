package cat.nyaa.mailer.command;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.abstracts.MetaCommand;
import cat.nyaa.mailer.command.subcommands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MailCommand extends MetaCommand<NyaaMailer> {

    public MailCommand(NyaaMailer plugin) {
        super(plugin);
        plugin.getCommand("mailer").setExecutor(this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean isAdmin = playerHasPermission(sender, "mailer.admin");

        if (args.length < 1) {
            sendMainHelp(sender, isAdmin);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§c§lMailer §8┃ §7You must be a player to use this command");
            return true;
        }
        Player player = (Player) sender;
        switch (args[0].toLowerCase()) {
            case "send":
                new Send(plugin, player, args).execute();
                break;
            case "sendchest":
                new SendChest(plugin, player, args).execute();
                break;
            case "purge":
                if (isAdmin) new Purge(plugin, player, args).execute();
                break;
            case "check":
                new Check(plugin, player, args).execute();
                break;
            case "retrieve":
                new Retrieve(plugin, player, args).execute();
                break;
            case "inbox":
                new Inbox(plugin, player, args).execute();
                break;
            case "reload":
                if (isAdmin) {
                    plugin.load();
                    sender.sendMessage("§c§lNyaaMailer§8┃ §7Configuration reloaded");
                }
                break;
            default:
                sendMainHelp(sender, isAdmin);
                break;

        }


        return true;
    }


    private void sendMainHelp(CommandSender sender, boolean isAdmin) {
        sender.sendMessage(plugin.componentColor("&m&7      [&r&c&lNyaaMailer&m&7]      "));
        sender.sendMessage("");
        if (isAdmin) {

            sender.sendMessage(plugin.componentColor("&7/mailer purge [player] &7- &r&cPurge the items that the player sent and has not been received"));
            sender.sendMessage("");
            sender.sendMessage(plugin.componentColor("&7/mailer reload &7- &r&cReload config"));
            sender.sendMessage("");
        }
        sender.sendMessage(plugin.componentColor("&7/mailer send [player] &7- Send items in main hand to [player]"));
        sender.sendMessage("");
        sender.sendMessage(plugin.componentColor("&7/mailer sendchest [player] &7- Send items in chest to [player]"));
        sender.sendMessage("");
        sender.sendMessage(plugin.componentColor("&7/mailer check &7- &r&cCheck your mail"));
        sender.sendMessage("");
        sender.sendMessage(plugin.componentColor("&7/mailer retrieve [confirm|latest|chest]&7- &r&cretrieve the items that you have received"));
        sender.sendMessage(plugin.componentColor("&a&l[confirm] &7- &r&cRetrieve all the items that you have received at your current location"));
        sender.sendMessage(plugin.componentColor("&a&l[latest] &7- &r&cRetrieve the latest items that you have received"));
        sender.sendMessage(plugin.componentColor("&a&l[chest] &7- &r&cRetrieve all the items that you have received in your inbox chest"));
        sender.sendMessage("");
        sender.sendMessage(plugin.componentColor("&7/mailer inbox [set|remove]&7- &r&cManage your inbox"));
        sender.sendMessage(plugin.componentColor("&a&l[set] &7- &r&cSet your inbox to a chest."));
        sender.sendMessage(plugin.componentColor("&a&l[remove] &7- &r&cRemove your inbox"));
        sender.sendMessage("");
        sender.sendMessage(plugin.componentColor("&c/mailer help &7- &r&cShow this help"));
        sender.sendMessage("");
        sender.sendMessage(plugin.componentColor("&m&7      [&r&c&lNyaaMailer&m&7]      "));
    }

}
