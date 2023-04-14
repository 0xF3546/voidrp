package de.polo.void_roleplay.PlayerUtils;

import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.entity.Player;

public class BankingUtils {
    public static void sendKontoauszug(Player player) {
        player.sendMessage(" ");
        player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §aBankguthaben§8:§7 " + PlayerManager.bank(player) + "$");
        player.sendMessage(" ");
    }

    public static void sendBankChangeReason(Player player, String reason) {
        player.sendMessage(" ");
        player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
        player.sendMessage(" ");
        player.sendMessage("§8 ➜ §3Kontoveränderung§8:§b " + reason);
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §aBankguthaben§8:§7 " + PlayerManager.bank(player) + "$");
        player.sendMessage(" ");
    }
}
