package de.polo.voidroleplay.utils;


import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.ClientProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.RankData;
import de.polo.voidroleplay.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

public class TeamSpeak implements CommandExecutor {
    static TS3Api api;
    static final TS3Query query = new TS3Query();
    public static final HashMap<String, Client> verifyCodes = new HashMap<>();
    static int GastChannel = 9;
    private static TeamSpeak teamSpeak;
    private final Utils utils;
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    public TeamSpeak(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.utils = utils;
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        init();
    }
    private void init() {
        teamSpeak = this;
        System.out.println("Lade TS3 config...");
        final TS3Config config = new TS3Config();
        config.setHost("voidroleplay.de").setFloodRate(TS3Query.FloodRate.DEFAULT).setQueryPort(10011);
        query.connect();
        if (!query.isConnected()) {
            System.out.println("[TS3Bot] Query konnte nicht verbunden werden.");
            return;
        }
        api = query.getApi();
        api.login("serveradmin", "M7FxPPB5");
        api.selectVirtualServerById(1);
        api.setNickname("VoidRoleplay");
    }
    public static TeamSpeak getTeamSpeak() {
        return teamSpeak;
    }

    public static void loadConfig() {
        System.out.println("Lade TS3 config...");
        final TS3Config config = new TS3Config();
        config.setHost("37.221.92.65").setFloodRate(TS3Query.FloodRate.DEFAULT).setQueryPort(10011);
        query.connect();
        if (!query.isConnected()) {
            System.out.println("[TS3Bot] Query konnte nicht verbunden werden.");
            return;
        }
        api = query.getApi();
        api.login("serveradmin", "WrQzPS72");
        api.selectVirtualServerById(2);
        api.setNickname("VoidRoleplay");
    }

    public TS3Api getAPI() {
        return api;
    }

    public void shutdown() {
        api.logout();
        query.exit();
    }

    public static TS3Query getQuery() {
        return query;
    }

    public void removeClientGroups(Client client) {
        List<ServerGroup> serverGroups = getAPI().getServerGroupsByClientId(client.getDatabaseId());
        for (ServerGroup serverGroup : serverGroups) {
            if (client.isInServerGroup(serverGroup.getId())) getAPI().removeClientFromServerGroup(serverGroup.getId(), client.getDatabaseId());
        }
        for (FactionData factionData : factionManager.getFactions()) {
            getAPI().setClientChannelGroup(9, factionData.getChannelGroupID(), client.getDatabaseId());
        }
    }

    public void updateClientGroup(Player player, Client client) {
        utils.sendActionBar(player, "§aSynchronisiere TeamSpeak-Daten.");
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        List<ServerGroup> serverGroups = getAPI().getServerGroupsByClientId(client.getDatabaseId());
        for (ServerGroup serverGroup : serverGroups) {
            getAPI().removeClientFromServerGroup(serverGroup.getId(), client.getDatabaseId());
        }
        utils.sendActionBar(player, "§aSynchronisiere TeamSpeak-Daten..");
        for (FactionData factionData : factionManager.getFactions()) {
            getAPI().setClientChannelGroup(9, factionData.getChannelGroupID(), client.getDatabaseId());
        }
        utils.sendActionBar(player, "§aSynchronisiere TeamSpeak-Daten...");
        getAPI().editClient(client.getId(), ClientProperty.CLIENT_DESCRIPTION, player.getName());
        RankData spielerRang = ServerManager.rankDataMap.get("Spieler");
        getAPI().addClientToServerGroup(spielerRang.getTeamSpeakID(), client.getDatabaseId());
        if (playerData.getFaction() != null && !playerData.getFaction().equals("Zivilist")) {
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            if (playerData.getFactionGrade() >= 7) {
                getAPI().setClientChannelGroup(10, factionData.getChannelGroupID(), client.getDatabaseId());
            } else {
                getAPI().setClientChannelGroup(11, factionData.getChannelGroupID(), client.getDatabaseId());
            }
            getAPI().addClientToServerGroup(factionData.getTeamSpeakID(), client.getDatabaseId());
        }
        utils.sendActionBar(player, "§aSynchronisiere TeamSpeak-Daten....");
        if (playerData.getSecondaryTeam() != null) {
            RankData rankData = ServerManager.rankDataMap.get(playerData.getSecondaryTeam());
            getAPI().addClientToServerGroup(rankData.getTeamSpeakID(), client.getDatabaseId());
        }
        utils.sendActionBar(player, "§aSynchronisiere TeamSpeak-Daten.....");
        if (!playerData.getRang().equals("Spieler")) {
            RankData rankData = ServerManager.rankDataMap.get(playerData.getRang());
            getAPI().addClientToServerGroup(rankData.getTeamSpeakID(), client.getDatabaseId());
        }
        utils.sendActionBar(player, "§a§lDaten Synchronisiert!");
        getAPI().sendPrivateMessage(client.getId(), "Deine TeamSpeak-Rechte wurden aktuallisiert.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        Main.registerCommand("verify", this);
        if (args.length >= 1) {
            Client client = verifyCodes.get(args[0]);
            if (client != null) {
                verifyCodes.remove(args[0]);
                PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
                getAPI().sendPrivateMessage(client.getId(), "Du bist nun verifiziert!");
                player.sendMessage("§8[§3TeamSpeak§8]§b Du bist nun verifiziert!");
                playerData.setTeamSpeakUID(client.getUniqueIdentifier());
                getAPI().editClient(client.getId(), ClientProperty.CLIENT_DESCRIPTION, player.getName());
                RankData spielerRang = ServerManager.rankDataMap.get("Spieler");
                getAPI().addClientToServerGroup(spielerRang.getTeamSpeakID(), client.getDatabaseId());
                if (playerData.getFaction() != null && !playerData.getFaction().equals("Zivilist")) {
                    FactionData factionData = factionManager.getFactionData(playerData.getFaction());
                    if (playerData.getFactionGrade() >= 7) {
                        getAPI().setClientChannelGroup(10, factionData.getChannelGroupID(), client.getDatabaseId());
                        getAPI().sendPrivateMessage(client.getId(), "Dir wurden Leaderrechte für " + playerData.getFaction() + " gegeben!");
                    } else {
                        getAPI().setClientChannelGroup(11, factionData.getChannelGroupID(), client.getDatabaseId());
                        getAPI().sendPrivateMessage(client.getId(), "Dir wurden Memberrechte für " + playerData.getFaction() + " gegeben!");
                    }
                    getAPI().addClientToServerGroup(factionData.getTeamSpeakID(), client.getDatabaseId());
                }
                if (playerData.getSecondaryTeam() != null) {
                    RankData rankData = ServerManager.rankDataMap.get(playerData.getSecondaryTeam());
                    getAPI().addClientToServerGroup(rankData.getTeamSpeakID(), client.getDatabaseId());
                }
                if (!playerData.getRang().equals("Spieler")) {
                    RankData rankData = ServerManager.rankDataMap.get(playerData.getRang());
                    getAPI().addClientToServerGroup(rankData.getTeamSpeakID(), client.getDatabaseId());
                }
                try {
                    Statement statement = Main.getInstance().mySQL.getStatement();
                    statement.executeUpdate("UPDATE `players` SET `teamSpeakUID` = '" + client.getUniqueIdentifier() + "' WHERE `uuid` = '" + player.getUniqueId() + "'");
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
