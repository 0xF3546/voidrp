package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.HouseData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.Housing;
import de.polo.metropiacity.utils.PlayerManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class MietersCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (args.length >= 1) {
            HouseData houseData = Housing.houseDataMap.get(Integer.parseInt(args[0]));
            if (houseData != null) {
                if (Housing.canPlayerInteract(player, Integer.parseInt(args[0]))) {
                    player.sendMessage("§7   ===§8[§6Haus " + args[0] + "§8]§7===");
                    player.sendMessage("§8 ➥ §eBesitzer§8:§7 " + Bukkit.getOfflinePlayer(UUID.fromString(houseData.getOwner())).getName());
                    for (Map.Entry<String, Integer> entry : houseData.getRenter().entrySet()) {
                        if (player.getUniqueId().toString().equals(houseData.getOwner())) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
                            TextComponent unrent = new TextComponent("§cKündigen");
                            unrent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/unrent " + offlinePlayer.getUniqueId() + " " + houseData.getNumber()));
                            unrent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§c§oKündigen")));
                            TextComponent message = new TextComponent("§8 ➥§e " + offlinePlayer.getName() + " (" + entry.getValue() + "$)§8 | ");
                            message.addExtra(unrent);
                            player.spigot().sendMessage(message);
                        } else {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
                            player.sendMessage("§8 ➥ §e" + offlinePlayer.getName());
                        }
                    }
                } else {
                    player.sendMessage(Main.error + "Du kannst auf dieses Haus nicht zugreifen.");
                }
            } else {
                player.sendMessage(Main.error + "Dieses Haus wurde nicht gefunden.");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /mieters [Haus]");
        }
        return false;
    }
}
