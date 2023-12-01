package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AktenCommand implements CommandExecutor {
    private PlayerManager playerManager;
    public AktenCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("akten", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFaction().equals("FBI") || playerData.getFaction().equals("Polizei")) {
            player.sendMessage("§7   ===§8[§9Offene Akten§8]§7===");
            HashMap<OfflinePlayer, Integer> hafteinheiten = new HashMap<>();
            try {
                Statement statement = Main.getInstance().mySQL.getStatement();
                ResultSet result = statement.executeQuery("SELECT * FROM player_akten");
                while (result.next()) {
                    OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString(result.getString(2)));
                    if (player1.isOnline()) {
                        if (hafteinheiten.get(player1) != null) {
                            hafteinheiten.replace(player1, hafteinheiten.get(player1) + result.getInt(5));
                        } else {
                            hafteinheiten.put(player1, result.getInt(5));
                        }
                    }
                }
                for (Map.Entry<OfflinePlayer, Integer> entry : hafteinheiten.entrySet()) {
                    player.sendMessage("§8 ➥ §3" + entry.getKey().getName() + "§8 - §3" + entry.getValue() + " Hafteinheiten");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
