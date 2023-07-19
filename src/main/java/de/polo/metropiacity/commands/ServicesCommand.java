package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.DataStorage.ServiceData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.StaatUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ServicesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFaction().equals("Medic") || playerData.getFaction().equals("Polizei")) {
            player.sendMessage("§7   ===§8[§9Notrufe§8]§7===");
            if (playerData.getFaction().equals("Medic")) {
                for (ServiceData serviceData : StaatUtil.serviceDataMap.values()) {
                    if (serviceData.getNumber() == 112) {
                        TextComponent message = new TextComponent("§8 ➥ §3" + Bukkit.getPlayer(UUID.fromString(serviceData.getUuid())).getName() + " §8|§3 " + serviceData.getReason());
                        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§3§lNotruf annehmen")));
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptservice " + Bukkit.getPlayer(UUID.fromString(serviceData.getUuid())).getName()));
                        player.spigot().sendMessage(message);
                    }
                }
            }
            if (playerData.getFaction().equals("Polizei")) {
                for (ServiceData serviceData : StaatUtil.serviceDataMap.values()) {
                    if (serviceData.getNumber() == 110) {
                        TextComponent message = new TextComponent("§8 ➥ §3" + Bukkit.getPlayer(UUID.fromString(serviceData.getUuid())).getName() + " §8|§3 " + serviceData.getReason());
                        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§3§oNotruf annehmen")));
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptservice " + Bukkit.getPlayer(UUID.fromString(serviceData.getUuid())).getName()));
                        player.spigot().sendMessage(message);
                    }
                }
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
