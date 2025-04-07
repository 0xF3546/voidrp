package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.WeaponData;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.manager.WeaponManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.player.enums.Gender;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.utils.player.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
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
        Player player = event.getPlayer();
        if (event.getRightClicked() instanceof Player) {
            Player targetplayer = (Player) event.getRightClicked();
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            if (player.getGameMode().equals(GameMode.SPECTATOR)) return;
            if (playerData.isDead()) {
                event.setCancelled(true);
                return;
            }
            if (player.isSneaking()) {
                playerManager.openInterActionMenu(player, targetplayer);
                return;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            PlayerData targetPlayerData = playerManager.getPlayerData(targetplayer);
            if (ItemManager.equals(item, RoleplayItem.CROWBAR)) {
                if (targetPlayerData.isCuffed()) {
                    targetPlayerData.setCuffed(false);
                    ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + "'s Handschellen geknackt");
                    ItemManager.removeCustomItem(player, RoleplayItem.CROWBAR, 1);
                }
            } else if (ItemManager.equals(item, RoleplayItem.CUFF)) {
                if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "handschellen")) {
                    if (targetPlayerData.isAduty()) {
                        player.sendMessage(Prefix.ERROR + "Du kannst Spieler im Admindienst nicht fesseln.");
                        return;
                    }
                    if (playerData.getFaction() != null) {
                        if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
                            if (!targetPlayerData.isCuffed()) {
                                if (playerData.getVariable("wantsToCuff") == null) {
                                    Main.getInstance().getCooldownManager().setCooldown(player, "handschellen_anlegen", 5);
                                    playerData.setVariable("wantsToCuff", true);
                                    return;
                                } else {
                                    if (Main.getInstance().getCooldownManager().isOnCooldown(player, "handschellen_anlegen")) {
                                        player.sendMessage(Prefix.MAIN + "Warte noch " + Main.getInstance().getCooldownManager().getRemainingTime(player, "handschellen_anlegen") + " Sekunden.");
                                        return;
                                    }
                                }
                                ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " Handschellen angelegt.");
                                targetPlayerData.setCuffed(true);
                                playerData.setVariable("wantsToCuff", null);
                                ItemManager.removeCustomItem(player, RoleplayItem.CUFF, 1);
                            } else {
                                for (WeaponData weaponData : WeaponManager.weaponDataMap.values()) {
                                    if (player.getInventory().getItemInMainHand().getType().equals(weaponData.getMaterial())) {
                                        return;
                                    }
                                }
                                ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " Handschellen abgenommen.");
                                targetPlayerData.setCuffed(false);
                                ItemManager.addCustomItem(player, RoleplayItem.CUFF, 1);
                            }
                        }
                    }
                    Main.getInstance().getCooldownManager().setCooldown(player, "handschellen", 1);
                }
            } else if (item.getType() == Material.DIAMOND) {
                if (item.getItemMeta().getDisplayName().contains("Ehering")) {
                    PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
                    if (playerData.getRelationShip() == null) return;
                    if (playerData.getRelationShip().get(targetplayer.getUniqueId().toString()).equals("verlobt")) {
                        if (targetplayerData.getRelationShip().get(player.getUniqueId().toString()).equals("verlobt")) {
                            ItemStack itemStack = item.clone();
                            itemStack.setAmount(1);
                            player.getInventory().removeItem(itemStack);
                            if (targetplayer.isOnline()) {
                                if (targetplayerData.getGender() == playerData.getGender()) {
                                    player.sendMessage(Prefix.ERROR + "Personen mit dem gleichen Geschlecht können nicht heiraten.");
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
                                    Statement statement = Main.getInstance().coreDatabase.getStatement();
                                    JSONObject object = new JSONObject(playerData.getRelationShip());
                                    statement.executeUpdate("UPDATE `players` SET `relationShip` = '" + object + "', `lastname` = '" + playerData.getLastname() + "' WHERE `uuid` = '" + player.getUniqueId() + "'");

                                    JSONObject object2 = new JSONObject(targetplayerData.getRelationShip());
                                    statement.executeUpdate("UPDATE `players` SET `relationShip` = '" + object2 + "', `lastname` = '" + targetplayerData.getLastname() + "'  WHERE `uuid` = '" + targetplayer.getUniqueId() + "'");
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                player.sendMessage(Prefix.ERROR + "Spieler konnte nicht gefunden werden.");
                            }
                        } else {
                            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " & du seid nicht verlobt.");
                        }
                    } else {
                        player.sendMessage(Prefix.ERROR + player.getName() + " & du seid nicht verlobt.");
                    }
                }
            } else if (item.getType().equals(RoleplayItem.IBOPROFEN.getMaterial()) && item.getItemMeta().getDisplayName().equalsIgnoreCase(RoleplayItem.IBOPROFEN.getDisplayName()) && !Main.getInstance().getCooldownManager().isOnCooldown(player, "ibo")) {
                if (playerData.getFaction().equals("Medic")) {
                    Main.getInstance().getCooldownManager().setCooldown(player, "ibo", 1);
                    targetplayer.addPotionEffect(PotionEffectType.ABSORPTION.createEffect(12000, 1));
                    targetplayer.addPotionEffect(PotionEffectType.HEALTH_BOOST.createEffect(12000, 1));
                    targetplayer.sendMessage("§dMediziner " + player.getName() + " hat dir Iboprofen verabreicht.");
                    player.sendMessage("§dDu hast " + targetplayer.getName() + " Iboprofen verabreicht.");
                    ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " Iboprofen verabreicht.");
                    ItemManager.removeCustomItem(player, RoleplayItem.IBOPROFEN, 1);
                } else {
                    player.sendMessage(Prefix.ERROR + "Dieses Feature steht nur der Fraktion \"Medic\" zu Verfügung.");
                }
            } else if (ItemManager.equals(item, RoleplayItem.BANDAGE) && !Main.getInstance().getCooldownManager().isOnCooldown(player, "band")) {
                if (Main.getInstance().getCooldownManager().isOnCooldown(targetplayer, "bandage")) {
                    player.sendMessage(Component.text("§5Warte noch " + Main.getInstance().getCooldownManager().getRemainingTime(targetplayer, "bandage") + " Sekunden"));
                    return;
                }
                if (playerData.getFaction().equals("Medic")) {
                    Main.getInstance().getCooldownManager().setCooldown(player, "band", 1);
                    Main.getInstance().getCooldownManager().setCooldown(targetplayer, "bandage", 120);
                    targetplayer.addPotionEffect(PotionEffectType.ABSORPTION.createEffect(12000, 1));
                    targetplayer.addPotionEffect(PotionEffectType.REGENERATION.createEffect(15 * 20, 1));
                    targetplayer.sendMessage("§dMediziner " + player.getName() + " hat dir Bandage verabreicht.");
                    player.sendMessage("§dDu hast " + targetplayer.getName() + " Bandage verabreicht.");
                    ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + targetplayer.getName() + " Bandage verabreicht.");
                    ItemManager.removeCustomItem(player, RoleplayItem.BANDAGE, 1);
                } else {
                    player.sendMessage(Prefix.ERROR + "Dieses Feature steht nur der Fraktion \"Medic\" zu Verfügung.");
                }
            }
        }
        if ((event.getRightClicked().getType() == EntityType.ARMOR_STAND || event.getRightClicked().getType() == EntityType.ITEM_FRAME || event.getRightClicked().getType() == EntityType.PAINTING) && !playerManager.getPlayerData(event.getPlayer().getUniqueId()).isAduty()) {
            event.setCancelled(true);
        }
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            String command = villager.getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "command"), PersistentDataType.STRING);
            if (command == null) return;
            if (playerManager.getPlayerData(event.getPlayer().getUniqueId()).isAduty()) {
                String id_name = villager.getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "name"), PersistentDataType.STRING);
                event.getPlayer().sendMessage(Prefix.GAMEDESIGN + "Befehl§8:§f " + command + "§8 | §7ID-Name§8:§f " + id_name);
            } else {
                event.getPlayer().performCommand(command);
            }
        }
        if (event.getRightClicked() instanceof Mob) {
            if (!player.getInventory().getItemInMainHand().getType().equals(Material.DEBUG_STICK)) return;
            event.getRightClicked().remove();
        }
    }
}
