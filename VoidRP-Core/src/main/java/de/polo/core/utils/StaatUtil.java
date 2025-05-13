package de.polo.core.utils;

import de.polo.api.VoidAPI;
import de.polo.api.player.PlayerWanted;
import de.polo.core.Main;
import de.polo.core.agreement.services.VertragUtil;
import de.polo.core.faction.service.LawEnforcementService;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.game.faction.laboratory.EvidenceChamber;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.ServerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.storage.JailInfo;
import de.polo.core.storage.ServiceData;
import de.polo.core.storage.WantedReason;
import de.polo.api.player.enums.WantedVariation;
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

import static de.polo.core.Main.database;

public class StaatUtil {
    public static final Map<String, ServiceData> serviceDataMap = new HashMap<>();
    public static EvidenceChamber Asservatemkammer;
    private final List<WantedReason> wantedReasons = new ObjectArrayList<>();
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;

    public StaatUtil(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public void loadParole(Player player) {
        database.executeQueryAsync("SELECT * FROM Jail_Parole WHERE uuid = ?", player.getUniqueId().toString())
                .thenAccept(result -> {
                    PlayerData playerData = playerManager.getPlayerData(player);
                    playerData.setJailParole((Integer) result.get(0).get("minutes_remaining"));
                });
        /*String sql = "SELECT * FROM Jail_Parole WHERE uuid = ?";

        try (PreparedStatement statement = Main.getInstance().coreDatabase.getConnection().prepareStatement(sql)) {
            statement.setString(1, player.getUniqueId().toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    PlayerData playerData = playerManager.getPlayerData(player);
                    playerData.setJailParole(result.getInt("minutes_remaining"));
                }
            }
        }*/
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
}
