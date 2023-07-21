package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ServerReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (!player.hasPermission("op")) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server Reloaded in 15 Sekunden!");
        Main.waitSeconds(5,() -> {
            Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server Reloaded in 10 Sekunden!");
            Main.waitSeconds(5,() -> {
                Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server Reloaded in 5 Sekunden!");
                Main.waitSeconds(2,() -> {
                    Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server Reloaded in 3 Sekunden!");
                    Main.waitSeconds(1,() -> {
                        Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server Reloaded in 2 Sekunden!");
                        Main.waitSeconds(1,() -> {
                            Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server Reloaded in 1 Sekunde!");
                            Main.waitSeconds(1,() -> {
                                Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server Reloaded jetzt!");
                                Bukkit.reload();
                            });
                        });
                    });
                });
            });
        });
        return false;
    }
}
