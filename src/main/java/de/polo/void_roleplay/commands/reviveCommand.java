package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.PlayerUtils.ChatUtils;
import de.polo.void_roleplay.PlayerUtils.DeathUtil;
import de.polo.void_roleplay.PlayerUtils.progress;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class reviveCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (Objects.equals(playerData.getFaction(), "Medic")) {
            Collection<Entity> entities = player.getWorld().getNearbyEntities(player.getLocation(), 3, 3, 3);
            Item nearestSkull = null;
            double nearestDistance = Double.MAX_VALUE;
            for (Entity entity : entities) {
                if (entity instanceof Item && entity.getType() == EntityType.DROPPED_ITEM) {
                    System.out.println("1 item gefunden");
                    Item item = (Item) entity;
                    if (item.getItemStack().getType() == Material.PLAYER_HEAD) {
                        double distance = item.getLocation().distance(player.getLocation());
                        if (distance < nearestDistance) {
                            nearestSkull = item;
                            nearestDistance = distance;
                        }
                    }
                }
            }

            if (nearestSkull != null) {
                SkullMeta skullMeta = (SkullMeta) nearestSkull.getItemStack().getItemMeta();
                UUID uuid = Objects.requireNonNull(skullMeta.getOwningPlayer()).getUniqueId();
                Player targetplayer = Bukkit.getPlayer(uuid);
                PlayerData targetplayerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
                targetplayer.sendMessage(Main.prefix + "Du wirst von " + player.getName() + " wiederbelebt.");
                player.sendMessage(Main.prefix + "Du fängst an " + targetplayer.getName() + " wiederzubeleben.");
                ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " fängt an " + targetplayer.getName() + " wiederzubeleben.");
                progress.start(player, 6);
                final Item skull = nearestSkull;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (skull.getLocation().distance(player.getLocation()) < 3) {
                            DeathUtil.RevivePlayer(targetplayer);
                            targetplayer.teleport(player.getLocation());
                            PlayerManager.addExp(player, Main.random(2, 5));
                            try {
                                FactionManager.addFactionMoney("Medic", ServerManager.getPayout("revive"), "Revive durch " + player.getName());
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            cancel();
                        }
                    }
                }.runTaskLater(Main.getInstance(), 20*6);
            } else {
                player.sendMessage(Main.error + "Kein Spieler in der nähe gefunden.");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
