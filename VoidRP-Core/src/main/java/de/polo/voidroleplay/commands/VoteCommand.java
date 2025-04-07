package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteCommand implements CommandExecutor {
    public VoteCommand() {
        Main.registerCommand("vote", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        TextComponent mcservereu = new TextComponent("§2Vote §8» §7minecraft-server.eu");
        mcservereu.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://minecraft-server.eu/vote/index/22AB8/" + player.getName()));
        player.spigot().sendMessage(mcservereu);

        TextComponent mcserverlist = new TextComponent("§2Vote §8» §7minecraft-serverlist.net");
        mcserverlist.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.minecraft-serverlist.net/vote/57619/" + player.getName()));
        player.spigot().sendMessage(mcserverlist);
        return false;
    }
}
