package de.polo.metropiacity.Utils;

import de.polo.metropiacity.DataStorage.WeaponData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Weapons implements Listener {
    public static Map<Material, WeaponData> weaponDataMap = new HashMap<>();
    public static void loadWeapons() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT `id`, `material`, `name`, `maxAmmo`, `reloadDuration`, `damage`, `weaponSound`, `velocity`, `shootDuration`, `type` FROM `weapons`");
        while (result.next()) {
            WeaponData weaponData = new WeaponData();
            weaponData.setId(result.getInt(1));
            weaponData.setMaterial(Material.valueOf(result.getString(2)));
            weaponData.setName(result.getString(3).replace("&", "§"));
            weaponData.setMaxAmmo(result.getInt(4));
            weaponData.setReloadDuration(result.getFloat(5));
            weaponData.setDamage(result.getFloat(6));
            weaponData.setWeaponSound(Sound.valueOf(result.getString(7)));
            weaponData.setArrowVelocity(result.getFloat(8));
            weaponData.setShootDuration(result.getFloat(9));
            weaponData.setType(result.getString(10));
            weaponDataMap.put(Material.valueOf(result.getString(2)), weaponData);
        }
    }
    public static void giveWeaponToPlayer(Player player, Material material, String type) {
        WeaponData weaponData = weaponDataMap.get(material);
        ItemStack item = new ItemStack(weaponData.getMaterial());
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(weaponData.getName());
        NamespacedKey current_ammo = new NamespacedKey(Main.plugin, "current_ammo");
        meta.getPersistentDataContainer().set(current_ammo, PersistentDataType.INTEGER, weaponData.getMaxAmmo());

        NamespacedKey canShoot = new NamespacedKey(Main.plugin, "canShoot");
        meta.getPersistentDataContainer().set(canShoot, PersistentDataType.INTEGER, 1);

        NamespacedKey isReloading = new NamespacedKey(Main.plugin, "isReloading");
        meta.getPersistentDataContainer().set(isReloading, PersistentDataType.INTEGER, 0);

        NamespacedKey typeKey = new NamespacedKey(Main.plugin, "type");
        meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type);

        meta.setLore(Arrays.asList("§eAirsoft-Waffe", "§8➥ §e" + weaponData.getMaxAmmo() + "§8/§6" + weaponData.getMaxAmmo()));
        item.setItemMeta(meta);
        player.getInventory().addItem(item);

    }
    public static void giveWeaponAmmoToPlayer(Player player, String ammo, Integer amount) {

    }

    @EventHandler
    public void onWeaponUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        WeaponData weaponData = weaponDataMap.get(player.getEquipment().getItemInMainHand().getType());
        if (weaponData != null) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                NamespacedKey current_ammo = new NamespacedKey(Main.plugin, "current_ammo");
                NamespacedKey canShoot = new NamespacedKey(Main.plugin, "canShoot");
                NamespacedKey isReloading = new NamespacedKey(Main.plugin, "isReloading");
                Integer isReloadingPers = event.getItem().getItemMeta().getPersistentDataContainer().get(isReloading, PersistentDataType.INTEGER);
                Integer canShootPers = event.getItem().getItemMeta().getPersistentDataContainer().get(canShoot, PersistentDataType.INTEGER);
                Integer curr_ammo = event.getItem().getItemMeta().getPersistentDataContainer().get(current_ammo, PersistentDataType.INTEGER);
                if (canShootPers == 1 && isReloadingPers == 0) {
                    if (curr_ammo >= 1) {
                        Arrow arrow = player.launchProjectile(Arrow.class);
                        ProjectileSource shooter = arrow.getShooter();
                        ItemMeta meta = event.getItem().getItemMeta();
                        Vector direction = player.getLocation().getDirection().normalize();
                        Location partikelLocation = player.getLocation().clone().add(direction.multiply(2));
                        partikelLocation.setY(partikelLocation.getY() + 1); // Die Y-Koordinate um 1 erhöhen
                        arrow.getWorld().spawnParticle(Particle.SMOKE_NORMAL, partikelLocation, 3, 0, 0, 0, 0.1);

                        if (shooter instanceof Player) {
                            arrow.setVelocity(arrow.getVelocity().multiply(weaponData.getArrowVelocity()));
                            arrow.setShooter(shooter);
                            arrow.setDamage(weaponData.getDamage());
                            arrow.setGravity(false);
                        }
                        Location location = player.getLocation();
                        for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
                            Location playerLocation = nearbyPlayer.getLocation();
                            double distance = location.distance(playerLocation);
                            if (distance <= 20) {
                                float volume = (float) (1.0 - (distance / 20.0));
                                nearbyPlayer.playSound(location, weaponData.getWeaponSound(), SoundCategory.MASTER, volume, 1.0f);
                            }
                        }
                        int newAmmo = curr_ammo - 1;
                        meta.getPersistentDataContainer().set(current_ammo, PersistentDataType.INTEGER, newAmmo);
                        meta.setLore(Arrays.asList("§eAirsoft-Waffe", "§8➥ §e" + newAmmo + "§8/§6" + weaponData.getMaxAmmo()));
                        String actionBarText = "§e" + newAmmo + "§8/§6" + weaponData.getMaxAmmo();
                        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));

                        meta.getPersistentDataContainer().set(canShoot, PersistentDataType.INTEGER, 0);
                        event.getItem().setItemMeta(meta);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                ItemMeta meta1 = event.getItem().getItemMeta();
                                if (meta1.getPersistentDataContainer().get(current_ammo, PersistentDataType.INTEGER) >= 1) {
                                    meta1.getPersistentDataContainer().set(canShoot, PersistentDataType.INTEGER, 1);
                                    event.getItem().setItemMeta(meta1);
                                } else {
                                    NamespacedKey type = new NamespacedKey(Main.plugin, "type");
                                    if (meta1.getPersistentDataContainer().get(type, PersistentDataType.STRING) == null) {
                                        if (ItemManager.getItem(player, weaponData.getAmmoItem()) >= 1) {
                                            reloadWeapon(player, event.getItem());
                                        } else {
                                            String actionBarText = "§cDu hast keine Magazine mehr!";
                                            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
                                        }
                                    }
                                }
                            }
                        }.runTaskLater(Main.getInstance(), (long) (weaponData.getShootDuration() * 2));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                arrow.remove();
                            }
                        }.runTaskLater(Main.getInstance(), 20 * 20);
                    } else {
                        reloadWeapon(event.getPlayer(), event.getItem());
                    }
                }
            } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                NamespacedKey isReloading = new NamespacedKey(Main.plugin, "isReloading");
                NamespacedKey canShoot = new NamespacedKey(Main.plugin, "canShoot");
                ItemMeta meta = event.getItem().getItemMeta();
                assert meta != null;
                meta.getPersistentDataContainer().set(isReloading, PersistentDataType.INTEGER, 0);
                meta.getPersistentDataContainer().set(canShoot, PersistentDataType.INTEGER, 1);
                event.getItem().setItemMeta(meta);
            }
        }
    }

    public static void reloadWeapon(Player player, ItemStack weapon) {
        WeaponData weaponData = weaponDataMap.get(weapon.getType());
        NamespacedKey current_ammo = new NamespacedKey(Main.plugin, "current_ammo");
        NamespacedKey isReloading = new NamespacedKey(Main.plugin, "isReloading");
        ItemMeta meta = weapon.getItemMeta();
        meta.getPersistentDataContainer().set(isReloading, PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(current_ammo, PersistentDataType.INTEGER, weaponData.getMaxAmmo());
        weapon.setItemMeta(meta);
        String actionBarText = "§7Lade " + weaponData.getName() + "§7 nach!";
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
        NamespacedKey type = new NamespacedKey(Main.plugin, "type");
        if (meta.getPersistentDataContainer().get(type, PersistentDataType.STRING) == null) {
            if (ItemManager.getItem(player, weaponData.getAmmoItem()) >= 1) {
                reload(player, weapon);
                ItemStack itemStack = new ItemStack(weaponData.getAmmoItem());
                itemStack.setAmount(1);
                player.getInventory().removeItem(itemStack);
            } else {
                String text = "§cDu hast keine Magazine mehr!";
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(text));
            }
        } else {
            reload(player, weapon);
        }
    }
    public static void reload(Player player, ItemStack weapon) {
        WeaponData weaponData = weaponDataMap.get(weapon.getType());
        NamespacedKey current_ammo = new NamespacedKey(Main.plugin, "current_ammo");
        NamespacedKey isReloading = new NamespacedKey(Main.plugin, "isReloading");
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemMeta meta1 = weapon.getItemMeta();
                meta1.getPersistentDataContainer().set(isReloading, PersistentDataType.INTEGER, 0);
                weapon.setItemMeta(meta1);
                String actionBarText = weaponData.getName() + "§7 wurde nachgeladen!";
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
            }
        }.runTaskLater(Main.getInstance(), (long) (weaponData.getReloadDuration() * 2));
    }
}
