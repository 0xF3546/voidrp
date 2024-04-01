package de.polo.voidroleplay.utils;


import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.ClientProperty;
import com.github.theholywaffle.teamspeak3.api.event.*;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.RankData;
import de.polo.voidroleplay.Main;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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

        Main.registerCommand("verify", this);
    }

    @SneakyThrows
    public static void reloadPlayer(UUID uuid) {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(uuid);
        System.out.println("RELOADING " + uuid);
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT teamSpeakUID FROM players WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet result = statement.executeQuery();
        if (result.next()) {

            try {
                String jsonInputString = "{\"uid\": \"" + result.getString("teamSpeakUID") + "\"}";
                byte[] postData = jsonInputString.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;

                URL url = new URL("http://localhost:3010/teamspeak/reloaduser");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("charset", "utf-8");
                con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                con.setUseCaches(false);

                try (OutputStream os = con.getOutputStream()) {
                    os.write(postData);
                }

                int responseCode = con.getResponseCode();
                System.out.println("Response Code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("Reloaded user successfully.");
                } else {
                    System.out.println("Failed to reload user.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("PlayerData or TeamSpeakUID is null");
        }
    }

    public static void verifyUser(Player player, String uid) {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());

        try {
            String jsonInputString = "{\"uid\": \"" + uid + "\", \"name\": \"" + player.getName() + "\", \"uuid\": \"" + player.getUniqueId() + "\"}";
            byte[] postData = jsonInputString.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            URL url = new URL("http://localhost:3010/teamspeak/verify");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            con.setUseCaches(false);

            try (OutputStream os = con.getOutputStream()) {
                os.write(postData);
            }

            int responseCode = con.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Reloaded user successfully.");
            } else {
                System.out.println("Failed to reload user.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        teamSpeak = this;
        System.out.println("[TS3Bot] Lade TS3 config...");
        final TS3Config config = new TS3Config();
        config.setHost("voidroleplay.de").setFloodRate(TS3Query.FloodRate.DEFAULT).setQueryPort(10011);
        query.connect();
        if (!query.isConnected()) {
            System.out.println("[TS3Bot] Query konnte nicht verbunden werden.");
            return;
        }
        api = query.getApi();
        api.login("serveradmin", "PK5KQ9BK");
        api.selectVirtualServerById(1);
        api.setNickname("VoidRoleplay");
        loadEvents();
        System.out.println("[TS3Bot] es wurde alles erfolgreich geladen.");
    }

    public static TeamSpeak getTeamSpeak() {
        return teamSpeak;
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
            if (client.isInServerGroup(serverGroup.getId()))
                getAPI().removeClientFromServerGroup(serverGroup.getId(), client.getDatabaseId());
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
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /verify [Code]");
            return false;
        }
        Client client = verifyCodes.get(args[0]);
        if (client == null) {
            player.sendMessage(Main.error + "Der Code konnte nicht gefunden werden.");
            return false;
        }
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
        return false;
    }

    private void loadEvents() {
        api.registerAllEvents();
        api.addTS3Listeners(new TS3Listener() {
            @Override
            public void onTextMessage(TextMessageEvent e) {

            }

            @Override
            public void onClientJoin(ClientJoinEvent e) {
                int unregisteredGroupId = getUnregisteredGroupId(); // Methode, um die ID der "Nicht Registriert"-Gruppe zu erhalten
                if (unregisteredGroupId != -1) {
                    Client client = api.getClientInfo(e.getClientId());
                    int[] serverGroups = client.getServerGroups();
                    for (int groupId : serverGroups) {
                        if (groupId == unregisteredGroupId) {
                            api.sendPrivateMessage(e.getClientId(), "Hey! Du bist noch nicht verifiziert. Nutze Ingame \"/tslink " + client.getUniqueIdentifier() + "\"");
                            break; // Wir haben die unregistrierte Gruppe gefunden, daher können wir die Schleife beenden
                        }
                    }
                }
            }

            private int getUnregisteredGroupId() {
                for (ServerGroup group : api.getServerGroups()) {
                    if (group.getName().equalsIgnoreCase("Nicht Registriert")) {
                        return group.getId();
                    }
                }
                return -1; // Rückgabe -1, wenn die Gruppe nicht gefunden wurde
            }


            @Override
            public void onClientLeave(ClientLeaveEvent e) {

            }

            @Override
            public void onServerEdit(ServerEditedEvent e) {

            }

            @Override
            public void onChannelEdit(ChannelEditedEvent e) {

            }

            @Override
            public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent e) {

            }

            @Override
            public void onClientMoved(ClientMovedEvent e) {

            }

            @Override
            public void onChannelCreate(ChannelCreateEvent e) {

            }

            @Override
            public void onChannelDeleted(ChannelDeletedEvent e) {

            }

            @Override
            public void onChannelMoved(ChannelMovedEvent e) {

            }

            @Override
            public void onChannelPasswordChanged(ChannelPasswordChangedEvent e) {

            }

            @Override
            public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent e) {

            }
        });
    }
}
