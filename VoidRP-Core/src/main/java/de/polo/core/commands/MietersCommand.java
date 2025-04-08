package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.game.base.housing.House;
import de.polo.core.game.base.housing.HouseManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
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
    private final PlayerManager playerManager;
    private final Utils utils;

    public MietersCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("mieters", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (args.length >= 1) {
            House houseData = HouseManager.houseDataMap.get(Integer.parseInt(args[0]));
            if (houseData != null) {
                if (utils.houseManager.canPlayerInteract(player, Integer.parseInt(args[0]))) {
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
                    player.sendMessage(Prefix.ERROR + "Du kannst auf dieses Haus nicht zugreifen.");
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Dieses Haus wurde nicht gefunden.");
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /mieters [Haus]");
        }
        return false;
    }
}
