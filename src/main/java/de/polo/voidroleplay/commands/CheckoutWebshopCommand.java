package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CheckoutWebshopCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    public CheckoutWebshopCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("checkout-webshop", this);
    }
    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            return false;
        }
        String uuid = args[0];
        float amount = Float.parseFloat(args[2]);
        Player target = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId().toString().replace("-", "").equalsIgnoreCase(uuid)) target = player;
        }
        logBuy(uuid, args[1], amount);
        switch (args[1].toLowerCase()) {
            case "coins":
                if (target != null) {
                    target.sendMessage("§8[§eShop§8]§a Du hast " + amount + " Coins erhalten!");
                    playerManager.addCoins(target, (int) amount);
                    return false;
                }
                Connection connection = Main.getInstance().mySQL.getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE players SET coins = coins + ? WHERE REPLACE(uuid, '-', '') = ?");
                statement.setInt(1, (int) amount);
                statement.setString(2, uuid);
                statement.executeUpdate();
                statement.close();
                connection.close();
                break;
            case "spende":
                OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(uuid.toString());
                if (offlinePlayer == null) return false;
                Bukkit.broadcastMessage("§c ❤ §6" + offlinePlayer.getName() + " hat " + amount + "€ gespendet!");
                break;
        }

        return false;
    }

    @SneakyThrows
    private void logBuy(String uuid, String product, float amount) {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO webshop_logs (uuid, product, amount) VALUES (?, ?, ?)");
        statement.setString(1, uuid);
        statement.setString(2, product);
        statement.setFloat(3, amount);
        statement.execute();
        statement.close();
        connection.close();
    }
}
