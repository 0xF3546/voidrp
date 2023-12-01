package de.polo.metropiacity.utils;

import de.polo.metropiacity.dataStorage.HouseData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.playerUtils.ChatUtils;
import de.polo.metropiacity.utils.Game.Housing;
import de.polo.metropiacity.utils.Game.Streetwar;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class VertragUtil {
    public static final HashMap<String, String> vertrag_type = new HashMap<>();
    public static final HashMap<String, String> current = new HashMap<>();

    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final AdminManager adminManager;
    public VertragUtil(PlayerManager playerManager, FactionManager factionManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.adminManager = adminManager;
    }

    public static boolean setVertrag(Player player, Player target, String type, String vertrag)  {
        if (current.get(target.getUniqueId().toString()) == null) {
            current.remove(target.getUniqueId().toString(), vertrag);
            vertrag_type.put(target.getUniqueId().toString(), type);
        }
        vertrag_type.put(target.getUniqueId().toString(), type);
        current.put(target.getUniqueId().toString(), vertrag);
        Statement statement = null;
        try {
            statement = Main.getInstance().mySQL.getStatement();
            assert statement != null;
            statement.execute("INSERT INTO verträge (first_person, second_person, type, vertrag, date) VALUES ('" + player.getUniqueId() + "', '" + target.getUniqueId() + "', '" + type + "', '" + vertrag + "', '" + new Date() + "')");
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        /*if (current.get(target.getUniqueId().toString()) == null) {
            vertrag_type.put(target.getUniqueId().toString(), type);
            current.put(target.getUniqueId().toString(), vertrag);
            Statement statement = MySQL.getStatement();
            assert statement != null;
            statement.execute("INSERT INTO verträge (first_person, second_person, type, vertrag, date) VALUES ('" + player.getUniqueId() + "', '" + target.getUniqueId() + "', '" + type + "', '" + vertrag + "', '" + new Date() + "')");
            return true;
        } else {
            current.remove(target.getUniqueId().toString(), vertrag);
            vertrag_type.put(target.getUniqueId().toString(), type);
            return false;
        }*/
    }

    public static void deleteVertrag(Player player) {
        if (current.get(player.getUniqueId().toString()) != null) {
            current.remove(player.getUniqueId().toString());
            vertrag_type.remove(player.getUniqueId().toString());
        }
    }

    public void acceptVertrag(Player player) throws SQLException {
        String curr = current.get(player.getUniqueId().toString());
        if (curr != null) {
            Player targetplayer = null;
            try {
                targetplayer = Bukkit.getPlayer(UUID.fromString(curr));
            } catch (IllegalArgumentException e) {
            }
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            switch (vertrag_type.get(player.getUniqueId().toString())) {
                case "faction_invite":
                    factionManager.setPlayerInFrak(player, curr, 0);
                    factionManager.sendMessageToFaction(curr, player.getName() + " ist der Fraktion beigetreten");
                    adminManager.send_message(player.getName() + " ist der Fraktion " + curr + " beigetreten.", ChatColor.DARK_PURPLE);
                    break;
                case "business_invite":
                    Main.getInstance().businessManager.setPlayerInBusiness(player, curr, 0);
                    break;
                case "rental":
                    String[] args = curr.split("_");
                    int haus = Integer.parseInt(args[0]);
                    int preis = Integer.parseInt(args[1]);
                    HouseData houseData = Housing.houseDataMap.get(haus);
                    houseData.addRenter(player.getUniqueId().toString(), preis);
                    Main.getInstance().utils.housing.updateRenter(haus);
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
                        PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
                        HashMap<String, String> hmap1 = new HashMap<>();
                        hmap1.put(player.getUniqueId().toString(), "beziehung");
                        targetplayerData.setRelationShip(hmap1);

                        HashMap<String, String> hmap2 = new HashMap<>();
                        hmap2.put(targetplayer.getUniqueId().toString(), "beziehung");
                        playerData.setRelationShip(hmap2);
                        Statement statement = Main.getInstance().mySQL.getStatement();
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
                        PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
                        HashMap<String, String> hmap1 = new HashMap<>();
                        hmap1.put(player.getUniqueId().toString(), "verlobt");
                        targetplayerData.getRelationShip().clear();
                        targetplayerData.setRelationShip(hmap1);

                        HashMap<String, String> hmap2 = new HashMap<>();
                        hmap2.put(targetplayer.getUniqueId().toString(), "verlobt");
                        playerData.getRelationShip().clear();
                        playerData.setRelationShip(hmap2);
                        Statement statement = Main.getInstance().mySQL.getStatement();
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
                        Player finalTargetplayer = targetplayer;
                        Main.waitSeconds(7, () -> {
                            if (!finalTargetplayer.isOnline() || !player.isOnline()) {
                                return;
                            }
                            String[] blutgruppen = {"A-", "A+", "B-", "B+", "AB-", "AB+", "0+", "0-"};
                            String random = blutgruppen[new Random().nextInt(blutgruppen.length)];
                            finalTargetplayer.sendMessage("§8[§cLabor§8]§e Die Blutgruppe ist " + random + "!");
                            player.sendMessage("§eDeine Blutgruppe ist " + random + "!");
                            playerData.setBloodType(random);
                            try {
                                Statement statement = Main.getInstance().mySQL.getStatement();
                                statement.executeUpdate("UPDATE players SET bloodtype = '" + random + "' WHERE uuid = '" + player.getUniqueId() + "'");
                                playerManager.removeMoney(player, 200, "Untersuchung (Blutgruppe)");
                                factionManager.addFactionMoney("Medic", 200, "Untersuchung durch " + finalTargetplayer.getName() + " (Blutgruppe)");
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else {
                        player.sendMessage(Main.error + "Spieler konnte nicht gefunden werden.");
                    }
                    break;
                case "streetwar":
                    Main.getInstance().streetwar.acceptStreetwar(player, curr);
                    break;
            }
            deleteVertrag(player);
        } else {
            player.sendMessage(Main.error + "Dir wird nichts angeboten.");
        }
    }

    public void denyVertrag(Player player) {
        String curr = current.get(player.getUniqueId().toString());
        if (curr != null) {
            Player targetplayer = null;
            try {
                targetplayer = Bukkit.getPlayer(UUID.fromString(curr));
            } catch (IllegalArgumentException e) {
            }
            switch (vertrag_type.get(player.getUniqueId().toString())) {
                case "faction_invite":
                    factionManager.sendMessageToFaction(curr, player.getName() + " wurde eingeladen und ist nicht beigetreten.");
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
                case "streetwar":
                    Main.getInstance().streetwar.denyStreetwar(player, curr);
                    break;
            }
            deleteVertrag(player);
        } else {
            player.sendMessage(Main.error + "Dir wird nichts angeboten.");
        }
    }

    public void sendInfoMessage(Player player) {
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
