package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.faction.gangwar.Gangwar;
import de.polo.voidroleplay.game.faction.streetwar.StreetwarData;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.game.faction.gangwar.GangwarUtils;
import de.polo.voidroleplay.game.faction.streetwar.Streetwar;
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
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final FactionManager factionManager;
    private final Utils utils;
    private final Streetwar streetwar;
    public DeathListener(PlayerManager playerManager, AdminManager adminManager, FactionManager factionManager, Utils utils, Streetwar streetwar) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.factionManager = factionManager;
        this.utils = utils;
        this.streetwar = streetwar;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
            Player player = event.getEntity().getPlayer();
            assert player != null;
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            boolean removeKarma = true;
            event.setKeepInventory(true);
            if (playerData.getVariable("current_lobby") != null) {
                utils.ffaUtils.useSpawn(player, playerData.getIntVariable("current_lobby"));
            } else {
                playerData.setDeathLocation(player.getLocation());
                if (!playerData.isDead()) {
                    adminManager.send_message(player.getName() + " starb.", null);
                } else {
                    adminManager.send_message(player.getName() + " starb. (Rejoin)", null);
                }
                utils.deathUtil.startDeathTimer(player);
                if (!playerData.isDead()) {
                    playerData.setDead(true);
                    playerData.setDeathTime(300);
                }
                // event.getEntity().getKiller().sendMessage("§8[§c✟§8]§7 " + event.getEntity().getName());
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                assert meta != null;
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
                meta.setDisplayName("§7" + player.getName());
                skull.setItemMeta(meta);

                Item item = player.getLocation().getWorld().dropItemNaturally(player.getLocation(), skull);
                utils.deathUtil.addDeathSkull(player.getUniqueId().toString(), item);
                if (ServerManager.contractDataMap.get(player.getUniqueId().toString()) != null && Objects.equals(factionManager.faction(Objects.requireNonNull(player.getKiller())), "ICA")) {
                    ContractData contractData = ServerManager.contractDataMap.get(player.getUniqueId().toString());
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        if (factionManager.faction(players).equals("ICA")) {
                            players.sendMessage("§8[§cKopfgeld§8]§e " + factionManager.getPlayerFactionRankName(player.getKiller()) + " " + player.getKiller().getName() + " §7hat sich das Kopfgeld von §e" + player.getName() + " §7geholt. §8(§a+" + contractData.getAmount() + "$§8)");
                        }
                    }
                    PlayerData killerData = playerManager.getPlayerData(player.getKiller());
                    killerData.addKarma(Main.random(1, 3), false);
                    removeKarma = false;
                    playerManager.addExp(player.getKiller(), Main.random(10, 30));
                    ServerManager.contractDataMap.remove(player.getUniqueId().toString());
                    try {
                        Statement statement = Main.getInstance().mySQL.getStatement();
                        statement.execute("DELETE FROM `contract` WHERE `uuid` = '" + player.getUniqueId() + "'");
                        factionManager.addFactionMoney("ICA", contractData.getAmount(), "Kopfgeld " + player.getName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    item.setCustomName("§8" + player.getName());
                    item.setCustomNameVisible(true);
                    utils.deathUtil.setHitmanDeath(player);
                    player.sendMessage("§8[§cKopfgeld§8]§7 Ein Kopfgeldjäger hat dich getötet.");
                    player.sendMessage("§8 ➥ §bInfo§8:§f Du kannst nun nicht mehr wiederbelebt werden & bist 5 Minuten länger tot.");
                } else {
                    item.setCustomName("§7" + player.getName());
                    item.setCustomNameVisible(true);
                    if (playerData.getVariable("gangwar") != null) {
                        utils.deathUtil.setGangwarDeath(player);
                        PlayerData killerData = playerManager.getPlayerData(player.getKiller().getUniqueId());
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            if (playerManager.getPlayerData(players.getUniqueId()).getFaction().equals(killerData.getFaction())) {
                                players.sendMessage("§8[§cGangwar§8]§7 +3 Punkte für das Töten eines Gegners (" + player.getKiller().getName() + "§8 » §7" + player.getName() + ").");
                            }
                        }
                        Gangwar gangwarData = Main.getInstance().utils.gangwarUtils.getGangwarByZone(playerData.getVariable("gangwar"));
                        if (!(playerData.getFaction().equals(killerData.getFaction()))) {
                            if (gangwarData.getAttacker().equals(killerData.getFaction())) {
                                gangwarData.setAttackerPoints(gangwarData.getAttackerPoints() + 3);
                            } else {
                                gangwarData.setDefenderPoints(gangwarData.getDefenderPoints() + 3);
                            }
                        }
                    } else {
                        if (player.getKiller() == null) return;
                        PlayerData killerData = playerManager.getPlayerData(player.getKiller().getUniqueId());
                        if (killerData.getFaction() == null) return;
                        for (StreetwarData streetwarData : Streetwar.streetwarDataMap.values()) {
                            if (playerData.getFaction().equalsIgnoreCase(streetwarData.getAttacker()) || playerData.getFaction().equalsIgnoreCase(streetwarData.getDefender())) {
                                if (playerManager.getPlayerData(player.getKiller().getUniqueId()).getFaction().equalsIgnoreCase(streetwarData.getDefender()) || playerManager.getPlayerData(player.getKiller().getUniqueId()).getFaction().equalsIgnoreCase(streetwarData.getAttacker())) {
                                    streetwar.addPunkte(playerManager.getPlayerData(player.getKiller().getUniqueId()).getFaction(), 3, "eliminierung durch " + player.getKiller().getName());
                                }
                            }
                        }
                        for (BlacklistData blacklistData : factionManager.getBlacklists()) {
                            if (blacklistData.getUuid().equals(player.getUniqueId().toString())) {
                                if (killerData.getFaction().equals(blacklistData.getFaction())) {
                                    FactionData factionData = factionManager.getFactionData(blacklistData.getFaction());
                                    player.sendMessage("§8[§cBlacklist§8]§7 Du wurdest getötet, weil du auf der Blacklist der " + factionData.getFullname() + " bist.");
                                    killerData.addKarma(Main.random(1, 3), false);
                                    removeKarma = false;
                                    playerManager.addExp(player.getKiller(), Main.random(3, 9));
                                    if (blacklistData.getKills() > 1) {
                                        blacklistData.setKills(blacklistData.getKills() - 1);
                                        try {
                                            Statement statement = Main.getInstance().mySQL.getStatement();
                                            statement.executeUpdate("UPDATE blacklist SET kills = " + blacklistData.getKills() + " WHERE uuid = '" + player.getUniqueId() + "' AND faction = '" + factionData.getName() + "'");
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        try {
                                            Statement statement = Main.getInstance().mySQL.getStatement();
                                            statement.execute("DELETE FROM `blacklist` WHERE `id` = " + blacklistData.getId());
                                            factionManager.sendMessageToFaction(factionData.getName(), "§c" + player.getName() + " wurde von der Blacklist gelöscht.");
                                            factionManager.removeBlacklist(blacklistData.getId());
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
                PlayerData killerData = playerManager.getPlayerData(player.getKiller());
                killerData.removeKarma(Main.random(1, 3), false);
            }
        }
}
