package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class SprengguertelCommand implements CommandExecutor {

    private final PlayerManager playerManager;
    private final Utils utils;

    public SprengguertelCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;

        Main.registerCommand("sprenggürtel", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("§cBitte gib eine Verzögerungszeit an.");
            return false;
        }

        int delay;
        try {
            delay = Integer.parseInt(args[0]);
            player.sendMessage("§cDein Sprenggürtel geht in " + args[0] + " hoch.");
        } catch (NumberFormatException e) {
            player.sendMessage("§cDie angegebene Verzögerungszeit ist ungültig.");
            return false;
        }

        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction().equalsIgnoreCase("Terroristen")) {
            player.sendMessage("§cDafür hast du keine rechte.");
            return false;
        }

        if (player.getInventory().getChestplate() != null &&
                player.getInventory().getChestplate().getType() == Material.LEATHER_CHESTPLATE) {
            Location loc = player.getLocation();
            new BukkitRunnable() {
                @Override
                public void run() {
                    loc.getWorld().createExplosion(loc, 10.0f, false, false);
                    player.sendMessage("§cDein Sprenggürtel ist explodiert.");
                    utils.deathUtil.setHitmanDeath(player);
                    player.getInventory().getChestplate().setType(Material.AIR);
                }
            }.runTaskLater(Main.getInstance(), delay * 20L);
        } else {
            player.sendMessage("§cDu hast keinen Sprenggürtel an!");
        }

        return true;
    }
}
