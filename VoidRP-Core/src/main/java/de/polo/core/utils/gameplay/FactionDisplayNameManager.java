package de.polo.core.utils.gameplay;

import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.player.entities.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FactionDisplayNameManager {

    public void updateForFaction(String faction) {
        Faction factionData = Main.factionManager.getFactionData(faction);
        if (factionData == null) return;

        // Legen Sie einen Rang für diese Fraktion fest. Der Prefix wird aus der Fraktionsfarbe abgeleitet.
        // "10" ist hier ein Beispiel für die Priority. Sie können einen anderen Wert wählen,
        // falls es verschiedene Ränge oder Prioritäten gibt.
        Main.customTabAPI.setRank(faction.toLowerCase(), "§" + factionData.getPrimaryColor(), 10);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = Main.playerManager.getPlayerData(player);
            if (playerData == null) continue;
            if (playerData.getFaction() == null) {
                // optional: remove the rank for the player
                continue;
            }

            if (playerData.getFaction().equalsIgnoreCase(faction)) {
                Main.customTabAPI.setPlayerRank(player, faction);
            } else {
                // other logic like removing the rank
            }
        }

        // Aktualisieren Sie die Tab-List für alle Spieler.
        Main.customTabAPI.updateTabForAllPlayers();
    }

}
