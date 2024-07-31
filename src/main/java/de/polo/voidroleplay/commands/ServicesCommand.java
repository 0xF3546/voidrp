package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.ServiceData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.StaatUtil;
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
    private final PlayerManager playerManager;
    public ServicesCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("services", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFaction().equals("Medic") || playerData.getFaction().equals("Polizei")) {
            player.sendMessage("§7   ===§8[§9Notrufe§8]§7===");
            if (playerData.getFaction().equals("Medic")) {
                for (ServiceData serviceData : StaatUtil.serviceDataMap.values()) {
                    if (Bukkit.getPlayer(UUID.fromString(serviceData.getUuid())) == null) {
                        StaatUtil.serviceDataMap.remove(serviceData.getUuid());
                        continue;
                    }
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
