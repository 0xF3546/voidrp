package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.Gender;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class MarryCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public MarryCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("marry", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.getFaction().equalsIgnoreCase("Kirche")) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (playerData.getFactionGrade() < 4) {
            player.sendMessage(Prefix.ERROR + "Dieser Befehl geht erst ab Rang 4!");
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /marry [Spieler] [Spieler]");
            return false;
        }
        Player firstplayer = Bukkit.getPlayer(args[0]);
        Player secondplayer = Bukkit.getPlayer(args[1]);
        if (firstplayer == null) {
            player.sendMessage(Prefix.ERROR + args[0] + " wurde nicht gefunden.");
            return false;
        }
        if (secondplayer == null) {
            player.sendMessage(Prefix.ERROR + args[1] + " wurde nicht gefunden.");
            return false;
        }

        if (firstplayer.getLocation().distance(player.getLocation()) > 10) {
            player.sendMessage(Prefix.ERROR + firstplayer.getName() + " ist nicht in der nähe.");
            return false;
        }
        if (secondplayer.getLocation().distance(player.getLocation()) > 10) {
            player.sendMessage(Prefix.ERROR + secondplayer.getName() + " ist nicht in der nähe.");
            return false;
        }

        PlayerData firstplayerData = playerManager.getPlayerData(firstplayer);
        PlayerData secondplayerData = playerManager.getPlayerData(secondplayer);

        if (!firstplayerData.isChurch()) {
            player.sendMessage(Prefix.ERROR + firstplayer.getName() + " zahlt keine Kirchensteuer.");
            return false;
        }

        if (!secondplayerData.isChurch()) {
            player.sendMessage(Prefix.ERROR + secondplayer.getName() + " zahlt keine Kirchensteuer.");
            return false;
        }
        if (secondplayer == player || firstplayer == player) {
            player.sendMessage(Prefix.ERROR + "Du kannst dich nicht selbst heiraten!");
            return false;
        }
        if (firstplayerData.getRelationShip() == null || secondplayerData.getRelationShip() == null) {
            player.sendMessage(Prefix.ERROR + "Einer der beiden Spieler ist in keiner Beziehung.");
            return false;
        }
        if (firstplayerData.getRelationShip().get(secondplayer.getUniqueId().toString()).equals("verlobt")) {
            if (secondplayerData.getRelationShip().get(firstplayer.getUniqueId().toString()).equals("verlobt")) {
                if (firstplayerData.getGender() == secondplayerData.getGender()) {
                    player.sendMessage(Main.error + "Personen mit dem gleichen Geschlecht können nicht heiraten.");
                    return false;
                }
                firstplayer.sendMessage("§6Du und " + secondplayer.getName() + " sind jetzt verheiratet.");
                secondplayer.sendMessage("§6Du und " + firstplayer.getName() + " sind jetzt verheiratet.");
                Bukkit.broadcastMessage("§8[§6Kirche§8]§e " + firstplayer.getName() + " & " + secondplayer.getName() + " sind jetzt Verheiratet. Herzlichen Glückwunsch!");
                HashMap<String, String> hmap1 = new HashMap<>();
                hmap1.put(firstplayer.getUniqueId().toString(), "verheiratet");
                secondplayerData.getRelationShip().clear();
                secondplayerData.setRelationShip(hmap1);

                HashMap<String, String> hmap2 = new HashMap<>();
                hmap2.put(secondplayer.getUniqueId().toString(), "verheiratet");
                Main.getInstance().beginnerpass.didQuest(firstplayer, 20);
                Main.getInstance().beginnerpass.didQuest(secondplayer, 20);
                firstplayerData.getRelationShip().clear();
                firstplayerData.setRelationShip(hmap2);
                if (firstplayerData.getGender().equals(Gender.MALE)) {
                    firstplayerData.setLastname(firstplayerData.getLastname());
                    secondplayer.sendMessage("§8 » §7Dein Nachname lautet nun \"" + firstplayerData.getLastname() + "\".");
                } else {
                    firstplayerData.setLastname(secondplayerData.getLastname());
                    firstplayer.sendMessage("§8 » §7Dein Nachname lautet nun \"" + firstplayerData.getLastname() + "\".");
                }
                try {
                    Statement statement = Main.getInstance().mySQL.getStatement();
                    JSONObject object = new JSONObject(firstplayerData.getRelationShip());
                    statement.executeUpdate("UPDATE `players` SET `relationShip` = '" + object + "', `lastname` = '" + firstplayerData.getLastname() + "' WHERE `uuid` = '" + firstplayer.getUniqueId() + "'");

                    JSONObject object2 = new JSONObject(secondplayerData.getRelationShip());
                    statement.executeUpdate("UPDATE `players` SET `relationShip` = '" + object2 + "', `lastname` = '" + secondplayerData.getLastname() + "'  WHERE `uuid` = '" + secondplayer.getUniqueId() + "'");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                player.sendMessage(Main.error + secondplayer.getName() + " & " + firstplayer.getName() + " sind nicht verlobt.");
            }
        } else {
            player.sendMessage(Main.error + firstplayer.getName() + " & " + secondplayer.getName() + " seid nicht verlobt.");
        }
        return false;
    }
}
