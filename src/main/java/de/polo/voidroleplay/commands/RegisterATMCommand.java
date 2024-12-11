package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.database.impl.MySQL;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class RegisterATMCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final MySQL mySQL;

    public RegisterATMCommand(PlayerManager playerManager, AdminManager adminManager, MySQL mySQL) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.mySQL = mySQL;
        Main.registerCommand("registeratm", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 90) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Main.error + "Syntax-Fehler: /registeratm [BlockId(/registerblock atm)] [ATM-Name]");
            return false;
        }
        int blockId = Integer.parseInt(args[0]);
        String atmName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Main.getInstance().getMySQL().insertAndGetKeyAsync("INSERT INTO atm (blockId, name) VALUES (?, ?)", blockId, atmName)
                .thenApply(key -> {
                    if (key.isPresent()) {
                        player.sendMessage(Prefix.gamedesign_prefix + "Du hast einen ATM registriert #" + key.get());
                        adminManager.send_message(player.getName() + " hat einen ATM registriert (ATM #" + key.get() + ").", ChatColor.GOLD);
                    }
                    return null;
                });
        return false;
    }
}
