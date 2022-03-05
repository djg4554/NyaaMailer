package cat.nyaa.mailer.command.subcommands;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.abstracts.SubCommand;
import org.bukkit.entity.Player;

public class Check extends SubCommand<NyaaMailer> {

    public Check(NyaaMailer plugin, Player sender, String[] args) {
        super(plugin, sender, args);
    }

    @Override
    public void execute() {
        plugin.getMailManager().checkMail(sender).thenAccept(itemsNumber -> {
            sender.sendMessage(plugin.getMessage("mail-check-result", "&aYou have %amount% new items!").replace("%amount%", ""+itemsNumber));
        });
    }
}
