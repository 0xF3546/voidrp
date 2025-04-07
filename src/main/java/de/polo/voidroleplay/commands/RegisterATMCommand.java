package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.admin.services.impl.AdminManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static de.polo.voidroleplay.Main.database;

public class RegisterATMCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;

    public RegisterATMCommand(PlayerManager playerManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        Main.registerCommand("registeratm", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 90) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /registeratm [BlockId(/registerblock atm)] [ATM-Name]");
            return false;
        }
        int blockId = Integer.parseInt(args[0]);
        String atmName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        database.insertAndGetKeyAsync("INSERT INTO atm (blockId, name) VALUES (?, ?)", blockId, atmName)
                .thenApply(key -> {
                    if (key.isPresent()) {
                        player.sendMessage(Prefix.GAMEDESIGN + "Du hast einen ATM registriert #" + key.get());
                        adminManager.send_message(player.getName() + " hat einen ATM registriert (ATM #" + key.get() + ").", ChatColor.GOLD);
                    }
                    return null;
                });
        return false;
    }
}
