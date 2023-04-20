package de.polo.void_roleplay.PlayerUtils;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class PayDayUtil {
    public static void givePayDay(Player player) throws SQLException {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        double plus = 0;
        double zinsen = Math.round(PlayerManager.bank(player) * 0.01);
        double steuern = Math.round(PlayerManager.bank(player) * 0.0035);
        int visumbonus = PlayerManager.visum(player) * 10;
        int frakpayday = 0;
        plus = plus + zinsen - steuern + visumbonus;
        player.sendMessage("");
        player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
        player.sendMessage(" ");
        player.sendMessage("§8 ➜ §3Kontoveränderung§8:§b PayDay");
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §aZinsen§8:§7 " + (int) zinsen + "$");
        player.sendMessage("§8 ➥ §cSteuern§8:§7 " + (int) steuern + "$");
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §2Sozialbonus§8:§7 " + visumbonus + "$");
        player.sendMessage(" ");
        if (playerData.getFaction() != "Zivilist" && playerData.getFaction() != null) {
            frakpayday = FactionManager.getPaydayFromFaction(playerData.getFaction(), playerData.getFactionGrade());
            if (FactionManager.removeFactionMoney(playerData.getFaction(), frakpayday, "Gehalt " + player.getName())) {
                player.sendMessage("§8 ➥ §" + FactionManager.getFactionPrimaryColor(playerData.getFaction()) + "Gehalt [" + playerData.getFaction() + "]§8: §7" + frakpayday + "$");
                player.sendMessage(" ");
                plus += frakpayday;
            }
        }
        plus = Math.round(plus);
        player.sendMessage("§8 ➥ §cGesamt§8:§7 " + (int) plus + "$");
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §eAlter Betrag§8:§7 " + PlayerManager.bank(player) + "$");
        PlayerManager.addBankMoney(player, (int) plus);
        player.sendMessage("§8 ➥ §6Neuer Betrag§8:§7 " + PlayerManager.bank(player) + "$");
        PlayerManager.addExp(player, Main.random(12, 20));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }
}
