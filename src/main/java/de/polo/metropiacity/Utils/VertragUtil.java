package de.polo.metropiacity.Utils;

import de.polo.metropiacity.DataStorage.HouseData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

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
                    break;
                case "rental":
                    String[] args = curr.split("_");
                    Integer haus = Integer.valueOf(args[0]);
                    Integer preis = Integer.valueOf(args[1]);
                    HouseData houseData = Housing.houseDataMap.get(haus);
                    houseData.addRenter(player.getUniqueId().toString(), preis);
                    Housing.updateRenter(haus);
                    Player player1 = Bukkit.getPlayer(UUID.fromString(houseData.getOwner()));
                    player1.sendMessage("§8[§6Haus§8]§a " + player.getName() + " Mietet nun in Haus " + houseData.getNumber() + " für " + preis + "$.");
                    player.sendMessage("§8[§6Haus§8]§a Du mietest nun in Haus " + houseData.getNumber() + " für " + preis + "$/PayDay.");
                    break;
                case "phonecall":
                    PhoneUtils.acceptCall(player, curr);
                    break;
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
                    break;
                case "phonecall":
                    PhoneUtils.denyCall(player, curr);
                    break;
                case "rental":
                    String[] args = curr.split("_");
                    Integer haus = Integer.valueOf(args[0]);
                    Integer preis = Integer.valueOf(args[1]);
                    HouseData houseData = Housing.houseDataMap.get(haus);
                    Player player1 = Bukkit.getPlayer(UUID.fromString(houseData.getOwner()));
                    player1.sendMessage("§8[§6Haus§8]§a " + player.getName() + " hat den Mietvertrag für Haus " + houseData.getNumber() + " abgelehnt.");
                    player.sendMessage("§8[§6Haus§8]§c Du hast den Mietvertrag abgelehnt.");
                    break;
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
