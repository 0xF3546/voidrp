package de.polo.voidroleplay.admin.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ServerReloadCommand implements CommandExecutor {
    public ServerReloadCommand() {
        Main.registerCommand("serverreload", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (!player.hasPermission("op")) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server Reloaded in 15 Sekunden!");
        Utils.waitSeconds(5, () -> {
            Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server Reloaded in 10 Sekunden!");
            Utils.waitSeconds(5, () -> {
                Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server Reloaded in 5 Sekunden!");
                Utils.waitSeconds(2, () -> {
                    Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server Reloaded in 3 Sekunden!");
                    Utils.waitSeconds(1, () -> {
                        Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server Reloaded in 2 Sekunden!");
                        Utils.waitSeconds(1, () -> {
                            Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server Reloaded in 1 Sekunde!");
                            Utils.waitSeconds(1, () -> {
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    p.kickPlayer("Server reloaded jetzt!");
                                }
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
