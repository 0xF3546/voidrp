package de.polo.core.commands;

import de.polo.core.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscordCommand implements CommandExecutor {
    public DiscordCommand() {
        Main.registerCommand("discord", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        TextComponent c = new TextComponent("§9Discord öffnen");
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/void-roleplay"));
        player.spigot().sendMessage(c);
        return false;
    }
}
