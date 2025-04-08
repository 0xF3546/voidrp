package de.polo.core.admin.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpeedCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public SpeedCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("speed", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 70 && !player.getGameMode().equals(GameMode.CREATIVE)) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (!playerData.isAduty() && !player.getGameMode().equals(GameMode.CREATIVE)) {
            player.sendMessage(Prefix.ADMIN_ERROR + "Du bist nicht im Admindienst!");
            return false;
        }
        if (args.length < 1) {
            player.setFlySpeed(0.1F);
            player.sendMessage(Prefix.ADMIN + "Dein Fly-Speed wurde §czurückgesetzt§7.");
            return false;
        }
        float speed = 0;
        try {
            speed = Float.parseFloat(args[0].replace(",", "."));
        } catch (IllegalArgumentException e) {
            player.sendMessage(Prefix.ERROR + "Das ist keine gültige Zahl.");
            return false;
        }
        if (speed < 0 || speed > 10) {
            player.sendMessage(Prefix.ERROR + "Der Speed muss von 0-10 sein!");
            return false;
        }
        speed = speed / 10;
        player.sendMessage(Prefix.ADMIN + "Dein Fly-Speed wurde auf §c" + args[0] + "§7 gestellt.");
        player.setFlySpeed(speed);
        return false;
    }
}
