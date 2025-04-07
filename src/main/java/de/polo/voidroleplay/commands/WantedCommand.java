package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.WantedReason;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
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
            Main.getInstance()
                    .getCoreDatabase()
                    .queryThreaded("SELECT * FROM player_wanteds")
                    .thenAcceptAsync(result -> {
                        try {
                            int i = 0;
                            while (result.next()) {
                                System.out.println("Found: " + result);
                                WantedReason wantedReason = utils.staatUtil.getWantedReason(result.resultSet().getInt("wantedId"));
                                Player player1 = Bukkit.getPlayer(UUID.fromString(result.resultSet().getString("uuid")));
                                if (player1 == null) continue;
                                i++;
                                player.sendMessage("§cGesucht! §8- §9" + player1.getName() + " §8-§9 " + wantedReason.getReason() + " §8-§9 " + wantedReason.getWanted() + " Wanteds");
                            }
                            if (i == 0) {
                                player.sendMessage("§9Es steht niemand auf der Fahndungsliste.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            result.close();
                        }
                    });
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }
}
