package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.agreement.services.VertragUtil;
import de.polo.voidroleplay.game.faction.laboratory.EvidenceChamber;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.location.services.impl.LocationManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.storage.*;
import de.polo.voidroleplay.utils.enums.WantedVariation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class StaatUtil {
    public static final Map<String, JailData> jailDataMap = new HashMap<>();
    public static final Map<String, ServiceData> serviceDataMap = new HashMap<>();
    public static EvidenceChamber Asservatemkammer;
    private final List<WantedReason> wantedReasons = new ObjectArrayList<>();
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
            Connection connection = Main.getInstance().coreDatabase.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM evidenceChamber");
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                Asservatemkammer = new EvidenceChamber(result.getInt("weed"), result.getInt("joints"), result.getInt("cocaine"), result.getInt("noble_joints"), result.getInt("crystal"));
                Asservatemkammer.setId(result.getInt("id"));
            }
            result.close();
            statement.close();
            connection.close();
            loadJail();
            loadWantedReasons();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private void loadWantedReasons() {
        /*
        Connection connection = Main.getInstance().getMySQL().getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM wantedreasons");
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            WantedReason reason = new WantedReason(resultSet.getInt("id"), resultSet.getString("reason"), resultSet.getInt("wanted"));
            wantedReasons.add(reason);
        }
        statement.close();
        connection.close();
        */
        Main.getInstance().getCoreDatabase().queryThreaded("SELECT * FROM wantedreasons")
                .thenAccept(result -> {
                    try {
                        while (result.next()) {
                            WantedReason reason = new WantedReason(result.resultSet().getInt("id"), result.resultSet().getString("reason"), result.resultSet().getInt("wanted"));
                            wantedReasons.add(reason);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        result.close();
                    }
                });
    }

    private void loadJail() throws SQLException {
        /*
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `Jail`");
        while (result.next()) {
            JailData jailData = new JailData();
            jailData.setId(result.getInt("id"));
            jailData.setUuid(result.getString("uuid"));
            jailData.setHafteinheiten(result.getInt("wps"));
            jailData.setReason(result.getString("wantedId"));
            jailDataMap.put(result.getString("uuid"), jailData);
        }
        */
        Main.getInstance()
                .getCoreDatabase()
                .queryThreaded("SELECT * FROM `Jail`")
                .thenAcceptAsync(result -> {
                    try {
                        while (result.next()) {
                            JailData jailData = new JailData();
                            jailData.setId(result.resultSet().getInt("id"));
                            jailData.setUuid(result.resultSet().getString("uuid"));
                            jailData.setHafteinheiten(result.resultSet().getInt("wps"));
                            jailData.setReason(result.resultSet().getString("wantedId"));
                            jailDataMap.put(result.resultSet().getString("uuid"), jailData);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        result.close();
                    }
                });
    }

    private String calculateManhuntTime(PlayerWanted wanted) {
        LocalDateTime now = Utils.getTime();
        Duration diff = Duration.between(wanted.getIssued(), now);

        if (diff.isNegative()) {
            diff = diff.abs();
        }

        long minutes = diff.toMinutes();
        if (minutes < 60) {
            return minutes + " Minute" + (minutes != 1 ? "n" : "");
        }

        long hours = diff.toHours();
        if (hours < 24) {
            return hours + " Stunde" + (hours != 1 ? "n" : "");
        }

        long days = diff.toDays();
        return days + " Tag" + (days != 1 ? "e" : "");
    }


    public boolean arrestPlayer(Player player, Player arrester, boolean deathArrest) throws SQLException {
        StringBuilder reason = new StringBuilder();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        PlayerData arresterData = playerManager.getPlayerData(arrester.getUniqueId());
        WantedReason wantedReason = getWantedReason(playerData.getWanted().getWantedId());
        if (playerData.getWanted() != null) {
            JailData jailData = new JailData();
            int wanteds = wantedReason.getWanted();
            for (WantedVariation variation : playerData.getWanted().getVariations()) {
                wanteds += variation.getWantedAmount();
            }
            if (!deathArrest) locationManager.useLocation(player, "gefaengnis");
            if (deathArrest) {
                player.sendMessage("§8[§6Gefängnis§8] §7Du wurdest für " + wanteds / 3 + " Minuten inhaftiert.");
            } else {
                player.sendMessage("§8[§6Gefängnis§8] §7Du bist nun für " + wanteds / 3 + "  Minuten im Gefängnis..");
            }
            playerData.setJailed(true);
            factionManager.addFactionMoney(arresterData.getFaction(), ServerManager.getPayout("arrest"), "Inhaftierung von " + player.getName() + ", durch " + arrester.getName());
            playerData.setHafteinheiten(wanteds / 3);
            Main.getInstance().getCoreDatabase().queryThreaded("DELETE FROM player_wanteds WHERE uuid = ?", player.getUniqueId().toString());
            for (Player players : Bukkit.getOnlinePlayers()) {
                PlayerData playerData1 = playerManager.getPlayerData(players.getUniqueId());
                if (playerData1 == null) continue;
                if (playerData.getFaction() == null) continue;
                if (playerData1.isExecutiveFaction()) {
                    if (deathArrest) {
                        players.sendMessage("§9HQ: " + player.getName() + " wurde von " + arrester.getName() + " getötet.");
                    } else {
                        players.sendMessage("§9HQ: " + factionManager.getTitle(arrester) + " " + arrester.getName() + " hat " + player.getName() + " in das Gefängnis inhaftiert.");
                    }
                    players.sendMessage("§9HQ: Fahndungsgrund: " + wantedReason.getReason() + " | Fahndungszeit: " + calculateManhuntTime(playerData.getWanted()));
                }
            }
            jailData.setHafteinheiten(playerData.getHafteinheiten());
            Main.getInstance().getCoreDatabase().insertAsync("INSERT INTO `Jail` (`uuid`, `wantedId`, `wps`, `arrester`) VALUES (?, ?, ?, ?)", player.getUniqueId().toString(), wantedReason.getId(), jailData.getHafteinheiten(), arrester.getUniqueId().toString());
            jailData.setUuid(player.getUniqueId().toString());
            jailData.setReason(String.valueOf(reason));
            jailDataMap.put(player.getUniqueId().toString(), jailData);
            playerData.clearWanted();
            return true;
        } else {
            return false;
        }
    }

    @SneakyThrows
    public void clearPlayerAkte(Player pLayer) {
        Main.getInstance().getCoreDatabase().deleteAsync("DELETE FROM player_akten WHERE uuid = ?", pLayer.getUniqueId().toString());
    }

    @SneakyThrows
    public void unarrestPlayer(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (locationManager.getDistanceBetweenCoords(player, "gefaengnis") < 200) {
            locationManager.useLocation(player, "gefaengnis_out");
        }
        playerData.setJailed(false);
        playerData.setHafteinheiten(0);
        jailDataMap.remove(player.getUniqueId().toString());
        Statement statement = Main.getInstance().coreDatabase.getStatement();
        player.sendMessage("§8[§cGefängnis§8] §7Du wurdest entlassen.");
        Main.getInstance().getCoreDatabase().deleteAsync("DELETE FROM Jail WHERE uuid = ?", player.getUniqueId().toString());
        loadParole(player);
    }

    @SneakyThrows
    public void loadParole(Player player) {
        PreparedStatement statement = Main.getInstance().coreDatabase.getConnection().prepareStatement("SELECT * FROM Jail_Parole WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            PlayerData playerData = playerManager.getPlayerData(player);
            playerData.setJailParole(result.getInt("minutes_remaining"));
        }
        statement.close();
    }

    @SneakyThrows
    public void setParole(Player player, int hafteinheiten) {
        PreparedStatement statement = Main.getInstance().coreDatabase.getConnection().prepareStatement("INSERT INTO Jail_Parole (uuid, hafteinheiten, minutes_remaining) VALUES (?, ?, ?)");
        statement.setString(1, player.getUniqueId().toString());
        statement.setInt(2, hafteinheiten);
        statement.setInt(3, hafteinheiten);
        statement.execute();
        statement.close();
    }

    @SneakyThrows
    public boolean hasParole(Player player) {
        PreparedStatement statement = Main.getInstance().coreDatabase.getConnection().prepareStatement("SELECT * FROM Jail_Parole WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        ResultSet result = statement.executeQuery();
        boolean hasParole = result.next();
        statement.close();
        return hasParole;
    }

    @SneakyThrows
    public void addAkteToPlayer(Player vergeber, Player player, int hafteinheiten, String akte, int geldstrafe) {
        Statement statement = Main.getInstance().coreDatabase.getStatement();
        statement.execute("INSERT INTO `player_akten` (`uuid`, `hafteinheiten`, `akte`, `geldstrafe`, `vergebendurch`) VALUES ('" + player.getUniqueId() + "', " + hafteinheiten + ", '" + akte + "', " + geldstrafe + ", '" + vergeber.getName() + "')");
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.hasAnwalt()) {
            player.sendMessage("§8[§6Anwalt§8]§7 Die Staatsanwaltschaft hat mich über eine neue Akte deinerseits Informiert.");
            player.sendMessage("§8[§6Anwalt§8]§7 Tatvorwurf: " + akte);
        }
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playerData1 = playerManager.getPlayerData(players.getUniqueId());
            if (Objects.equals(playerData1.getFaction(), "FBI") || Objects.equals(playerData1.getFaction(), "Polizei")) {
                players.sendMessage("§8[§9Zentrale§8]§7 " + factionManager.getTitle(vergeber) + " " + vergeber.getName() + " hat " + player.getName() + " eine Akte hinzugefügt.");
            }
        }
    }

    public void removeAkteFromPlayer(Player player, int id) throws SQLException {
        Statement statement = Main.getInstance().coreDatabase.getStatement();
        ResultSet akte = statement.executeQuery("SELECT * FROM player_akten WHERE id = " + id);
        OfflinePlayer target = null;
        if (akte.next()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(akte.getString(2)));
            target = offlinePlayer;
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
                players.sendMessage("§8[§9Zentrale§8]§7 " + factionManager.getTitle(player) + " " + player.getName() + " hat " + target.getName() + " eine Akte entfernt.");
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
            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " hat nicht genug Geld dabei! (200$)");
            return;
        }
        if (VertragUtil.setVertrag(player, targetplayer, "blutgruppe", player.getUniqueId().toString())) {
            player.sendMessage("§eDu hast " + targetplayer.getName() + " eine Anfrage zur Prüfung seiner Blutgruppe gestellt.");
            targetplayer.sendMessage("§eMediziner " + player.getName() + " möchte deine Blutgruppe testen.");
            utils.vertragUtil.sendInfoMessage(targetplayer);
        } else {
            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " hat einen Vertrag offen.");
        }
    }

    public WantedReason getWantedReason(int id) {
        return wantedReasons.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
    }

    public Collection<WantedReason> getWantedReasons() {
        return wantedReasons;
    }

    public WantedReason getWantedReason(String reason) {
        return wantedReasons.stream().filter(x -> x.getReason().equalsIgnoreCase(reason)).findFirst().orElse(null);
    }

    public void addWantedReason(WantedReason wantedReason) {
        wantedReasons.add(wantedReason);
    }
}
