package de.polo.metropiacity.Listener;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.PlayerUtils.ChatUtils;
import de.polo.metropiacity.PlayerUtils.rubbellose;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.VertragUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.JSONObject;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class playerInteractWithPlayerListener implements Listener {
    @EventHandler
    public void onPlayerInteractWithPlayer(PlayerInteractEntityEvent event) {
            if (event.getRightClicked() instanceof Player) {
                Player player = event.getPlayer();
                Player targetplayer = (Player) event.getRightClicked();
                PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
                System.out.println(player.getName());
                System.out.println(targetplayer.getName());
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.LEAD) {
                    if (!Main.cooldownManager.isOnCooldown(player, "handschellen")) {
                        if (!PlayerManager.canPlayerMove(targetplayer)) {
                            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " Handschellen angelegt.");
                            PlayerManager.setPlayerMove(targetplayer, false);
                        } else {
                            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " Handschellen abgenommen.");
                            PlayerManager.setPlayerMove(targetplayer, true);
                        }
                        Main.cooldownManager.setCooldown(player, "handschellen", 1);
                    }
                } else if (item.getType() == Material.DIAMOND) {
                    if (item.getItemMeta().getDisplayName().contains("Ehering")) {
                        PlayerData targetplayerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
                        if (playerData.getRelationShip().get(targetplayer.getUniqueId().toString()).equals("verlobt")) {
                            if (targetplayerData.getRelationShip().get(player.getUniqueId().toString()).equals("verlobt")) {
                                ItemStack itemStack = item.clone();
                                itemStack.setAmount(1);
                                player.getInventory().removeItem(itemStack);
                                if (targetplayer.isOnline()) {
                                    player.sendMessage("§6Du und " + targetplayer.getName() + " sind jetzt verheiratet.");
                                    targetplayer.sendMessage("§6Du und " + player.getName() + " sind jetzt verheiratet.");
                                    Bukkit.broadcastMessage("§8[§6News§8]§e " + player.getName() + " & " + targetplayer.getName() + " sind jetzt Verheiratet. Herzlichen Glückwunsch!");
                                    HashMap<String, String> hmap1 = new HashMap<>();
                                    hmap1.put(player.getUniqueId().toString(), "verheiratet");
                                    targetplayerData.getRelationShip().clear();
                                    targetplayerData.setRelationShip(hmap1);

                                    HashMap<String, String> hmap2 = new HashMap<>();
                                    hmap2.put(targetplayer.getUniqueId().toString(), "verheiratet");
                                    playerData.getRelationShip().clear();
                                    playerData.setRelationShip(hmap2);
                                    try {
                                        Statement statement = MySQL.getStatement();
                                        JSONObject object = new JSONObject(playerData.getRelationShip());
                                        statement.executeUpdate("UPDATE `players` SET `relationShip` = '" + object + "' WHERE `uuid` = '" + player.getUniqueId() + "'");

                                        JSONObject object2 = new JSONObject(targetplayerData.getRelationShip());
                                        statement.executeUpdate("UPDATE `players` SET `relationShip` = '" + object2 + "' WHERE `uuid` = '" + targetplayer.getUniqueId() + "'");
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    player.sendMessage(Main.error + "Spieler konnte nicht gefunden werden.");
                                }
                            } else {
                                player.sendMessage(Main.error + targetplayer.getName() + " & du seid nicht verlobt.");
                            }
                        } else {
                            player.sendMessage(Main.error + player.getName() + " & du seid nicht verlobt.");
                        }
                    }
                } else if (item.getType().equals(Material.PAPER) && item.getItemMeta().getDisplayName().equalsIgnoreCase("§c§lIboprofen")) {
                    if (playerData.getFaction().equals("Medic")) {
                        targetplayer.addPotionEffect(PotionEffectType.ABSORPTION.createEffect(12, 1));
                        targetplayer.addPotionEffect(PotionEffectType.HEAL.createEffect(12, 1));
                        targetplayer.sendMessage("§dMediziner " + player.getName() + " hat dir Iboprofen verabreicht.");
                        player.sendMessage("§dDu hast " + targetplayer.getName() + " Iboprofen verabreicht.");
                        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " Iboprofen verabreicht.");
                    } else {
                        player.sendMessage(Main.error + "Dieses Feature steht nur der Fraktion \"Medic\" zu Verfügung.");
                    }
                } else {
                    if (player.isSneaking()) PlayerManager.openInterActionMenu(player, targetplayer);
                }
            }
        if ((event.getRightClicked().getType() == EntityType.ARMOR_STAND || event.getRightClicked().getType() == EntityType.ITEM_FRAME || event.getRightClicked().getType() == EntityType.PAINTING) && !PlayerManager.playerDataMap.get(event.getPlayer().getUniqueId().toString()).isAduty()) {
            event.setCancelled(true);
        }
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            String command = villager.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "command"), PersistentDataType.STRING);
            assert command != null;
            if (PlayerManager.playerDataMap.get(event.getPlayer().getUniqueId().toString()).isAduty()) {
                String id_name = villager.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "name"), PersistentDataType.STRING);
                event.getPlayer().sendMessage(Main.gamedesign_prefix + "Befehl§8:§f " + command + "§8 | §7ID-Name§8:§f " + id_name);
            } else {
                event.getPlayer().performCommand(command);
            }
        }
    }
}
