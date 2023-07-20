package de.polo.metropiacity.utils;

import de.polo.metropiacity.dataStorage.HouseData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.playerUtils.ChatUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class VertragUtil {
    public static final HashMap<String, String> vertrag_type = new HashMap<>();
    public static final HashMap<String, String> current = new HashMap<>();

    public static boolean setVertrag(Player player, Player target, String type, String vertrag) throws SQLException {
        if (current.get(target.getUniqueId().toString()) == null) {
            vertrag_type.put(target.getUniqueId().toString(), type);
            current.put(target.getUniqueId().toString(), vertrag);
            Statement statement = MySQL.getStatement();
            assert statement != null;
            statement.execute("INSERT INTO verträge (first_person, second_person, type, vertrag, date) VALUES ('" + player.getUniqueId() + "', '" + target.getUniqueId() + "', '" + type + "', '" + vertrag + "', '" + new Date() + "')");
            return true;
        } else {
            return false;
        }
    }

    public static void deleteVertrag(Player player) {
        if (current.get(player.getUniqueId().toString()) != null) {
            current.remove(player.getUniqueId().toString());
            vertrag_type.remove(player.getUniqueId().toString());
        } else {
        }
    }
    public static void acceptVertrag(Player player) throws SQLException {
        String curr = current.get(player.getUniqueId().toString());
        if (curr != null) {
            Player targetplayer = Bukkit.getPlayer(UUID.fromString(curr));
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            switch (vertrag_type.get(player.getUniqueId().toString())) {
                case "faction_invite":
                    FactionManager.setPlayerInFrak(player, curr, 0);
                    FactionManager.sendMessageToFaction(curr,player.getName() + " ist der Fraktion §abeigetreten§7.");
                    break;
                case "business_invite":
                    BusinessManager.setPlayerInBusiness(player, curr, 0);
                    break;
                case "rental":
                    String[] args = curr.split("_");
                    int haus = Integer.parseInt(args[0]);
                    int preis = Integer.parseInt(args[1]);
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
                case "beziehung":
                    if (targetplayer.isOnline()) {
                        player.sendMessage("§aDu und " + targetplayer.getName() + " sind jetzt zusammen.");
                        targetplayer.sendMessage("§aDu und " + player.getName() + " sind jetzt zusammen.");
                        PlayerData targetplayerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
                        HashMap<String, String> hmap1 = new HashMap<>();
                        hmap1.put(player.getUniqueId().toString(), "beziehung");
                        targetplayerData.setRelationShip(hmap1);

                        HashMap<String, String> hmap2 = new HashMap<>();
                        hmap2.put(targetplayer.getUniqueId().toString(), "beziehung");
                        playerData.setRelationShip(hmap2);
                        Statement statement = MySQL.getStatement();
                        JSONObject object = new JSONObject(playerData.getRelationShip());
                        statement.executeUpdate("UPDATE `players` SET `relationShip` = '" + object + "' WHERE `uuid` = '" + player.getUniqueId() + "'");

                        JSONObject object2 = new JSONObject(targetplayerData.getRelationShip());
                        statement.executeUpdate("UPDATE `players` SET `relationShip` = '" + object2 + "' WHERE `uuid` = '" + targetplayer.getUniqueId() + "'");
                    } else {
                        player.sendMessage(Main.error + "Spieler konnte nicht gefunden werden.");
                    }
                    break;
                case "verlobt":
                    if (targetplayer.isOnline()) {
                        player.sendMessage("§aDu und " + targetplayer.getName() + " sind jetzt verlobt.");
                        targetplayer.sendMessage("§aDu und " + player.getName() + " sind jetzt verlobt.");
                        PlayerData targetplayerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
                        HashMap<String, String> hmap1 = new HashMap<>();
                        hmap1.put(player.getUniqueId().toString(), "verlobt");
                        targetplayerData.getRelationShip().clear();
                        targetplayerData.setRelationShip(hmap1);

                        HashMap<String, String> hmap2 = new HashMap<>();
                        hmap2.put(targetplayer.getUniqueId().toString(), "verlobt");
                        playerData.getRelationShip().clear();
                        playerData.setRelationShip(hmap2);
                        Statement statement = MySQL.getStatement();
                        JSONObject object = new JSONObject(playerData.getRelationShip());
                        statement.executeUpdate("UPDATE `players` SET `relationShip` = '" + object + "' WHERE `uuid` = '" + player.getUniqueId() + "'");

                        JSONObject object2 = new JSONObject(targetplayerData.getRelationShip());
                        statement.executeUpdate("UPDATE `players` SET `relationShip` = '" + object2 + "' WHERE `uuid` = '" + targetplayer.getUniqueId() + "'");
                    } else {
                        player.sendMessage(Main.error + "Spieler konnte nicht gefunden werden.");
                    }
                    break;
                case "blutgruppe":
                    if (targetplayer.isOnline()) {
                        ChatUtils.sendGrayMessageAtPlayer(targetplayer, targetplayer.getName() + " testet eine Blutgruppe im Labor.");
                        targetplayer.sendMessage("§8[§cLabor§8]§e Prüfe Ergebnisse...");
                        Main.waitSeconds(7, () -> {
                            if (!targetplayer.isOnline() || !player.isOnline()) {
                                return;
                            }
                            String[] blutgruppen = {"A-", "A+", "B-", "B+", "AB-", "AB+", "0+", "0-"};
                            String random = blutgruppen[new Random().nextInt(blutgruppen.length)];
                            targetplayer.sendMessage("§8[§cLabor§8]§e Die Blutgruppe ist " + random + "!");
                            player.sendMessage("§eDeine Blutgruppe ist " + random + "!");
                            playerData.setBloodType(random);
                            try {
                                Statement statement = MySQL.getStatement();
                                statement.executeUpdate("UPDATE players SET bloodtype = '" + random + "' WHERE uuid = '" + player.getUniqueId() + "'");
                                PlayerManager.removeMoney(player, 200, "Untersuchung (Blutgruppe)");
                                FactionManager.addFactionMoney("Medic", 200, "Untersuchung durch " + targetplayer.getName() + " (Blutgruppe)");
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else {
                        player.sendMessage(Main.error + "Spieler konnte nicht gefunden werden.");
                    }
                    break;
            }
            deleteVertrag(player);
        } else {
            player.sendMessage(Main.error + "Dir wird nichts angeboten.");
        }
    }
    public static void denyVertrag(Player player) {
        String curr = current.get(player.getUniqueId().toString());
        if (curr != null) {
            Player targetplayer = Bukkit.getPlayer(UUID.fromString(curr));
            switch (vertrag_type.get(player.getUniqueId().toString())) {
                case "faction_invite":
                    FactionManager.sendMessageToFaction(curr, player.getName() + " wurde eingeladen und ist nicht beigetreten.");
                    break;
                case "business_invite":
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
                case "beziehung":
                case "verlobt":
                    if (targetplayer.isOnline()) {
                        player.sendMessage("§cDu hast die Anfrage abgelehnt.");
                        targetplayer.sendMessage("§c" + player.getName() + " hat die Anfrage abgelehnt.");
                    } else {
                        player.sendMessage(Main.error + "Spieler konnte nicht gefunden werden.");
                    }
                    break;
                case "blutgruppe":
                    if (targetplayer.isOnline()) {
                        player.sendMessage("§cDu hast die Anfrage abgelehnt.");
                        targetplayer.sendMessage("§e" + player.getName() + " hat die Anfrage abgelehnt.");
                    } else {
                        player.sendMessage(Main.error + "Spieler konnte nicht gefunden werden.");
                    }
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
