package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.chat.hover.content.TextSerializer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class VertragUtil {
    public static HashMap<String, String> vertrag_type = new HashMap<>();
    public static HashMap<String, String> current = new HashMap<>();

    public static boolean setVertrag(Player player, Player target, String type, String vertrag) throws SQLException {
        if (current.get(target.getUniqueId().toString()) == null) {
            vertrag_type.put(target.getUniqueId().toString(), type);
            current.put(target.getUniqueId().toString(), vertrag);
            Statement statement = MySQL.getStatement();
            assert statement != null;
            statement.execute("INSERT INTO verträge (first_person, second_person, type, vertrag, date) VALUES ('" + player.getUniqueId().toString() + "', '" + target.getUniqueId().toString() + "', '" + type + "', '" + vertrag + "', '" + new Date() + "')");
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteVertrag(Player player) {
        if (current.get(player.getUniqueId().toString()) != null) {
            current.remove(player.getUniqueId().toString());
            vertrag_type.remove(player.getUniqueId().toString());
            return true;
        } else {
            return false;
        }
    }
    public static void acceptVertrag(Player player) throws SQLException {
        String curr = current.get(player.getUniqueId().toString());
        if (curr != null) {
            switch (vertrag_type.get(player.getUniqueId().toString())) {
                case "faction_invite":
                    FactionManager.setPlayerInFrak(player, curr, 0);
                    FactionManager.sendMessageToFaction(curr,player.getName() + " ist der Fraktion §abeigetreten§7.");
                case "rental":
                    String[] args = curr.split("_");
                    Integer haus = Integer.valueOf(args[0]);
                    Integer preis = Integer.valueOf(args[1]);
                case "phonecall":
                    PhoneUtils.acceptCall(player, curr);
            }
            deleteVertrag(player);
        } else {
            player.sendMessage(Main.error + "Dir wird nichts angeboten.");
        }
    }
    public static void denyVertrag(Player player) throws SQLException {
        String curr = current.get(player.getUniqueId().toString());
        if (curr != null) {
            switch (vertrag_type.get(player.getUniqueId().toString())) {
                case "faction_invite":
                    FactionManager.sendMessageToFaction(curr, player.getName() + "wurde eingeladen und ist §cnicht§7 beigetreten.");
                case "phonecall":
                    PhoneUtils.denyCall(player, curr);
            }
            deleteVertrag(player);
        } else {
            player.sendMessage(Main.error + "Dir wird nichts angeboten.");
        }
    }

    public static void sendInfoMessage(Player player) {
        TextComponent annehmen = new TextComponent("§8/§aannehmen");
        annehmen.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/annehmen"));
        annehmen.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oAnnehmen")));

        TextComponent ablehnen = new TextComponent("§8/§cablehnen");
        ablehnen.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ablehnen"));
        ablehnen.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§c§oAblehnen")));

        TextComponent message = new TextComponent("§8 ➥§7 Nutze ");
        message.addExtra(annehmen);
        message.addExtra(new TextComponent("§7 oder "));
        message.addExtra(ablehnen);
        message.addExtra(new TextComponent("§7."));
        player.spigot().sendMessage(message);
    }
}
