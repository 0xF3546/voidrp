package de.polo.void_roleplay.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class discordCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        TextComponent c = new TextComponent("§9Discord öffnen");
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/99wCXkZeRx"));
        player.spigot().sendMessage(c);
        return false;
    }
}
