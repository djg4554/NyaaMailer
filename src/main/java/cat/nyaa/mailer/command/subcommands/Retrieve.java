package cat.nyaa.mailer.command.subcommands;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.abstracts.SubCommand;
import org.bukkit.entity.Player;

public class Retrieve extends SubCommand<NyaaMailer> {
    public Retrieve(NyaaMailer plugin, Player player, String[] args) {
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
            case "confirm":
                plugin.getMailManager().processConfirm(sender);
                break;
            case "latest":
                plugin.getMailManager().processLatest(sender);
                break;
            case "chest":
                plugin.getMailManager().processChest(sender);
                break;
            default:
                sendUsage(sender);
                break;
                
        }

    }
    
    private void sendUsage(Player sender) {
        sender.sendMessage(plugin.componentColor("Usage: "));
        sender.sendMessage(plugin.componentColor("&7/mailer retrieve [confirm|latest|chest]&7- &r&cretrieve the items that you have received"));
        sender.sendMessage(plugin.componentColor("&a&l[confirm] &7- &r&cRetrieve all the items that you have received at your current location"));
        sender.sendMessage(plugin.componentColor("&a&l[latest] &7- &r&cRetrieve the latest items that you have received"));
        sender.sendMessage(plugin.componentColor("&a&l[chest] &7- &r&cRetrieve all the items that you have received in your inbox chest"));
    }
}
