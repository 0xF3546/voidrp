package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.handler.TabCompletion;
import de.polo.core.storage.LocationData;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.location.services.impl.LocationManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TPToCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;

    public TPToCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.registerCommand("tpto", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
        if (voidPlayer.isAduty()) {
            if (args.length >= 1) {
                StringBuilder message = new StringBuilder();
                for (String arg : args) {
                    message.append(" ").append(arg);
                }
                locationManager.useLocation(player, String.valueOf(message).replace(" ", ""));
                player.sendMessage(Prefix.ADMIN + "Du hast dich zu §c" + message + "§7 teleportiert.");
                player.getWorld().playEffect(player.getLocation().add(0.0D, 0.0D, 0.0D), Effect.ENDER_SIGNAL, 1);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 2);
            } else {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /tpto [Punkt]");
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Du bist nicht im Admindienst!");
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, LocationManager.locationDataMap.values()
                        .stream()
                        .map(LocationData::getName)
                        .toList())
                .build();
    }
}
