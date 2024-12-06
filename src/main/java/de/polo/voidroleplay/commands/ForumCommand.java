package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.RankData;
import de.polo.voidroleplay.database.MySQL;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ForumCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;

    public ForumCommand(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        Main.registerCommand("forum", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /forum [link/unlink]");
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "link":
                try {
                    Statement statement = MySQL.forum.getStatement();
                    ResultSet res = statement.executeQuery("SELECT userID from wcf1_user WHERE username = '" + player.getName() + "'");
                    if (!res.next()) {
                        player.sendMessage(Main.error + "Es wurde kein Forum-Account mit dem Namen \"" + player.getName() + "\" gefunden.");
                        return false;
                    }
                    int forumID = res.getInt(1);
                    statement.execute("UPDATE wcf1_user SET activationCode = 0 WHERE userID = " + forumID);
                    Statement statement1 = Main.getInstance().mySQL.getStatement();
                    utils.sendActionBar(player, "§aVerknüpfe Forum & Minecraft...");
                    statement1.executeUpdate("UPDATE players SET forumID = " + forumID + " WHERE uuid = '" + player.getUniqueId() + "'");
                    utils.sendActionBar(player, "§aAccount freigeschaltet.");
                    playerData.setForumID(forumID);
                    utils.sendActionBar(player, "§aWeise Forum-Rechte zu...");
                    ArrayList<Integer> ranks = new ArrayList<>();
                    RankData spielerData = ServerManager.rankDataMap.get("Spieler");
                    ranks.add(spielerData.getForumID());
                    RankData rankData = ServerManager.rankDataMap.get(playerData.getRang());
                    if (rankData.getForumID() != spielerData.getForumID()) {
                        ranks.add(rankData.getForumID());
                    }
                    if (playerData.getFaction() != null) {
                        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
                        if (playerData.getFactionGrade() >= 5) {
                            ranks.add(factionData.getForumID_Leader());
                        } else {
                            ranks.add(factionData.getForumID());
                        }
                    }
                    statement.execute("DELETE FROM wcf1_user_to_group WHERE userID = " + playerData.getForumID());
                    statement.execute("INSERT INTO wcf1_user_to_group (userID, groupID) VALUES (" + playerData.getForumID() + ", 1)");
                    for (int i = 0; i < ranks.size(); i++) {
                        statement.execute("INSERT INTO wcf1_user_to_group (userID, groupID) VALUES (" + playerData.getForumID() + ", " + ranks.get(i) + ")");
                    }
                    utils.sendActionBar(player, "§aErfolgreich!");
                    player.sendMessage("§8[§6Forum§8]§a Du hast dein Forum-Account verknüpft & freigeschaltet.");

                } catch (SQLException e) {
                    player.sendMessage(Main.error + "Etwas ist schief gelaufen...");
                    throw new RuntimeException(e);
                }
                break;
            case "unlink":
                if (playerData.getForumID() == null) {
                    player.sendMessage("§8[§6Forum§8]§c Du hast keinen Forum-Account verknüpft.");
                    return false;
                }
                try {
                    Statement mcStatement = Main.getInstance().mySQL.getStatement();
                    mcStatement.executeUpdate("UPDATE players SET forumID = null WHERE uuid = '" + player.getName() + "'");
                    Statement wcfStatement = MySQL.forum.getStatement();
                    wcfStatement.execute("DELETE FROM wcf1_user_to_group WHERE userID = " + playerData.getForumID());
                    wcfStatement.execute("INSERT INTO wcf1_user_to_group (userID, groupID) VALUES (" + playerData.getForumID() + ", 1)");
                    playerData.setForumID(null);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
        }
        return false;
    }
}
