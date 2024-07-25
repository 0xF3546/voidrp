package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.Gender;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
import de.polo.voidroleplay.utils.PlayerManager;
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
import org.bukkit.potion.PotionEffectType;
import org.json.JSONObject;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class PlayerInteractWithPlayerListener implements Listener {
    private final PlayerManager playerManager;
    public PlayerInteractWithPlayerListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onPlayerInteractWithPlayer(PlayerInteractEntityEvent event) {
            if (event.getRightClicked() instanceof Player) {
                Player player = event.getPlayer();
                Player targetplayer = (Player) event.getRightClicked();
                PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
                if (playerData.isDead()) {
                    event.setCancelled(true);
                    return;
                }
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.LEAD) {
                    if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "handschellen")) {
                        PlayerData targetPlayerData = playerManager.getPlayerData(targetplayer);
                        if (targetPlayerData.isAduty()) {
                            player.sendMessage(Prefix.ERROR + "Du kannst Spieler im Aduty nicht fesseln.");
                            return;
                        }
                        if (!targetPlayerData.isCuffed()) {
                            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " Handschellen angelegt.");
                            targetPlayerData.setCuffed(true);
                        } else {
                            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " Handschellen abgenommen.");
                            targetPlayerData.setCuffed(false);
                        }
                        Main.getInstance().getCooldownManager().setCooldown(player, "handschellen", 1);
                    }
                } else if (item.getType() == Material.DIAMOND) {
                    if (item.getItemMeta().getDisplayName().contains("Ehering")) {
                        PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
                        if (playerData.getRelationShip().get(targetplayer.getUniqueId().toString()).equals("verlobt")) {
                            if (targetplayerData.getRelationShip().get(player.getUniqueId().toString()).equals("verlobt")) {
                                ItemStack itemStack = item.clone();
                                itemStack.setAmount(1);
                                player.getInventory().removeItem(itemStack);
                                if (targetplayer.isOnline()) {
                                    if (targetplayerData.getGender() == playerData.getGender()) {
                                        player.sendMessage(Main.error + "Personen mit dem gleichen Geschlecht können nicht heiraten.");
                                        return;
                                    }
                                    player.sendMessage("§6Du und " + targetplayer.getName() + " sind jetzt verheiratet.");
                                    targetplayer.sendMessage("§6Du und " + player.getName() + " sind jetzt verheiratet.");
                                    Bukkit.broadcastMessage("§8[§6News§8]§e " + player.getName() + " & " + targetplayer.getName() + " sind jetzt Verheiratet. Herzlichen Glückwunsch!");
                                    HashMap<String, String> hmap1 = new HashMap<>();
                                    hmap1.put(player.getUniqueId().toString(), "verheiratet");
                                    targetplayerData.getRelationShip().clear();
                                    targetplayerData.setRelationShip(hmap1);

                                    HashMap<String, String> hmap2 = new HashMap<>();
                                    hmap2.put(targetplayer.getUniqueId().toString(), "verheiratet");
                                    Main.getInstance().beginnerpass.didQuest(player, 20);
                                    Main.getInstance().beginnerpass.didQuest(targetplayer, 20);
                                    playerData.getRelationShip().clear();
                                    playerData.setRelationShip(hmap2);
                                    if (playerData.getGender().equals(Gender.MALE)) {
                                        targetplayerData.setLastname(playerData.getLastname());
                                        targetplayer.sendMessage("§8 » §7Dein Nachname lautet nun \"" + playerData.getLastname() + "\".");
                                    } else {
                                        playerData.setLastname(targetplayerData.getLastname());
                                        player.sendMessage("§8 » §7Dein Nachname lautet nun \"" + playerData.getLastname() + "\".");
                                    }
                                    try {
                                        Statement statement = Main.getInstance().mySQL.getStatement();
                                        JSONObject object = new JSONObject(playerData.getRelationShip());
                                        statement.executeUpdate("UPDATE `players` SET `relationShip` = '" + object + "', `lastname` = '" + playerData.getLastname() +  "' WHERE `uuid` = '" + player.getUniqueId() + "'");

                                        JSONObject object2 = new JSONObject(targetplayerData.getRelationShip());
                                        statement.executeUpdate("UPDATE `players` SET `relationShip` = '" + object2 + "', `lastname` = '" + targetplayerData.getLastname() + "'  WHERE `uuid` = '" + targetplayer.getUniqueId() + "'");
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
                } else if (item.getType().equals(RoleplayItem.IBOPROFEN.getMaterial()) && item.getItemMeta().getDisplayName().equalsIgnoreCase(RoleplayItem.IBOPROFEN.getDisplayName()) && !Main.getInstance().getCooldownManager().isOnCooldown(player, "ibo")) {
                    if (playerData.getFaction().equals("Medic")) {
                        Main.getInstance().getCooldownManager().setCooldown(player, "ibo", 1);
                        targetplayer.addPotionEffect(PotionEffectType.ABSORPTION.createEffect(12000, 1));
                        targetplayer.addPotionEffect(PotionEffectType.HEAL.createEffect(12000, 1));
                        targetplayer.sendMessage("§dMediziner " + player.getName() + " hat dir Iboprofen verabreicht.");
                        player.sendMessage("§dDu hast " + targetplayer.getName() + " Iboprofen verabreicht.");
                        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " Iboprofen verabreicht.");
                        ItemManager.removeCustomItem(player, RoleplayItem.IBOPROFEN, 1);
                    } else {
                        player.sendMessage(Main.error + "Dieses Feature steht nur der Fraktion \"Medic\" zu Verfügung.");
                    }
                } else {
                    if (player.isSneaking()) playerManager.openInterActionMenu(player, targetplayer);
                }
            }
        if ((event.getRightClicked().getType() == EntityType.ARMOR_STAND || event.getRightClicked().getType() == EntityType.ITEM_FRAME || event.getRightClicked().getType() == EntityType.PAINTING) && !playerManager.getPlayerData(event.getPlayer().getUniqueId()).isAduty()) {
            event.setCancelled(true);
        }
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            String command = villager.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "command"), PersistentDataType.STRING);
            if (command == null) return;
            if (playerManager.getPlayerData(event.getPlayer().getUniqueId()).isAduty()) {
                String id_name = villager.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "name"), PersistentDataType.STRING);
                event.getPlayer().sendMessage(Main.gamedesign_prefix + "Befehl§8:§f " + command + "§8 | §7ID-Name§8:§f " + id_name);
            } else {
                event.getPlayer().performCommand(command);
            }
        }
    }
}
