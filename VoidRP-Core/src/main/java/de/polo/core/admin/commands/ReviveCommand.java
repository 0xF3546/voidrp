package de.polo.core.admin.commands;

import de.polo.core.Main;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.player.ChatUtils;
import de.polo.core.utils.player.Progress;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import java.util.Objects;
import java.util.UUID;

public class ReviveCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;
    private final FactionManager factionManager;

    public ReviveCommand(PlayerManager playerManager, Utils utils, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.utils = utils;
        this.factionManager = factionManager;
        Main.registerCommand("revive", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getVariable("isReviving") == null) {
            playerData.setVariable("isReviving", false);
        }
        boolean reviving = playerData.getVariable("isReviving");
        if (reviving) {
            return false;
        }
        if (Objects.equals(playerData.getFaction(), "Medic")) {
            Collection<Entity> entities = player.getWorld().getNearbyEntities(player.getLocation(), 3, 3, 3);
            Item nearestSkull = null;
            double nearestDistance = Double.MAX_VALUE;
            for (Entity entity : entities) {
                if (entity instanceof Item item && entity.getType() == EntityType.DROPPED_ITEM) {
                    if (item.getItemStack().getType() == Material.PLAYER_HEAD) {
                        double distance = item.getLocation().distance(player.getLocation());
                        if (distance < nearestDistance) {
                            if (!item.getCustomName().contains("§8")) {
                                nearestSkull = item;
                                nearestDistance = distance;
                            } else {
                                player.sendMessage(Prefix.ERROR + "Du kannst diesen Spieler nicht wiederbeleben.");
                            }
                        }
                    }
                }
            }

            if (nearestSkull != null) {
                SkullMeta skullMeta = (SkullMeta) nearestSkull.getItemStack().getItemMeta();
                final Item skull = nearestSkull;
                if (skull.getOwner() == player.getUniqueId()) {
                    player.sendMessage(Prefix.ERROR + "Du kannst dich nicht selbst wiederbeleben.");
                    return false;
                }
                if (!playerData.isDuty()) {
                    player.sendMessage(Prefix.ERROR + "Du bist nicht im Dienst.");
                    return false;
                }
                UUID uuid = Objects.requireNonNull(skullMeta.getOwningPlayer()).getUniqueId();
                Player targetplayer = Bukkit.getPlayer(uuid);
                PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
                if (targetplayerData.isHitmanDead()) {
                    return false;
                }
                playerData.setVariable("isReviving", true);
                targetplayer.sendMessage(Prefix.MAIN + "Du wirst von " + player.getName() + " wiederbelebt.");
                player.sendMessage(Prefix.MAIN + "Du fängst an " + targetplayer.getName() + " wiederzubeleben.");
                ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " fängt an " + targetplayer.getName() + " wiederzubeleben.");
                Progress.start(player, 6);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (skull.getLocation().distance(player.getLocation()) < 3) {
                            utils.deathUtil.revivePlayer(targetplayer, true);
                            playerManager.addExp(player, Utils.random(2, 5));
                            targetplayer.sendMessage(Prefix.MAIN + "Du wurdest wiederbelebt.");
                            playerData.setVariable("isReviving", false);
                            try {
                                factionManager.addFactionMoney("Medic", ServerManager.getPayout("revive"), "Revive durch " + player.getName());
                                playerManager.removeBankMoney(targetplayer, ServerManager.getPayout("revive"), "Medizinische Behandlung");
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            cancel();
                        }
                    }
                }.runTaskLater(Main.getInstance(), 20 * 6);
            } else {
                player.sendMessage(Prefix.ERROR + "Kein Spieler in der nähe gefunden.");
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }
}
