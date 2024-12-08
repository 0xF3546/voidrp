package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.WantedReason;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class WantedCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public WantedCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("wanteds", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (playerData.getFaction().equals("FBI") || playerData.getFaction().equals("Polizei")) {
            Main.getInstance().getMySQL().queryThreaded("SELECT * FROM player_wanteds").thenAccept(result -> {
                try {
                    int i = 0;
                    while (result.next()) { // Durchläuft alle Zeilen im ResultSet
                        System.out.println("Found: " + result);
                        WantedReason wantedReason = utils.staatUtil.getWantedReason(result.getInt("wantedId"));
                        OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString(result.getString("uuid")));
                        if (!player1.isOnline()) continue; // Nur anzeigen, wenn der Spieler online ist
                        i++;
                        player.sendMessage("§cGesucht! §8- §9" + player1.getName() + " §8-§9 " + wantedReason.getReason() + " §8-§9 " + wantedReason.getWanted() + " Wanteds");
                    }
                    if (i == 0) {
                        player.sendMessage("§9Es steht niemand auf der Fahndungsliste.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
