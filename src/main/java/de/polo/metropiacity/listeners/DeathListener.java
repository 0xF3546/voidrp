package de.polo.metropiacity.listeners;

import de.polo.metropiacity.dataStorage.*;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.playerUtils.DeathUtils;
import de.polo.metropiacity.playerUtils.FFAUtils;
import de.polo.metropiacity.playerUtils.GangwarUtils;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.ServerManager;
import de.polo.metropiacity.commands.ADutyCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class DeathListener implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
            Player player = event.getEntity().getPlayer();
            assert player != null;
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            event.setKeepInventory(true);
            if (playerData.getVariable("current_lobby") != null) {
                FFAUtils.useSpawn(player, playerData.getIntVariable("current_lobby"));
            } else {
                playerData.setDeathLocation(player.getLocation());
                if (!playerData.isDead()) {
                    ADutyCommand.send_message(player.getName() + " starb.");
                } else {
                    ADutyCommand.send_message(player.getName() + " starb. (Rejoin)");
                }
                DeathUtils.startDeathTimer(player);
                if (!playerData.isDead()) {
                    playerData.setDead(true);
                    playerData.setDeathTime(300);
                }
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                assert meta != null;
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
                meta.setDisplayName("§7" + player.getName());
                skull.setItemMeta(meta);

                Item item = player.getLocation().getWorld().dropItemNaturally(player.getLocation(), skull);
                DeathUtils.deathSkulls.put(player.getUniqueId().toString(), item);
                if (ServerManager.contractDataMap.get(player.getUniqueId().toString()) != null && Objects.equals(FactionManager.faction(Objects.requireNonNull(player.getKiller())), "ICA")) {
                    ContractData contractData = ServerManager.contractDataMap.get(player.getUniqueId().toString());
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        if (FactionManager.faction(players).equals("ICA")) {
                            players.sendMessage("§8[§cKopfgeld§8]§e " + FactionManager.getPlayerFactionRankName(player.getKiller()) + " " + player.getKiller().getName() + " §7hat sich das Kopfgeld von §e" + player.getName() + " §7geholt. §8(§a+" + contractData.getAmount() + "$§8)");
                        }
                    }
                    PlayerManager.addExp(player.getKiller(), Main.random(10, 30));
                    ServerManager.contractDataMap.remove(player.getUniqueId().toString());
                    try {
                        Statement statement = MySQL.getStatement();
                        statement.execute("DELETE FROM `contract` WHERE `uuid` = '" + player.getUniqueId() + "'");
                        FactionManager.addFactionMoney("ICA", contractData.getAmount(), "Kopfgeld " + player.getName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    item.setCustomName("§8" + player.getName());
                    item.setCustomNameVisible(true);
                    DeathUtils.setHitmanDeath(player);
                    player.sendMessage("§8[§cKopfgeld§8]§7 Ein Kopfgeldjäger hat dich getötet.");
                    player.sendMessage("§8 ➥ §bInfo§8:§f Du kannst nun nicht mehr wiederbelebt werden & bist 5 Minuten länger tot.");
                } else {
                    item.setCustomName("§7" + player.getName());
                    item.setCustomNameVisible(true);
                    if (playerData.getVariable("gangwar") != null) {
                        DeathUtils.setGangwarDeath(player);
                        PlayerData killerData = PlayerManager.playerDataMap.get(player.getKiller().getUniqueId().toString());
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            if (PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getFaction().equals(killerData.getFaction())) {
                                players.sendMessage("§8[§cGangwar§8]§7 +3 Punkte für das Töten eines Gegners (" + player.getKiller().getName() + "§8 » §7" + player.getName() + ").");
                            }
                        }
                        GangwarData gangwarData = GangwarUtils.gangwarDataMap.get(playerData.getVariable("gangwar"));
                        if (gangwarData.getAttacker().equals(killerData.getFaction())) {
                            gangwarData.setAttackerPoints(gangwarData.getAttackerPoints() + 3);
                        } else {
                            gangwarData.setDefenderPoints(gangwarData.getDefenderPoints() + 3);
                        }
                    } else {
                        if (player.getKiller() == null) return;
                        PlayerData killerData = PlayerManager.playerDataMap.get(player.getKiller().getUniqueId().toString());
                        if (killerData.getFaction() == null) return;
                        for (BlacklistData blacklistData : FactionManager.blacklistDataMap.values()) {
                            if (blacklistData.getUuid().equals(player.getUniqueId().toString())) {
                                if (killerData.getFaction().equals(blacklistData.getFaction())) {
                                    FactionData factionData = FactionManager.factionDataMap.get(blacklistData.getFaction());
                                    player.sendMessage("§8[§cBlacklist§8]§7 Du wurdest getötet, weil du auf der Blacklist der " + factionData.getFullname() + " bist.");
                                    PlayerManager.addExp(player.getKiller(), Main.random(3, 9));
                                    if (blacklistData.getKills() > 1) {
                                        blacklistData.setKills(blacklistData.getKills() - 1);
                                        try {
                                            Statement statement = MySQL.getStatement();
                                            statement.executeUpdate("UPDATE blacklist SET kills = " + blacklistData.getKills() + " WHERE uuid = '" + player.getUniqueId() + "' AND faction = '" + factionData.getName() + "'");
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        try {
                                            Statement statement = MySQL.getStatement();
                                            statement.execute("DELETE FROM `blacklist` WHERE `id` = " + blacklistData.getId());
                                            FactionManager.sendMessageToFaction(factionData.getName(), "§c" + player.getName() + " wurde von der Blacklist gelöscht.");
                                            FactionManager.blacklistDataMap.remove(blacklistData.getId());
                                            player.sendMessage("§8[§cBlacklist§8]§7 Du wurdest von der Blacklist der " + factionData.getFullname() + " gelöscht.");
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
}
