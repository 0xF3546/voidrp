package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.commands.BombeCommand;
import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.faction.gangwar.Gangwar;
import de.polo.voidroleplay.game.faction.streetwar.StreetwarData;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.game.faction.gangwar.GangwarUtils;
import de.polo.voidroleplay.game.faction.streetwar.Streetwar;
import de.polo.voidroleplay.utils.GamePlay.MilitaryDrop;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;

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
    @SneakyThrows
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
            Player player = event.getEntity().getPlayer();
            assert player != null;
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            boolean removeKarma = true;
            event.setKeepInventory(true);
            playerData.setDeathLocation(player.getLocation());
            if (BombeCommand.ACTIVE) {
                if (ItemManager.getCustomItemCount(player, RoleplayItem.DRAHT) >= 1) {
                    ItemStack draht = ItemManager.createItem(RoleplayItem.DRAHT.getMaterial(), 1, 0, RoleplayItem.DRAHT.getDisplayName(), Main.getInstance().commands.bombeCommand.getDrahtColor());
                    player.getWorld().dropItemNaturally(player.getLocation(), draht);
                    ItemManager.removeCustomItem(player, RoleplayItem.DRAHT, 1);
                }
            } else {
                ItemManager.removeCustomItem(player, RoleplayItem.DRAHT, 1);
            }
            if (MilitaryDrop.ACTIVE) {
                if (player.getKiller() == null) return;
                if (Main.getInstance().gamePlay.militaryDrop.handleDeath(player, player.getKiller())) return;
            }
            if (playerData.getVariable("ffa") != null) {
                playerData.setDead(true);
                playerData.setDeathTime(5);
                Main.getInstance().gamePlay.getFfa().handleDeath(player);
                return;
            } else {
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
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                assert meta != null;
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
                meta.setDisplayName("§7" + player.getName());
                skull.setItemMeta(meta);

                Player killer = player.getKiller();
                UUID playerUUID = player.getUniqueId();

                Item item = player.getLocation().getWorld().dropItemNaturally(player.getLocation(), skull);
                utils.deathUtil.addDeathSkull(player.getUniqueId().toString(), item);
                if ((ServerManager.contractDataMap.get(playerUUID.toString()) != null
                        && killer != null
                        && Objects.equals(factionManager.faction(killer), "ICA"))
                        || playerData.isHitmanDead()) {

                    item.setCustomName("§8" + player.getName());
                    item.setCustomNameVisible(true);

                    ContractData contractData = ServerManager.contractDataMap.get(playerUUID.toString());

                    for (Player players : Bukkit.getOnlinePlayers()) {
                        PlayerData playersData = playerManager.getPlayerData(players);
                        if (playersData.getFaction() == null) continue;
                        if (playersData.getFaction().equalsIgnoreCase("ICA")) {
                            players.sendMessage("§8[§cKopfgeld§8]§e " + factionManager.getPlayerFactionRankName(killer)
                                    + " " + killer.getName()
                                    + " §7hat sich das Kopfgeld von §e" + player.getName()
                                    + " §7geholt. §8(§a+" + contractData.getAmount() + "$§8)");
                        }
                    }

                    PlayerData killerData = playerManager.getPlayerData(killer);
                    killerData.addKarma(Main.random(1, 3), false);
                    removeKarma = false;
                    playerManager.addExp(killer, Main.random(30, 90));
                    ServerManager.contractDataMap.remove(playerUUID.toString());

                    try {
                        Statement statement = Main.getInstance().mySQL.getStatement();
                        statement.execute("DELETE FROM `contract` WHERE `uuid` = '" + player.getUniqueId() + "'");
                        factionManager.addFactionMoney("ICA", contractData.getAmount(), "Kopfgeld " + player.getName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

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
                            PlayerData playersData = playerManager.getPlayerData(players.getUniqueId());
                            if (playersData.getFaction() == null) continue;
                            if (killerData.getFaction() == null) continue;
                            if (playersData.getFaction().equals(killerData.getFaction())) {
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
                                    Main.getInstance().seasonpass.didQuest(player.getKiller(), 17);
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
                if (killerData != null) killerData.removeKarma(Main.random(1, 3), false);
                Main.getInstance().seasonpass.didQuest(player.getKiller(), 5);
                PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("INSERT INTO death_logs (uuid, killer) VALUES (?, ?)");
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, player.getKiller().getUniqueId().toString());
                statement.execute();
                statement.close();
            }


        }
}
