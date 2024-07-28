package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.Weapon;
import de.polo.voidroleplay.dataStorage.WeaponData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.WeaponType;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Weapons implements Listener {
    public static final Map<Material, WeaponData> weaponDataMap = new HashMap<>();

    private final HashMap<Integer, Weapon> weaponList = new HashMap<>();

    private final Utils utils;
    private final PlayerManager playerManager;

    public Weapons(Utils utils, PlayerManager playerManager) {
        this.utils = utils;
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        try {
            loadWeapons();
            loadPlayerWeapons();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadWeapons() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM weapons");
        while (result.next()) {
            WeaponData weaponData = new WeaponData();
            weaponData.setId(result.getInt("id"));
            weaponData.setMaterial(Material.valueOf(result.getString("material")));
            weaponData.setName(result.getString("name").replace("&", "§"));
            weaponData.setMaxAmmo(result.getInt("maxAmmo"));
            weaponData.setReloadDuration(result.getFloat("reloadDuration"));
            weaponData.setDamage(result.getFloat("damage"));
            weaponData.setWeaponSound(Sound.valueOf(result.getString("weaponSound")));
            weaponData.setArrowVelocity(result.getFloat("velocity"));
            weaponData.setShootDuration(result.getFloat("shootDuration"));
            weaponData.setType(result.getString("type"));
            weaponData.setSoundPitch(result.getFloat("soundPitch"));
            weaponData.setKnockback(result.getInt("knockback"));
            weaponDataMap.put(Material.valueOf(result.getString("material")), weaponData);
        }
    }

    @SneakyThrows
    private void loadPlayerWeapons() {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM player_weapons");
        while (result.next()) {
            Weapon weapon = new Weapon();
            weapon.setAmmo(result.getInt("ammo"));
            weapon.setCurrentAmmo(result.getInt("current_ammo"));
            weapon.setWeaponType(WeaponType.valueOf(result.getString("weaponType")));
            weapon.setId(result.getInt("id"));
            weapon.setOwner(UUID.fromString(result.getString("uuid")));
            for (WeaponData weaponData : weaponDataMap.values()) {
                if (weaponData.getId() == result.getInt("weapon")) {
                    weapon.setWeaponData(weaponData);
                }
            }
            weaponList.put(weapon.getId(), weapon);
        }
    }

    @SneakyThrows
    public void giveWeaponToPlayer(Player player, Material material, WeaponType type) {
        WeaponData weaponData = weaponDataMap.get(material);
        ItemStack item = new ItemStack(weaponData.getMaterial());
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(weaponData.getName());
        Weapon weapon = new Weapon();
        weapon.setOwner(player.getUniqueId());
        weapon.setWeaponData(weaponData);
        weapon.setWeaponType(type);
        weapon.setAmmo(0);
        weapon.setCurrentAmmo(0);
        weapon.setReloading(false);
        addWeapon(weapon);

        NamespacedKey idKey = new NamespacedKey(Main.getInstance(), "id");
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.INTEGER, weapon.getId());

        meta.setLore(Arrays.asList("§eAirsoft-Waffe", "§8➥ §e" + weapon.getCurrentAmmo() + "§8/§6" + weaponData.getMaxAmmo()));
        item.setItemMeta(meta);
        player.getInventory().addItem(item);

    }

    public void giveWeaponAmmoToPlayer(Player player, ItemStack weapon, Integer amount) {
        ItemMeta meta = weapon.getItemMeta();
        NamespacedKey ammoKey = new NamespacedKey(Main.plugin, "id");
        Integer id = meta.getPersistentDataContainer().get(ammoKey, PersistentDataType.INTEGER);
        Weapon w = weaponList.get(id);

        int newAmmo = w.getAmmo() + amount;
        w.setAmmo(newAmmo);

        meta.setLore(Arrays.asList("§eAirsoft-Waffe", "§8➥ §e" + w.getCurrentAmmo() + "§8/§6" + newAmmo));
        weapon.setItemMeta(meta);
    }


    @SneakyThrows
    public Integer addWeapon(Weapon weapon) {
        Statement statement = Main.getInstance().mySQL.getStatement();
        statement.execute("INSERT INTO player_weapons (uuid, weapon, weaponType) VALUES ('" + weapon.getOwner().toString() + "', " + weapon.getWeaponData().getId() + ", '" + weapon.getWeaponType().name() + "')", Statement.RETURN_GENERATED_KEYS);

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            weapon.setId(generatedKeys.getInt(1));
            weaponList.put(weapon.getId(), weapon);
            return generatedKeys.getInt(1);
        }

        statement.close();
        return null;
    }

    @SneakyThrows
    private void removeWeapon(Weapon weapon) {
        weaponList.remove(weapon);
        Statement statement = Main.getInstance().mySQL.getStatement();
        statement.execute("DELETE FROM player_weapons WHERE id = " + weapon.getId());
        statement.close();
    }

    @SneakyThrows
    public void removeWeapon(Player player, ItemStack stack) {
        player.getInventory().remove(stack);
        NamespacedKey idKey = new NamespacedKey(Main.getInstance(), "id");
        Integer id = stack.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.INTEGER);
        Weapon weapon = weaponList.get(id);
        removeWeapon(weapon);
    }

    public Weapon getWeaponFromItemStack(ItemStack stack) {
        NamespacedKey idKey = new NamespacedKey(Main.getInstance(), "id");
        Integer id = stack.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.INTEGER);
        Weapon weapon = weaponList.get(id);
        return weapon;
    }

    public WeaponData getWeaponData(Material material) {
        for (WeaponData weaponData : weaponDataMap.values()) {
            if (weaponData.getMaterial() == null) continue;
            if (weaponData.getMaterial().equals(material)) return weaponData;
        }
        return null;
    }

    public HashMap<Integer, Weapon> getWeapons() {
        return weaponList;
    }

    @EventHandler
    public void onWeaponUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.isCuffed()) return;
        if (playerData.isDead()) {
            return;
        }
        if (Objects.requireNonNull(player.getEquipment()).getItemInMainHand().equals(new ItemStack(Material.AIR))) return;
        if (player.getEquipment().getItemInMainHand().getType().equals(RoleplayItem.TAZER.getMaterial()) && player.getEquipment().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase(RoleplayItem.TAZER.getDisplayName())) {
            if (Main.getInstance().getCooldownManager().isOnCooldown(player, "tazer")) {
                utils.sendActionBar(player, "§cWarte noch " + Main.getInstance().getCooldownManager().getRemainingTime(player, "tazer") + " Sekunden.");
                return;
            }
            Main.getInstance().getCooldownManager().setCooldown(player, "tazer", 9);
            Vector direction = player.getEyeLocation().getDirection().normalize();
            Location particleLocation = player.getEyeLocation().clone();

            int particleCount = 30;
            double beamLength = 3.0;
            double stepSize = beamLength / particleCount;

            Player target = null;

            for (int i = 0; i < particleCount; i++) {
                particleLocation.add(direction.clone().multiply(stepSize));

                RayTraceResult result = player.getWorld().rayTraceEntities(particleLocation, particleLocation.getDirection(), 1);
                if (result != null && result.getHitEntity() instanceof Player) {
                    Player hitPlayer = (Player) result.getHitEntity();
                    if (target == null || !target.equals(hitPlayer)) {
                        target = hitPlayer;
                    }
                    System.out.println("Getroffener Spieler: " + hitPlayer.getName());
                }
                // Erzeuge den Partikel
                player.spawnParticle(Particle.FIREWORKS_SPARK, particleLocation, 1, 0.0, 0.0, 0.0, 0.0);
            }
            if (target == null || target == player) return;
            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + target.getName() + " getazert.");
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 6, 2, true, false)); // Slow für 6 Sekunden
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 6, -10, true, false)); // Jump für 6 Sekunden, -10 für eine geringere Sprunghöhe
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 6, 0, true, false)); // Blindness für 6 Sekunden
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 0, true, false));

        }

        WeaponData weaponData = weaponDataMap.get(player.getEquipment().getItemInMainHand().getType());
        if (weaponData == null) {
            return;
        }
        /*if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
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
        }*/
        NamespacedKey id = new NamespacedKey(Main.plugin, "id");
        Integer weaponId = event.getItem().getItemMeta().getPersistentDataContainer().get(id, PersistentDataType.INTEGER);
        Weapon weapon = weaponList.get(weaponId);
        if (weapon.isReloading() || !weapon.isCanShoot()) {
            return;
        }
        if (weapon.getCurrentAmmo() < 1) {
            reloadWeapon(event.getPlayer(), event.getItem());
            return;
        }
        Arrow arrow = player.launchProjectile(Arrow.class);
        ProjectileSource shooter = arrow.getShooter();
        ItemMeta meta = event.getItem().getItemMeta();
        Vector direction = player.getEyeLocation().getDirection().normalize();

        Location particleLocation = player.getEyeLocation().clone().add(direction.clone().multiply(1));
        player.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0.0, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.BLACK, 1));

        if (shooter instanceof Player) {
            arrow.setVelocity(direction.multiply(weaponData.getArrowVelocity()));
            arrow.setShooter(shooter);
            arrow.setDamage(weaponData.getDamage());
            arrow.setGravity(false);
            arrow.setKnockbackStrength(weaponData.getKnockback());
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
        int newAmmo = weapon.getCurrentAmmo() - 1;
        weapon.setCurrentAmmo(newAmmo);
        meta.setLore(Arrays.asList("§eAirsoft-Waffe", "§8➥ §e" + newAmmo + "§8/§6" + weapon.getWeaponData().getMaxAmmo()));
        String actionBarText = "§e" + newAmmo + "§8/§6" + weapon.getWeaponData().getMaxAmmo();
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));

        event.getItem().setItemMeta(meta);
        weapon.setCanShoot(false);
        updateWeaponLore(weapon, event.getItem());
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemMeta meta1 = event.getItem().getItemMeta();
                weapon.setCanShoot(true);
                if (weapon.getCurrentAmmo() < 1) {
                    reloadWeapon(player, event.getItem());
                }
                cancel();
            }
        }.runTaskLater(Main.getInstance(), (long) (weaponData.getShootDuration() * 2));
        new BukkitRunnable() {
            @Override
            public void run() {
                arrow.remove();
                cancel();
            }
        }.runTaskLater(Main.getInstance(), 20 * 20);
    }

    public void reloadWeapon(Player player, ItemStack weapon) {
        NamespacedKey idKey = new NamespacedKey(Main.getInstance(), "id");
        Integer id = weapon.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.INTEGER);
        Weapon w = weaponList.get(id);
        WeaponData weaponData = weaponDataMap.get(weapon.getType());
        w.setReloading(true);
        utils.sendActionBar(player, "§7Lade " + weaponData.getName() + "§7 nach!");
        if (w.getWeaponType() != WeaponType.NORMAL) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    reload(player, weapon, id);
                    cancel();
                }
            }.runTaskLater(Main.getInstance(), (long) (weaponData.getReloadDuration() * 2));
            return;
        }
        if (w.getAmmo() >= 1) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    reload(player, weapon, id);
                    cancel();
                }
            }.runTaskLater(Main.getInstance(), (long) (weaponData.getReloadDuration() * 2));
        } else {
            utils.sendActionBar(player, "§cDu hast keine Munition mehr!");
        }
    }

    public void reload(Player player, ItemStack weapon, Integer id) {
        Weapon w = weaponList.get(id);
        if (!w.getWeaponType().isNeedsAmmoToReload()) {
            w.setCurrentAmmo(w.getWeaponData().getMaxAmmo());
        } else {
            if (w.getAmmo() >= w.getWeaponData().getMaxAmmo()) {
                int dif = w.getWeaponData().getMaxAmmo() - w.getCurrentAmmo();
                w.setCurrentAmmo(w.getWeaponData().getMaxAmmo());
                w.setAmmo(w.getAmmo() - dif);
            } else {
                w.setCurrentAmmo(w.getWeaponData().getMaxAmmo() - w.getAmmo());
                w.setAmmo(0);
            }
        }
        w.setReloading(false);
        utils.sendActionBar(player, w.getWeaponData().getName() + "§7 wurde nachgeladen!");
        updateWeaponLore(w, weapon);
    }

    private void updateWeaponLore(Weapon weapon, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(Arrays.asList("§eAirsoft-Waffe", "§8➥ §e" + weapon.getCurrentAmmo() + "§8/§6" + weapon.getWeaponData().getMaxAmmo() + " §7(" + weapon.getAmmo() + "§7)"));
        stack.setItemMeta(meta);
    }
}
