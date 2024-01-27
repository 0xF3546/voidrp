package de.polo.metropiacity.utils;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.JailData;
import de.polo.metropiacity.dataStorage.ServiceData;
import de.polo.metropiacity.dataStorage.PlayerData;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class StaatUtil {
    public static final Map<String, JailData> jailDataMap = new HashMap<>();
    public static final Map<String, ServiceData> serviceDataMap = new HashMap<>();

    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final LocationManager locationManager;
    private final Utils utils;

    public StaatUtil(PlayerManager playerManager, FactionManager factionManager, LocationManager locationManager, Utils utils) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        this.utils = utils;
        try {
            loadJail();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadJail() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `Jail`");
        while (result.next()) {
            JailData jailData = new JailData();
            jailData.setId(result.getInt(1));
            jailData.setUuid(result.getString(2));
            jailData.setHafteinheiten(result.getInt(3));
            jailData.setReason(result.getString(4));
            jailDataMap.put(result.getString(2), jailData);
        }
    }

    public boolean arrestPlayer(Player player, Player arrester) throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT `hafteinheiten`, `akte`, `geldstrafe` FROM `player_akten` WHERE `uuid` = '" + player.getUniqueId() + "'");
        int hafteinheiten = 0;
        int geldstrafe = 0;
        StringBuilder reason = new StringBuilder();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        PlayerData arresterData = playerManager.getPlayerData(arrester.getUniqueId());
        while (result.next()) {
            hafteinheiten += result.getInt(1);
            assert false;
            reason.append(result.getString(2)).append(", ");
            geldstrafe += result.getInt(3);
        }
        if (hafteinheiten > 0) {
            JailData jailData = new JailData();
            locationManager.useLocation(player, "gefaengnis");
            player.sendMessage("§8[§cGefängnis§8] §7Du wurdest für §6" + hafteinheiten + " Hafteinheiten§7 inhaftiert.");
            player.sendMessage("§8[§cGefängnis§8] §7Tatvorwürfe§8:§7 " + reason.substring(0, reason.length() - 2) + ".");
            playerData.setJailed(true);
            playerData.setHafteinheiten(hafteinheiten);
            factionManager.addFactionMoney(arresterData.getFaction(), Main.getInstance().serverManager.getPayout("arrest"), "Inhaftierung von " + player.getName() + ", durch " + arrester.getName());
            if (geldstrafe > 0) {
                if (playerData.getBank() >= geldstrafe) {
                    playerManager.removeBankMoney(player, geldstrafe, "Gefängnis Geldstrafe");
                    player.sendMessage("§8[§cGefängnis§8] §7Strafzahlung§8:§7 " + geldstrafe + "$.");
                } else if (playerData.getBank() > 0) {
                    playerManager.removeBankMoney(player, playerData.getBank(), "Gefängnis Geldstrafe");
                    player.sendMessage("§8[§cGefängnis§8] §7Strafzahlung§8:§7 " + geldstrafe + "$.");
                }
            }
            statement.execute("DELETE FROM `player_akten` WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
            for (Player players : Bukkit.getOnlinePlayers()) {
                PlayerData playerData1 = playerManager.getPlayerData(players.getUniqueId());
                if (Objects.equals(playerData1.getFaction(), "FBI") || Objects.equals(playerData1.getFaction(), "Polizei")) {
                    players.sendMessage("§8[§cGefängnis§8] §7" + factionManager.getTitle(arrester) + " " + arrester.getName() + " hat " + player.getName() + " in das Gefängnis inhaftiert.");
                }
            }
            statement.execute("INSERT INTO `Jail` (`uuid`, `hafteinheiten`, `reason`, `hafteinheiten_verbleibend`) VALUES ('" + player.getUniqueId().toString() + "', " + hafteinheiten + ", '" + reason + "', " + hafteinheiten + ")");
            jailData.setUuid(player.getUniqueId().toString());
            jailData.setHafteinheiten(hafteinheiten);
            jailData.setReason(String.valueOf(reason));
            jailDataMap.put(player.getUniqueId().toString(), jailData);
            return true;
        } else {
            return false;
        }
    }

    @SneakyThrows
    public void unarrestPlayer(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setJailed(false);
        playerData.setHafteinheiten(0);
        jailDataMap.remove(player.getUniqueId().toString());
        Statement statement = Main.getInstance().mySQL.getStatement();
        locationManager.useLocation(player, "gefaengnis_out");
        player.sendMessage("§8[§cGefängnis§8] §7Du wurdest entlassen.");
        statement.execute("DELETE FROM `Jail` WHERE `uuid` = '" + player.getUniqueId() + "'");
    }

    @SneakyThrows
    public void addAkteToPlayer(Player vergeber, Player player, int hafteinheiten, String akte, int geldstrafe) {
        Statement statement = Main.getInstance().mySQL.getStatement();
        statement.execute("INSERT INTO `player_akten` (`uuid`, `hafteinheiten`, `akte`, `geldstrafe`, `vergebendurch`) VALUES ('" + player.getUniqueId() + "', " + hafteinheiten + ", '" + akte + "', " + geldstrafe + ", '" + vergeber.getName() + "')");
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        player.sendMessage("§8[§6Anwalt§8]§7 Die Staatsanwaltschaft hat mich über eine neue Akte deinerseits Informiert.");
        player.sendMessage("§8[§6Anwalt§8]§7 Tatvorwurf: " + akte);
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playerData1 = playerManager.getPlayerData(players.getUniqueId());
            if (Objects.equals(playerData1.getFaction(), "FBI") || Objects.equals(playerData1.getFaction(), "Polizei")) {
                players.sendMessage("§8[§9Zentrale§8]§7 " + factionManager.getTitle(vergeber) + " " + vergeber.getName() + " hat " + player.getName() + " eine Akte hinzugefügt.");
            }
        }
    }

    public void removeAkteFromPlayer(Player player, int id) throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet akte = statement.executeQuery("SELECT * FROM player_akten WHERE id = " + id);
        if (akte.next()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(akte.getString(2)));
            if (offlinePlayer.isOnline()) {
                Player targetplayer = Bukkit.getPlayer(offlinePlayer.getUniqueId());
                PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
                if (targetplayerData.hasAnwalt()) {
                    targetplayer.sendMessage("§8[§6Anwalt§8]§7 Ein " + factionManager.getTitle(player) + " " + " hat dir eine Akte erlassen.");
                    targetplayer.sendMessage("§8[§6Anwalt§8]§7 Akte: " + akte.getString("akte"));
                }
            }
        }
        statement.execute("DELETE FROM `player_akten` WHERE `id` = " + id);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playerData1 = playerManager.getPlayerData(players.getUniqueId());
            if (Objects.equals(playerData1.getFaction(), "FBI") || Objects.equals(playerData1.getFaction(), "Polizei")) {
                players.sendMessage("§8[§9Zentrale§8]§7 " + factionManager.getTitle(player) + " " + player.getName() + " hat " + player.getName() + " eine Akte entfernt.");
            }
        }
    }

    public void createService(Player player, int service, String reason) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setIntVariable("service", service);
        ServiceData serviceData = new ServiceData();
        serviceData.setLocation(player.getLocation());
        serviceData.setNumber(service);
        serviceData.setReason(reason);
        serviceData.setUuid(player.getUniqueId().toString());
        serviceDataMap.put(player.getUniqueId().toString(), serviceData);
        player.sendMessage("§8[§6Notruf§8]§e Du hast einen Notruf abgesetzt.");
        if (service == 110) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData pData = playerManager.getPlayerData(p.getUniqueId());
                if (pData.getFaction() != null) {
                    if (pData.getFaction().equals("Polizei")) {
                        p.sendMessage("§8[§9Zentrale§8]§3 " + player.getName() + " hat ein Notruf abgesendet: " + reason);
                        TextComponent message = new TextComponent("§8 ➥ §bAnnehmen [" + (int) p.getLocation().distance(player.getLocation()) + "m]");
                        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§3§oNotruf annehmen")));
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptservice " + player.getName()));
                        p.spigot().sendMessage(message);
                    }
                }
            }
        }
        if (service == 112) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData pData = playerManager.getPlayerData(p.getUniqueId());
                if (pData.getFaction() != null) {
                    if (pData.getFaction().equals("Medic")) {
                        p.sendMessage("§8[§9Zentrale§8]§3 " + player.getName() + " hat ein Notruf abgesendet: " + reason);
                        TextComponent message = new TextComponent("§8 ➥ §bAnnehmen [" + (int) p.getLocation().distance(player.getLocation()) + "m]");
                        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§3§oNotruf annehmen")));
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptservice " + player.getName()));
                        p.spigot().sendMessage(message);
                    }
                }
            }
        }
    }

    public void cancelService(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        ServiceData serviceData = serviceDataMap.get(player.getUniqueId().toString());
        playerData.setVariable("service", null);
        player.sendMessage("§8[§6Notruf§8]§e Du hast deinen Notruf abgebrochen.");
        if (serviceData.getAcceptedByUuid() != null) {
            Player accepter = Bukkit.getPlayer(UUID.fromString(serviceData.getAcceptedByUuid()));
            assert accepter != null;
            accepter.sendMessage("§8[§6Notruf§8]§e " + player.getName() + " hat seinen Notruf abgebrochen.");
        }
        StaatUtil.serviceDataMap.remove(player.getUniqueId().toString());
    }

    public void checkBloodGroup(Player player, Player targetplayer) {
        PlayerData targetPlayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        if (targetPlayerData.getBloodType() != null) {
            player.sendMessage("§e" + targetplayer.getName() + "'s Blutgruppe ist " + targetPlayerData.getBloodType() + "!");
            return;
        }
        if (targetPlayerData.getBargeld() < 200) {
            player.sendMessage(Main.error + targetplayer.getName() + " hat nicht genug Geld dabei! (200$)");
            return;
        }
        if (VertragUtil.setVertrag(player, targetplayer, "blutgruppe", player.getUniqueId().toString())) {
            player.sendMessage("§eDu hast " + targetplayer.getName() + " eine Anfrage zur Prüfung seiner Blutgruppe gestellt.");
            targetplayer.sendMessage("§eMediziner " + player.getName() + " möchte deine Blutgruppe testen.");
            utils.vertragUtil.sendInfoMessage(targetplayer);
        } else {
            player.sendMessage(Main.error + targetplayer.getName() + " hat einen Vertrag offen.");
        }
    }
}
