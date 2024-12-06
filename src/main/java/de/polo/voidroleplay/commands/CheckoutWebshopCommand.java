package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
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

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

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
                Main.getInstance().getMySQL().insertAsync("INSERT INTO player_shop_claims (uuid, type, amount) VALUES (?, ?, ?)",
                        uuid,
                        args[1],
                        amount);
                break;
            case "premium":
                if (target != null) {
                    target.sendMessage("§8[§eShop§8]§a Du hast " + amount + " Tage Premium erhalten!");
                    playerManager.redeemRank(target, "premium", (int) amount, "d");
                    return false;
                }
                Main.getInstance().getMySQL().insertAsync("INSERT INTO player_shop_claims (uuid, type, amount) VALUES (?, ?, ?)",
                        uuid,
                        args[1],
                        amount);
                break;
            case "spende":
                OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(uuid);
                if (offlinePlayer == null) return false;
                Bukkit.broadcastMessage("§c ❤ §6" + offlinePlayer.getName() + " hat " + amount + "€ gespendet!");
                break;
            case "gameboost":
                if (target != null) {
                    target.sendMessage("§8[§eShop§8]§a Du hast " + amount + "h Gameboost erhalten!");
                    playerManager.addEXPBoost(target, (int) amount);
                    return false;
                }
                Main.getInstance().getMySQL().insertAsync("INSERT INTO player_shop_claims (uuid, type, amount) VALUES (?, ?, ?)",
                        uuid,
                        args[1],
                        amount);
                break;
        }

        return false;
    }

    @SneakyThrows
    private void logBuy(String uuid, String product, float amount) {
        Main.getInstance().getMySQL().insertAsync("INSERT INTO webshop_logs (uuid, product, amount) VALUES (?, ?, ?)",
                uuid,
                product,
                amount);
    }

    public void loadShopBuys(Player player) {
        CompletableFuture.supplyAsync(() -> {
            PlayerData playerData = playerManager.getPlayerData(player);
            String uuid = player.getUniqueId().toString().replace("-", "").toLowerCase();
            Main.getInstance().getMySQL().executeQueryAsync("SELECT * FROM player_shop_claims WHERE LOWER(uuid) = ?", uuid)
                    .thenApply(result -> {
                        for (java.util.Map<String, Object> stringObjectMap : result) {
                            switch (stringObjectMap.get("type").toString().toLowerCase()) {
                                case "coins":
                                    playerManager.addCoins(player, (int) stringObjectMap.get("amount"));
                                    player.sendMessage("§8[§eShop§8]§a Du hast " + stringObjectMap.get("amount") + " Coins erhalten!");
                                    break;
                                case "premium":
                                    playerManager.redeemRank(player, "premium", (int) stringObjectMap.get("amount"), "d");
                                    player.sendMessage("§8[§eShop§8]§a Du hast " + stringObjectMap.get("amount") + " Tage Premium erhalten!");
                                    break;
                                case "gameboost":
                                    try {
                                        playerManager.addEXPBoost(player, (int) stringObjectMap.get("amount"));
                                        player.sendMessage("§8[§eShop§8]§a Du hast " + stringObjectMap.get("amount") + "h Gameboost erhalten!");
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                    break;
                            }
                        }
                        return null;
                    });
            return null;
        });
    }
}
