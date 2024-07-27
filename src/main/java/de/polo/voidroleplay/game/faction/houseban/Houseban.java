package de.polo.voidroleplay.game.faction.houseban;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Houseban implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private List<PlayerHouseban> housebans = new ArrayList<>();
    public Houseban(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("houseban", this);

        loadAll();
    }

    @SneakyThrows
    private void loadAll() {
        housebans.clear();
        PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("SELECT * FROM housebans");
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            PlayerHouseban playerHouseban = new PlayerHouseban(UUID.fromString(result.getString("uuid")), result.getString("reason"), Utils.toLocalDateTime(result.getDate("until")));
            playerHouseban.setId(result.getInt("id"));
            playerHouseban.setPunisher(UUID.fromString(result.getString("punisher")));
            housebans.add(playerHouseban);
        }
        statement.close();
    }
    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!factionManager.isPlayerInGoodFaction(player)) {
            player.sendMessage(Prefix.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage("§7   ===§8[§cHausverbote§8]§7===");
            for (PlayerHouseban playerHouseban : housebans) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerHouseban.getUuid());
                if (offlinePlayer.isOnline()) {
                    player.sendMessage("§8 ➥ §a" + offlinePlayer.getName() + " §8| §7" + playerHouseban.getUntil() + " §8 | §7" + playerHouseban.getReason());
                } else {
                    player.sendMessage("§8 ➥ §c" + offlinePlayer.getName() + " §8| §7" + playerHouseban.getUntil() + " §8 | §7" + playerHouseban.getReason());
                }
            }
            return false;
        }
        if (args.length < 3) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /houseban [add/remove] [Spieler] [Zeit] [Grund]");
        }
        if (args[0].equalsIgnoreCase("add")) {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (offlinePlayer.getName() == null) continue;
                if (offlinePlayer.getName().equalsIgnoreCase(args[0])) {
                    String time = args[2];
                    args[0] = "";
                    args[1] = "";
                    args[2] = "";
                    String reason = Utils.stringArrayToString(args);
                    factionManager.sendCustomMessageToFactions("§8[§cHausverbot§8]§7 " + player.getName() + " hat " + offlinePlayer.getName() + " Hausverbot erteilt. Grund: " + reason, "Medic", "FBI", "Polizei");
                    return false;
                }
            }
        }
        if (args[0].equalsIgnoreCase("remove")) {

        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        return null;
    }
}
