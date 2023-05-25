package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.DataStorage.ContractData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.PlayerUtils.DeathUtil;
import de.polo.void_roleplay.PlayerUtils.FFA;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.ItemManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.ServerManager;
import de.polo.void_roleplay.commands.aduty;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.SQLException;
import java.sql.Statement;

public class deathListener implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
            Player player = event.getEntity().getPlayer();
            assert player != null;
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            event.setKeepInventory(true);
            if (playerData.getVariable("current_lobby") != null) {
                FFA.useSpawn(player, playerData.getIntVariable("current_lobby"));
            } else {
                playerData.setDeathLocation(player.getLocation());
                if (!playerData.isDead()) {
                    aduty.send_message("§c" + player.getName() + "§7 starb.");
                } else {
                    aduty.send_message("§c" + player.getName() + "§7 starb. (Rejoin)");
                }
                DeathUtil.startDeathTimer(player);
                if (!playerData.isDead()) playerData.setDead(true);
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                assert meta != null;
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
                meta.setDisplayName("§7" + player.getName());
                skull.setItemMeta(meta);

                Item item = player.getLocation().getWorld().dropItemNaturally(player.getLocation(), skull);
                DeathUtil.deathSkulls.put(player.getUniqueId().toString(), item);
                Entity entity = item;
                if (ServerManager.contractDataMap.get(player.getUniqueId().toString()) != null) {
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
                        statement.execute("DELETE FROM `contract` WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
                        FactionManager.addFactionMoney("ICA", contractData.getAmount(), "Kopfgeld " + player.getName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    entity.setCustomName("§8" + player.getName());
                    entity.setCustomNameVisible(true);
                    DeathUtil.setHitmanDeath(player);
                    player.sendMessage("§8[§cKopfgeld§8]§7 Ein Kopfgeldjäger hat dich getötet.");
                    player.sendMessage("§8 ➥ §bInfo§8:§f Du kannst nun nicht mehr wiederbelebt werden & bist 5 Minuten länger tot.");
                } else {
                    entity.setCustomName("§7" + player.getName());
                    entity.setCustomNameVisible(true);
                }

            }
        }
}
