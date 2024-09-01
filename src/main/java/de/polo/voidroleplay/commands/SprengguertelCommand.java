package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.game.base.extra.ExplosionBelt;
import de.polo.voidroleplay.game.events.SecondTickEvent;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SprengguertelCommand implements CommandExecutor, Listener {

    private final PlayerManager playerManager;
    private final Utils utils;

    private final List<ExplosionBelt> explosionBelts = new ArrayList<>();

    public SprengguertelCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;

        Main.registerCommand("sprenggürtel", this);
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /sprenggürtel [Zeit (in Sekunden)]");
            return false;
        }

        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.getFaction().equalsIgnoreCase("Terroristen")) {
            player.sendMessage("§cDafür hast du keine rechte.");
            return false;
        }

        int delay;
        try {
            delay = Integer.parseInt(args[0]);
            if (delay < 7) {
                player.sendMessage(Prefix.ERROR + "Die Zeit muss größer als 7 sein.");
                return false;
            }
            player.sendMessage("§cDein Sprenggürtel geht in " + args[0] + " sekunden hoch.");
        } catch (NumberFormatException e) {
            player.sendMessage(Prefix.ERROR + "Die Zeit muss numerisch sein.");
            return false;
        }

        if (player.getInventory().getChestplate() != null &&
                player.getInventory().getChestplate().getType() == Material.LEATHER_CHESTPLATE) {
            ExplosionBelt belt = new ExplosionBelt(player, delay);
            explosionBelts.add(belt);
        } else {
            player.sendMessage("§cDu hast keinen Sprenggürtel an!");
        }

        return true;
    }

    @EventHandler
    public void onSecond(SecondTickEvent event) {
        for (ExplosionBelt belt : explosionBelts) {
            Player player = belt.getPlayer();
            if (belt.getSeconds() >= 1) {
                belt.setSeconds(belt.getSeconds() - 1);
                utils.sendActionBar(player, "§cNoch " + belt.getSeconds() + " Sekunden!");
                continue;
            }
            player.getWorld().createExplosion(player.getLocation(), 10.0f, false, false);
            if (player.getInventory().getChestplate() != null)
                player.getInventory().getChestplate().setType(Material.AIR);

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getLocation().distance(player.getLocation()) < 10) {
                    p.setHealth(0);
                    continue;
                }
                if (p.getLocation().distance(player.getLocation()) < 15) {
                    p.setHealth(player.getHealth() - 15);
                    continue;
                }
                if (p.getLocation().distance(player.getLocation()) < 20) {
                    p.setHealth(player.getHealth() - 10);
                }
            }
            explosionBelts.remove(belt);

        }
    }
}
