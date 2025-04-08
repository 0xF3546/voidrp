package de.polo.core.faction.commands;

import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.RoleplayItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Objects;

import static de.polo.core.Main.seasonpass;

public class ArrestCommand implements CommandExecutor {
    private final FactionManager factionManager;
    private final Utils utils;
    private final PlayerManager playerManager;

    public ArrestCommand(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        Main.registerCommand("arrest", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (Objects.equals(playerData.getFaction(), "FBI") || Objects.equals(playerData.getFaction(), "Polizei")) {
            if (!playerData.isDuty()) {
                player.sendMessage(Prefix.ERROR + "Du bist nicht im Dienst.");
            }
            Faction factionData = factionManager.getFactionData(playerData.getFaction());
            if (args.length > 0) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (targetplayer != null) {
                    PlayerData targetPlayerData = playerManager.getPlayerData(targetplayer);
                    if (targetPlayerData.isCuffed()) {
                        if (player.getLocation().distance(targetplayer.getLocation()) <= 5) {
                            try {
                                if (utils.staatUtil.arrestPlayer(targetplayer, player, false)) {
                                    if (targetPlayerData.isAduty()) {
                                        player.sendMessage(Prefix.ERROR + "Spieler im Admindienst kannst du nicht inhaftieren.");
                                        return false;
                                    }
                                    player.sendMessage("§" + factionData.getPrimaryColor() + factionData.getName() + "§8 » §7Du hast " + targetplayer.getName() + " §aerfolgreich§7 inhaftiert.");
                                    playerManager.addExp(player, Utils.random(15, 44));
                                    playerManager.setPlayerMove(targetplayer, true);
                                    targetPlayerData.setCuffed(false);
                                    player.getInventory().addItem(ItemManager.createItem(RoleplayItem.CUFF.getMaterial(), 1, 0, RoleplayItem.CUFF.getDisplayName()));
                                    seasonpass.didQuest(targetplayer, 8);
                                } else {
                                    player.sendMessage(Prefix.ERROR + targetplayer.getName() + " wird nicht gesucht.");
                                }
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in deiner nähe.");
                        }
                    } else {
                        player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in Handschellen.");
                    }
                } else {
                    player.sendMessage(Prefix.ERROR + "§c" + args[0] + " ist nicht online.");
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /arrest [Spieler]");
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }
}
