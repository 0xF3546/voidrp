package de.polo.metropiacity.utils;

import de.polo.metropiacity.dataStorage.WeaponData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Weapons implements Listener {
    public static final Map<Material, WeaponData> weaponDataMap = new HashMap<>();

    public static void loadWeapons() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT `id`, `material`, `name`, `maxAmmo`, `reloadDuration`, `damage`, `weaponSound`, `velocity`, `shootDuration`, `type`, `soundPitch` FROM `weapons`");
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
            weaponData.setSoundPitch(result.getFloat(11));
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

        NamespacedKey ammoKey = new NamespacedKey(Main.plugin, "ammo");
        meta.getPersistentDataContainer().set(ammoKey, PersistentDataType.INTEGER, weaponData.getMaxAmmo());

        meta.setLore(Arrays.asList("§eAirsoft-Waffe", "§8➥ §e" + weaponData.getMaxAmmo() + "§8/§6" + weaponData.getMaxAmmo()));
        item.setItemMeta(meta);
        player.getInventory().addItem(item);

    }

    public static void giveWeaponAmmoToPlayer(Player player, ItemStack weapon, Integer amount) {
        ItemMeta meta = weapon.getItemMeta();
        NamespacedKey ammoKey = new NamespacedKey(Main.plugin, "ammo");
        int current_ammo = meta.getPersistentDataContainer().get(ammoKey, PersistentDataType.INTEGER);
        meta.getPersistentDataContainer().set(ammoKey, PersistentDataType.INTEGER, current_ammo += amount);
        int ammo = meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "current_ammo"), PersistentDataType.INTEGER);
        meta.setLore(Arrays.asList("§eAirsoft-Waffe", "§8➥ §e" + ammo + "§8/§6" + (current_ammo += amount)));
        weapon.setItemMeta(meta);
    }

    @EventHandler
    public void onWeaponUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        WeaponData weaponData = weaponDataMap.get(player.getEquipment().getItemInMainHand().getType());
        if (weaponData == null) {
            return;
        }
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                NamespacedKey isReloading = new NamespacedKey(Main.plugin, "isReloading");
                NamespacedKey canShoot = new NamespacedKey(Main.plugin, "canShoot");
                ItemMeta meta = event.getItem().getItemMeta();
                assert meta != null;
                if (meta.getPersistentDataContainer().get(isReloading, PersistentDataType.INTEGER) == 1) {
                    meta.getPersistentDataContainer().set(isReloading, PersistentDataType.INTEGER, 0);
                    meta.getPersistentDataContainer().set(canShoot, PersistentDataType.INTEGER, 1);
                    reload(player, player.getEquipment().getItemInMainHand());
                    //das event wird auch beim droppen einer waffe (wenn man in die luft schaut) getriggered
                }
                event.getItem().setItemMeta(meta);
            }
            return;
        }
        NamespacedKey current_ammo = new NamespacedKey(Main.plugin, "current_ammo");
        NamespacedKey canShoot = new NamespacedKey(Main.plugin, "canShoot");
        NamespacedKey isReloading = new NamespacedKey(Main.plugin, "isReloading");
        NamespacedKey ammoKey = new NamespacedKey(Main.plugin, "ammo");
        Integer isReloadingPers = event.getItem().getItemMeta().getPersistentDataContainer().get(isReloading, PersistentDataType.INTEGER);
        Integer canShootPers = event.getItem().getItemMeta().getPersistentDataContainer().get(canShoot, PersistentDataType.INTEGER);
        Integer curr_ammo = event.getItem().getItemMeta().getPersistentDataContainer().get(current_ammo, PersistentDataType.INTEGER);
        Integer ammo = event.getItem().getItemMeta().getPersistentDataContainer().get(ammoKey, PersistentDataType.INTEGER);
        if (canShootPers == 0 && isReloadingPers == 0) {
            return;
        }
        if (curr_ammo < 1) {
            reloadWeapon(event.getPlayer(), event.getItem());
            return;
        }
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
                nearbyPlayer.playSound(location, weaponData.getWeaponSound(), SoundCategory.MASTER, volume, weaponData.getSoundPitch());
            }
        }
        int newAmmo = curr_ammo - 1;
        meta.getPersistentDataContainer().set(current_ammo, PersistentDataType.INTEGER, newAmmo);
        meta.setLore(Arrays.asList("§eAirsoft-Waffe", "§8➥ §e" + newAmmo + "§8/§6" + ammo));
        String actionBarText = "§e" + newAmmo + "§8/§6" + meta.getPersistentDataContainer().get(ammoKey, PersistentDataType.INTEGER);
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
                    reloadWeapon(player, event.getItem());
                }
            }
        }.runTaskLater(Main.getInstance(), (long) (weaponData.getShootDuration() * 2));
        new BukkitRunnable() {
            @Override
            public void run() {
                arrow.remove();
            }
        }.runTaskLater(Main.getInstance(), 20 * 20);
    }

    public static void reloadWeapon(Player player, ItemStack weapon) {
        WeaponData weaponData = weaponDataMap.get(weapon.getType());
        NamespacedKey isReloading = new NamespacedKey(Main.plugin, "isReloading");
        ItemMeta meta = weapon.getItemMeta();
        meta.getPersistentDataContainer().set(isReloading, PersistentDataType.INTEGER, 1);
        weapon.setItemMeta(meta);
        Utils.sendActionBar(player, "§7Lade " + weaponData.getName() + "§7 nach!");
        NamespacedKey type = new NamespacedKey(Main.plugin, "type");
        if (!Objects.equals(meta.getPersistentDataContainer().get(type, PersistentDataType.STRING), "default")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    reload(player, weapon);
                }
            }.runTaskLater(Main.getInstance(), (long) (weaponData.getReloadDuration() * 2));
            return;
        }
        NamespacedKey ammoKey = new NamespacedKey(Main.plugin, "ammo");
        int ammo = meta.getPersistentDataContainer().get(ammoKey, PersistentDataType.INTEGER);
        if (ammo >= 1) {
            meta.getPersistentDataContainer().set(isReloading, PersistentDataType.INTEGER, 1);
            weapon.setItemMeta(meta);
            new BukkitRunnable() {
                @Override
                public void run() {
                    reload(player, weapon);
                }
            }.runTaskLater(Main.getInstance(), (long) (weaponData.getReloadDuration() * 2));
        } else {
            Utils.sendActionBar(player, "§cDu hast eine Munition mehr!");
        }
    }

    public static void reload(Player player, ItemStack weapon) {
        WeaponData weaponData = weaponDataMap.get(weapon.getType());
        NamespacedKey current_ammo = new NamespacedKey(Main.plugin, "current_ammo");
        NamespacedKey isReloading = new NamespacedKey(Main.plugin, "isReloading");
        NamespacedKey canShoot = new NamespacedKey(Main.plugin, "canShoot");
        NamespacedKey type = new NamespacedKey(Main.plugin, "type");
        ItemMeta meta = weapon.getItemMeta();
        if (meta.getPersistentDataContainer().get(isReloading, PersistentDataType.INTEGER) == 0) return;
        meta.getPersistentDataContainer().set(isReloading, PersistentDataType.INTEGER, 0);
        meta.getPersistentDataContainer().set(canShoot, PersistentDataType.INTEGER, 1);
        if (!Objects.equals(meta.getPersistentDataContainer().get(type, PersistentDataType.STRING), "default")) {
            meta.getPersistentDataContainer().set(current_ammo, PersistentDataType.INTEGER, weaponData.getMaxAmmo());
        } else {
            NamespacedKey ammoKey = new NamespacedKey(Main.plugin, "ammo");
            int ammo = meta.getPersistentDataContainer().get(ammoKey, PersistentDataType.INTEGER);
            if (ammo >= weaponData.getMaxAmmo()) {
                meta.getPersistentDataContainer().set(current_ammo, PersistentDataType.INTEGER, weaponData.getMaxAmmo());
                meta.getPersistentDataContainer().set(ammoKey, PersistentDataType.INTEGER, ammo - weaponData.getMaxAmmo());
            } else {
                meta.getPersistentDataContainer().set(current_ammo, PersistentDataType.INTEGER, weaponData.getMaxAmmo() - ammo);
                meta.getPersistentDataContainer().set(ammoKey, PersistentDataType.INTEGER, 0);
            }
            weapon.setItemMeta(meta);
        }
        weapon.setItemMeta(meta);
        Utils.sendActionBar(player, weaponData.getName() + "§7 wurde nachgeladen!");
    }
}
