package de.polo.void_roleplay.Utils;


import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.ClientProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;
import de.polo.void_roleplay.DataStorage.FactionData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.DataStorage.RankData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

public class TeamSpeak implements CommandExecutor {
    static TS3Api api = null;
    static TS3Query query = new TS3Query();
    public static HashMap<String, Client> verifyCodes = new HashMap<>();



    public static void loadConfig() {
        System.out.println("Lade TS3 config...");
        final TS3Config config = new TS3Config();
        config.setHost("91.212.121.55");

        query.connect();
        System.out.println("Query connection: " + query.isConnected());

        api = query.getApi();
        api.login("tsquery", "xKwmHgQU");
        api.selectVirtualServerById(1);
        api.setNickname("ts0433");
        api.sendChannelMessage("Bot gestartet");
    }

    public static TS3Api getAPI() {
        return api;
    }

    public static TS3Query getQuery() {
        return query;
    }

    public static void removeClientGroups(Client client) {
        List<ServerGroup> serverGroups = TeamSpeak.getAPI().getServerGroupsByClientId(client.getDatabaseId());
        for (ServerGroup serverGroup : serverGroups) {
            TeamSpeak.getAPI().removeClientFromServerGroup(serverGroup.getId(), client.getDatabaseId());
        }
        for (FactionData factionData: FactionManager.factionDataMap.values()) {
            getAPI().setClientChannelGroup(8, factionData.getChannelGroupID(), client.getDatabaseId());
        }
    }

    public static void updateClientGroup(Player player, Client client) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        List<ServerGroup> serverGroups = TeamSpeak.getAPI().getServerGroupsByClientId(client.getDatabaseId());
        for (ServerGroup serverGroup : serverGroups) {
            TeamSpeak.getAPI().removeClientFromServerGroup(serverGroup.getId(), client.getDatabaseId());
        }
        for (FactionData factionData: FactionManager.factionDataMap.values()) {
            getAPI().setClientChannelGroup(8, factionData.getChannelGroupID(), client.getDatabaseId());
        }
        getAPI().editClient(client.getId(), ClientProperty.CLIENT_DESCRIPTION, player.getName());
        if (playerData.getFaction() != null && !playerData.getFaction().equals("Zivilist")) {
            FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
            RankData spielerRang = ServerManager.rankDataMap.get("Spieler");
            getAPI().addClientToServerGroup(spielerRang.getTeamSpeakID(), client.getDatabaseId());
            if (playerData.getFactionGrade() >= 7) {
                getAPI().setClientChannelGroup(9, factionData.getChannelGroupID(), client.getDatabaseId());
            } else {
                getAPI().setClientChannelGroup(10, factionData.getChannelGroupID(), client.getDatabaseId());
            }
            if (!playerData.getRang().equals("Spieler")) {
                RankData rankData = ServerManager.rankDataMap.get(playerData.getRang());
                getAPI().addClientToServerGroup(rankData.getTeamSpeakID(), client.getDatabaseId());
            }
            getAPI().addClientToServerGroup(factionData.getTeamSpeakID(), client.getDatabaseId());
            getAPI().sendPrivateMessage(client.getId(), "Deine TeamSpeak-Rechte wurden aktuallisiert.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            Client client = verifyCodes.get(args[0]);
            if (client != null) {
                PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
                getAPI().sendPrivateMessage(client.getId(), "Du bist nun verifiziert!");
                player.sendMessage("§8[§3TeamSpeak§8]§b Du bist nun verifiziert!");
                playerData.setTeamSpeakUID(client.getUniqueIdentifier());
                getAPI().editClient(client.getId(), ClientProperty.CLIENT_DESCRIPTION, player.getName());
                if (playerData.getFaction() != null && !playerData.getFaction().equals("Zivilist")) {
                    FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
                    verifyCodes.remove(args[0]);
                    RankData spielerRang = ServerManager.rankDataMap.get("Spieler");
                    getAPI().addClientToServerGroup(spielerRang.getTeamSpeakID(), client.getDatabaseId());
                    if (playerData.getFactionGrade() >= 7) {
                        getAPI().setClientChannelGroup(9, factionData.getChannelGroupID(), client.getDatabaseId());
                        getAPI().sendPrivateMessage(client.getId(), "Dir wurden Leaderrechte für " + playerData.getFaction() + " gegeben!");
                    } else {
                        getAPI().setClientChannelGroup(10, factionData.getChannelGroupID(), client.getDatabaseId());
                        getAPI().sendPrivateMessage(client.getId(), "Dir wurden Memberrechte für " + playerData.getFaction() + " gegeben!");
                    }
                    if (!playerData.getRang().equals("Spieler")) {
                        RankData rankData = ServerManager.rankDataMap.get(playerData.getRang());
                        getAPI().addClientToServerGroup(rankData.getTeamSpeakID(), client.getDatabaseId());
                    }
                    getAPI().addClientToServerGroup(factionData.getTeamSpeakID(), client.getDatabaseId());
                }
                try {
                    Statement statement = MySQL.getStatement();
                    statement.executeUpdate("UPDATE `players` SET `teamSpeakUID` = '" + client.getUniqueIdentifier() + "' WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                player.sendMessage(Main.error + "Der Code konnte nicht gefunden werden.");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /verify [Code]");
        }
        return false;
    }
}
