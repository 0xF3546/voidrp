package de.polo.metropiacity.utils.GamePlay;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.Apotheke;
import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.*;
import de.polo.metropiacity.utils.events.MinuteTickEvent;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ApothekeFunctions implements Listener {
    private final List<Apotheke> apotheken = new ArrayList<>();
    private final MySQL mySQL;
    private final Utils utils;
    private final FactionManager factionManager;
    private final PlayerManager playerManager;

    @SneakyThrows
    public ApothekeFunctions(MySQL mySQL, Utils utils, FactionManager factionManager, PlayerManager playerManager) {
        this.mySQL = mySQL;
        this.utils = utils;
        this.factionManager = factionManager;
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        Statement statement = mySQL.getStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM apotheken");
        while (resultSet.next()) {
            Apotheke apotheke = new Apotheke();
            apotheke.setId(resultSet.getInt("id"));
            apotheke.setStaat(resultSet.getBoolean("isStaat"));
            apotheke.setOwner(resultSet.getString("owner"));
            apotheke.setLastAttack(resultSet.getTimestamp("lastAttack").toLocalDateTime());
            apotheken.add(apotheke);
        }
    }

    public Collection<Apotheke> getApotheken() {
        return apotheken;
    }

    @EventHandler
    public void MinuteTick(MinuteTickEvent event) {
        if (LocalDateTime.now().getHour() < 16 && LocalDateTime.now().getHour() > 22) return;
        for (FactionData factionData : factionManager.getFactions()) {
            int plus = 0;
            for (Apotheke apotheke : getApotheken()) {
                if (apotheke.getOwner().equalsIgnoreCase(factionData.getName())) {
                    if (apotheke.getLastAttack().getMinute() == event.getMinute()) {
                        if (apotheke.isStaat()) plus += ServerManager.getPayout("apotheke_besetzt_staat");
                        else plus += ServerManager.getPayout("apotheke_besetzt_normal");
                    }
                }
            }
            factionData.setJointsMade(plus);
            if (plus >= 1) {
                for (PlayerData playerData : playerManager.getPlayers()) {
                    if (playerData.getFaction().equalsIgnoreCase(factionData.getName())) {
                        Player player = Bukkit.getPlayer(playerData.getUuid());
                        player.sendMessage("§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§a Deine Fraktion hat §2" + plus + " Joints§a aus den aktuell übernommenen Apotheken erhalten.");
                        factionData.storage.setJoint(factionData.storage.getJoint() + plus);
                        factionData.storage.save();
                    }
                }
            }
        }
    }
}
