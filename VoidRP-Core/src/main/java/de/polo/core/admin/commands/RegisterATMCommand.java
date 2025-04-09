package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static de.polo.core.Main.database;

public class RegisterATMCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public RegisterATMCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
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
                        AdminService adminService = VoidAPI.getService(AdminService.class);
                        player.sendMessage(Prefix.GAMEDESIGN + "Du hast einen ATM registriert #" + key.get());
                        adminService.send_message(player.getName() + " hat einen ATM registriert (ATM #" + key.get() + ").", Color.ORANGE);
                    }
                    return null;
                });
        return false;
    }
}
