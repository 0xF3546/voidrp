package de.polo.void_roleplay.Utils;


import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;

public class TeamSpeak {
    static TS3Api api = null;
    public static void loadConfig() {
        System.out.println("Lade TS3 config...");
        final TS3Config config = new TS3Config();
        config.setHost("91.212.121.55");
        final TS3Query query = new TS3Query();
        query.connect();
        System.out.println("Query connection: " + query.isConnected());

        api = query.getApi();
        api.login("VoidRoleplayJava", "JrL4OHQf");
        api.selectVirtualServerById(1);
        api.setNickname("Void Roleplay");
        api.sendChannelMessage("Bot gestartet");
    }

    public static TS3Api getAPI() {
        return api;
    }
}
