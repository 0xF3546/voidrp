package de.polo.voidroleplay.admin.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ServerStopCommand implements CommandExecutor {
    public ServerStopCommand() {
        Main.registerCommand("serverstop", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (!player.hasPermission("op")) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        Bukkit.broadcastMessage("§8[§cRestart§8]§c Der Server startet in 15 Sekunden neu!");
        Utils.waitSeconds(5, () -> {
            Bukkit.broadcastMessage("§8[§cRestart§8]§c Der Server startet in 10 Sekunden neu!");
            Utils.waitSeconds(5, () -> {
                Bukkit.broadcastMessage("§8[§cRestart§8]§c Der Server startet in 5 Sekunden neu!");
                Utils.waitSeconds(2, () -> {
                    Bukkit.broadcastMessage("§8[§cRestart§8]§c Der Server startet in 3 Sekunden neu!");
                    Utils.waitSeconds(1, () -> {
                        Bukkit.broadcastMessage("§8[§cRestart§8]§c Der Server startet in 2 Sekunden neu!");
                        Utils.waitSeconds(1, () -> {
                            Bukkit.broadcastMessage("§8[§cRestart§8]§c Der Server startet in 1 Sekunden neu!");
                            Utils.waitSeconds(1, () -> {
                                Bukkit.broadcastMessage("§8[§cReload§8]§c Der Server stoppt jetzt!");
                                Bukkit.shutdown();
                            });
                        });
                    });
                });
            });
        });
        return false;
    }
}
