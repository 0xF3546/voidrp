package de.polo.voidroleplay.game.faction.staat;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.utils.FactionManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class StaatsbankRob {
    @Getter
    @Setter
    private FactionData attacker;

    @Getter
    @Setter
    private int minutes = 0;

    @Getter
    @Setter
    private int vaults;

    @Getter
    @Setter
    private int vaultsOpen;

    @Getter
    private List<Integer> openVaults;

    public StaatsbankRob() {
        vaults = Main.random(12, 20);
    }

    public void sendMessage(String message, ChatColor color, String... factions) {
        Main.getInstance().factionManager.sendCustomMessageToFactions("§8[§3Staatsbank§8] " + color + message, factions);
    }

    public boolean openVault(int vault) {
        if (openVaults.contains(vault)) return false;
        openVaults.add(vault);
        return true;
    }
    public boolean isVaultOpen(int vault) {
        return openVaults.contains(vault);
    }
}
