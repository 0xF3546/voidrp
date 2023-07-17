package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.BusinessData;
import de.polo.metropiacity.DataStorage.FactionData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.BusinessManager;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.VertragUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class bizinviteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getBusiness() != null) {
            BusinessData businessData = BusinessManager.businessDataMap.get(playerData.getBusiness());
            if (playerData.getBusiness_grade() >= 4) {
                if (args.length >= 1) {
                    Player targetplayer = Bukkit.getPlayer(args[0]);
                    if (targetplayer != null) {
                        if (player.getLocation().distance(targetplayer.getLocation()) <= 5) {
                            if (PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString()).getBusiness() == null) {
                                if (BusinessManager.getMemberCount(playerData.getBusiness()) < businessData.getMaxMember()) {
                                    try {
                                        if (VertragUtil.setVertrag(player, targetplayer, "business_invite", playerData.getBusiness())) {
                                            player.sendMessage("§8[§6Business§8] §7" + targetplayer.getName() + " wurde in das Business §aeingeladen§7.");
                                            targetplayer.sendMessage("§6" + player.getName() + " hat dich in das Business §e" + playerData.getBusiness() + "§6 eingeladen.");
                                            VertragUtil.sendInfoMessage(targetplayer);
                                            PlayerData tplayerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
                                        } else {
                                            player.sendMessage("§8[§6Business§8]§8 §7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
                                        }
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    player.sendMessage(Main.error + "Dein Business ist voll!");
                                }
                            } else {
                                player.sendMessage("§8[§6Business§8] §c" + targetplayer.getName() + "§7 ist bereits in einem Business.");
                            }
                        } else {
                            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner nähe.");
                        }
                    } else {
                        player.sendMessage(Main.error + args[0] + " ist nicht online.");
                    }
                } else {
                    player.sendMessage(Main.error + "Syntax-Fehler: /bizinvite [Spieler]");
                }
            } else {
                player.sendMessage(Main.error_nopermission);
            }
        } else {
            player.sendMessage(Main.error + "Du bist in keinem Business");
        }
        return false;
    }
}
