package de.polo.core.manager;

import de.polo.core.player.entities.PlayerData;
import de.polo.core.Main;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.storage.*;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.RoleplayItem;
import de.polo.core.utils.player.ChatUtils;
import lombok.SneakyThrows;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WeaponManager implements Listener {
    public static final ConcurrentMap<Material, WeaponData> weaponDataMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<Integer, Weapon> weaponList = new ConcurrentHashMap<>();

    private final Utils utils;
    private final PlayerManager playerManager;
    public ConcurrentMap<UUID, LocalDateTime> weaponUsages = new ConcurrentHashMap<>();
    ConcurrentMap<Arrow, LocalDateTime> arrows = new ConcurrentHashMap<>();

    public WeaponManager(Utils utils, PlayerManager playerManager) {
        this.utils = utils;
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        try {
            loadWeapons();
            loadPlayerWeapons();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Arrow arrow : arrows.keySet()) {
                    if (Utils.getTime().isAfter(arrows.get(arrow))) {
                        arrow.remove();
                        arrows.remove(arrow);
                    }
                }
            }
        }.runTaskLater(Main.getInstance(), 20 * 20);
    }

    private void loadWeapons() throws SQLException {
        /*
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
            weaponData.setMeele(result.getBoolean("isMelee"));
            weaponDataMap.put(Material.valueOf(result.getString("material")), weaponData);
        }
        */
        Main.getInstance()
                .getCoreDatabase()
                .queryThreaded("SELECT * FROM weapons")
                .thenAcceptAsync(result -> {
                    try {
                        while (result.next()) {
                            WeaponData weaponData = new WeaponData();
                            weaponData.setId(result.resultSet().getInt("id"));
                            weaponData.setMaterial(Material.valueOf(result.resultSet().getString("material")));
                            weaponData.setName(result.resultSet().getString("name").replace("&", "§"));
                            weaponData.setMaxAmmo(result.resultSet().getInt("maxAmmo"));
                            weaponData.setReloadDuration(result.resultSet().getFloat("reloadDuration"));
                            weaponData.setDamage(result.resultSet().getFloat("damage"));
                            weaponData.setWeaponSound(Sound.valueOf(result.resultSet().getString("weaponSound")));
                            weaponData.setArrowVelocity(result.resultSet().getFloat("velocity"));
                            weaponData.setShootDuration(result.resultSet().getFloat("shootDuration"));
                            weaponData.setType(result.resultSet().getString("type"));
                            weaponData.setSoundPitch(result.resultSet().getFloat("soundPitch"));
                            weaponData.setKnockback(result.resultSet().getInt("knockback"));
                            weaponData.setMeele(result.resultSet().getBoolean("isMelee"));
                            weaponDataMap.put(Material.valueOf(result.resultSet().getString("material")), weaponData);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } finally {
                        result.close();
                    }
                });
    }

    @SneakyThrows
    private void loadPlayerWeapons() {
        /*
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM player_weapons");
        while (result.next()) {
            Weapon weapon = new Weapon();
            weapon.setAmmo(result.getInt("ammo"));
            weapon.setCurrentAmmo(result.getInt("current_ammo"));
            weapon.setWeaponType(WeaponType.valueOf(result.getString("weaponType")));
            weapon.setId(result.getInt("id"));
            weapon.setOwner(UUID.fromString(result.getString("uuid")));
            weapon.setType(de.polo.voidroleplay.utils.enums.Weapon.valueOf(result.getString("weapon")));
            weaponList.put(weapon.getId(), weapon);
        }
        */
        Main.getInstance()
                .getCoreDatabase()
                .queryThreaded("SELECT * FROM player_weapons")
                .thenAcceptAsync(result -> {
                    try {
                        while (result.next()) {
                            Weapon weapon = new Weapon();
                            weapon.setAmmo(result.resultSet().getInt("ammo"));
                            weapon.setCurrentAmmo(result.resultSet().getInt("current_ammo"));
                            weapon.setWeaponType(WeaponType.valueOf(result.resultSet().getString("weaponType")));
                            weapon.setId(result.resultSet().getInt("id"));
                            weapon.setOwner(UUID.fromString(result.resultSet().getString("uuid")));
                            weapon.setType(de.polo.core.utils.enums.Weapon.valueOf(result.resultSet().getString("weapon")));
                            weaponList.put(weapon.getId(), weapon);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } finally {
                        result.close();
                    }
                });
    }

    @SneakyThrows
    private void removeWeapon(Weapon weapon) {
        weaponList.remove(weapon.getId());
        Main.getInstance()
                .getCoreDatabase()
                .executeAsync("DELETE FROM weapons WHERE id = " + weapon.getId());
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
        return weaponList.get(id);
    }

    @EventHandler
    public void onWeaponUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        if (player.getGameMode().equals(GameMode.SPECTATOR) || player.isInsideVehicle()) return;
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.isCuffed() || playerData.isDead()) return;
        if (weaponUsages.get(player.getUniqueId()) != null) {
            if (Utils.getTime().isAfter(weaponUsages.get(player.getUniqueId()))) {
                weaponUsages.remove(player.getUniqueId());
            } else {
                utils.sendActionBar(player, "§cDu hast keine Kraft um die Waffe zu benutzen!");
                return;
            }
        }
        if (player.getEquipment().getItemInMainHand().isEmpty()) return;
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
                if (result != null && result.getHitEntity() instanceof Player hitPlayer) {
                    if (target == null || !target.equals(hitPlayer)) {
                        target = hitPlayer;
                    }
                }
                // Erzeuge den Partikel
                player.spawnParticle(Particle.FIREWORKS_SPARK, particleLocation, 1, 0.0, 0.0, 0.0, 0.0);
            }
            if (target == null || target == player) return;
            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat " + target.getName() + " getazert.");
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 9, 2, true, false)); // Slow für 6 Sekunden
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 9, -10, true, false)); // Jump für 6 Sekunden, -10 für eine geringere Sprunghöhe
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 9, 0, true, false)); // Blindness für 6 Sekunden
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 1, true, false));
            PlayerData targetData = playerManager.getPlayerData(target);
            if (targetData == null) return;
            if (targetData.isCuffed()) targetData.setCuffed(true);
        }

        Weapon gun = getWeaponFromItemStack(player.getEquipment().getItemInMainHand());
        if (gun == null) {
            return;
        }
        de.polo.core.utils.enums.Weapon weaponData = gun.getType();
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
        NamespacedKey id = new NamespacedKey(Main.getInstance(), "id");
        Integer weaponId = event.getItem().getItemMeta().getPersistentDataContainer().get(id, PersistentDataType.INTEGER);
        Weapon weapon = weaponList.get(weaponId);
        if (weapon == null) return;

        if (!Instant.now().isAfter(weapon.getShootCooldown())) {
            return;
        }
        if (weapon.isReloading()) {
            return;
        }
        if (weapon.getCurrentAmmo() < 1) {
            reloadWeapon(event.getPlayer(), event.getItem());
            return;
        }

        ItemMeta meta = event.getItem().getItemMeta();
        // Shooting logic
        if (weaponData == de.polo.core.utils.enums.Weapon.SHOTGUN) {
            double spread = 10.0;

            Vector direction = player.getEyeLocation().getDirection().normalize();

            for (int i = 0; i < 6; i++) {
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrows.put(arrow, Utils.getTime().plusSeconds(20));

                Vector spreadDirection = direction.clone();
                spreadDirection.add(new Vector(
                        (Math.random() - 0.5) * 2 * spread / 100,
                        (Math.random() - 0.5) * 2 * spread / 100,
                        (Math.random() - 0.5) * 2 * spread / 100
                )).normalize();

                arrow.setVelocity(spreadDirection.multiply(weaponData.getVelocity()));
                arrow.setShooter(player);
                arrow.setDamage(weaponData.getDamage());
                arrow.setGravity(false);
                arrow.setKnockbackStrength((int) weaponData.getKnockback());

                Location particleLocation = player.getEyeLocation().clone().add(spreadDirection.clone().multiply(1));
                player.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0.0, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.BLACK, 1));
            }
        } else {
            Arrow arrow = player.launchProjectile(Arrow.class);
            arrows.put(arrow, Utils.getTime().plusSeconds(20));
            ProjectileSource shooter = arrow.getShooter();
            Vector direction = player.getEyeLocation().getDirection().normalize();

            Location particleLocation = player.getEyeLocation().clone().add(direction.clone().multiply(1));
            player.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0.0, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.BLACK, 1));

            if (shooter instanceof Player) {
                arrow.setVelocity(direction.multiply(weaponData.getVelocity()));
                arrow.setShooter(shooter);
                arrow.setDamage(weaponData.getDamage());
                arrow.setGravity(false);
                arrow.setKnockbackStrength((int) weaponData.getKnockback());
            }
        }


        Location location = player.getLocation();
        /*for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
            Location playerLocation = nearbyPlayer.getLocation();
            double distance = location.distance(playerLocation);
            if (distance <= 20) {
                float volume = (float) (1.0 - (distance / 20.0));
                nearbyPlayer.playSound(location, weaponData.getWeaponSound(), SoundCategory.MASTER, volume, weaponData.getSoundPitch());
            }
        }*/
        Bukkit.getWorld("World").playSound(location, weaponData.getSound(), SoundCategory.MASTER, 1, weaponData.getSoundPitch());

        int newAmmo = weapon.getCurrentAmmo() - 1;
        weapon.setCurrentAmmo(newAmmo);
        meta.setLore(Arrays.asList("§8➥ §e" + newAmmo + "§8/§6" + weapon.getType().getMaxAmmo()));
        String actionBarText = "§e" + newAmmo + "§8/§6" + weapon.getType().getMaxAmmo();
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));

        event.getItem().setItemMeta(meta);

        long shootDuration = (long) weaponData.getShootDuration(); // In Sekunden
        long delayTicks = shootDuration * 2; // In Ticks
        long delayMillis = delayTicks * 30; // In Millisekunden

        Instant cooldownExpiration = Instant.now().plusMillis(delayMillis);
        weapon.setShootCooldown(cooldownExpiration);

        updateWeaponLore(weapon, event.getItem());
        if (weapon.getCurrentAmmo() < 1) {
            reloadWeapon(player, event.getItem());
        }
    }

    public void reloadWeapon(Player player, ItemStack weapon) {
        NamespacedKey idKey = new NamespacedKey(Main.getInstance(), "id");
        Integer id = weapon.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.INTEGER);
        Weapon w = weaponList.get(id);
        // ISSUE VRP-10000: Fix reload logic
        // TODO: Fix reload logic
        if (w == null) return;
        de.polo.core.utils.enums.Weapon weaponData = w.getType();
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
            w.setCurrentAmmo(w.getType().getMaxAmmo());
        } else {
            if (w.getAmmo() >= w.getType().getMaxAmmo()) {
                int dif = w.getType().getMaxAmmo() - w.getCurrentAmmo();
                w.setCurrentAmmo(w.getType().getMaxAmmo());
                w.setAmmo(w.getAmmo() - dif);
            } else {
                w.setCurrentAmmo(w.getType().getMaxAmmo() - w.getAmmo());
                w.setAmmo(0);
            }
        }
        w.setReloading(false);
        utils.sendActionBar(player, w.getType().getName() + "§7 wurde nachgeladen!");
        updateWeaponLore(w, weapon);
    }

    private void updateWeaponLore(Weapon weapon, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(Arrays.asList("§8➥ §e" + weapon.getCurrentAmmo() + "§8/§6" + weapon.getType().getMaxAmmo() + " §7(" + weapon.getAmmo() + "§7)"));
        stack.setItemMeta(meta);
    }

    private void giveWeapon(Player player, PlayerWeapon playerWeapon, Weapon weapon) {
        System.out.println("WAFFEE");
        ItemStack item = new ItemStack(playerWeapon.getWeapon().getMaterial());
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(playerWeapon.getWeapon().getName());

        NamespacedKey idKey = new NamespacedKey(Main.getInstance(), "id");
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.INTEGER, weapon.getId());

        meta.setLore(Arrays.asList("§8➥ §e" + weapon.getCurrentAmmo() + "§8/§6" + playerWeapon.getWeapon().getMaxAmmo()));
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
        weaponList.put(weapon.getId(), weapon);
    }

    public void giveWeapon(Player player, de.polo.core.utils.enums.Weapon weapon, WeaponType weaponType) {
        giveWeapon(player, weapon, weaponType, 0, 1);
    }

    public void giveWeapon(Player player, de.polo.core.utils.enums.Weapon weapon, WeaponType weaponType, int ammo) {
        giveWeapon(player, weapon, weaponType, ammo, 1);
    }

    public void giveWeapon(Player player, de.polo.core.utils.enums.Weapon weapon, WeaponType weaponType, int ammo, int wear) {
        Weapon w = new Weapon();
        w.setOwner(player.getUniqueId());
        w.setAmmo(ammo);
        w.setCurrentAmmo(0);
        w.setWeaponType(weaponType);
        w.setType(weapon);
        w.setReloading(false);

        if (weaponType == WeaponType.NORMAL) {
            PlayerWeapon playerWeapon = new PlayerWeapon(
                    weapon,
                    wear,
                    ammo,
                    weaponType
            );
            w.setPlayerWeapon(playerWeapon);
        }
        Main.getInstance().getCoreDatabase()
                .insertAndGetKeyAsync("INSERT INTO player_weapons (uuid, weapon, weaponType) VALUES (?, ?, ?)",
                        w.getOwner().toString(), weapon.name(), weaponType.name())
                .thenApply(key -> {
                    if (key.isPresent()) {
                        w.setId(key.get());
                        giveWeapon(player, new PlayerWeapon(
                                weapon,
                                wear,
                                ammo,
                                weaponType
                        ), w);
                    } else {
                        player.sendMessage(Prefix.ERROR + "Es gab einen Fehler beim erstellen der Waffe.");
                    }
                    return null;
                });
    }

    public void giveAmmo(Player player, Weapon weapon, int ammo) {
        ItemStack item = null;
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) continue;
            if (itemStack.getItemMeta().getDisplayName()
                    .equals(weapon.getType().getName())
                    && itemStack.getType()
                    == weapon.getType().getMaterial()) {
                item = itemStack;
            }
        }
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey ammoKey = new NamespacedKey(Main.getInstance(), "id");
        Integer id = meta.getPersistentDataContainer().get(ammoKey, PersistentDataType.INTEGER);
        Weapon w = weaponList.get(id);

        int newAmmo = weapon.getAmmo() + ammo;
        weapon.setAmmo(newAmmo);

        meta.setLore(Arrays.asList("§8➥ §e" + w.getCurrentAmmo() + "§8/§6" + newAmmo));
        item.setItemMeta(meta);
    }

    public void giveAmmo(Player player, de.polo.core.utils.enums.Weapon weapon, int amount) {
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            if (stack.getType() != weapon.getMaterial()) continue;
            Weapon w = getWeaponFromItemStack(stack);
            if (w == null) continue;
            giveAmmo(player, w, amount);
        }
    }

    public void giveAmmoToCabinet(PlayerWeapon playerWeapon, int ammo) {
        playerWeapon.setAmmo(playerWeapon.getAmmo() + ammo);
        playerWeapon.save();
    }

    public void takeOutWeapon(Player player, PlayerWeapon playerWeapon) {
        giveWeapon(player, playerWeapon.getWeapon(), WeaponType.NORMAL, 0);
        playerWeapon.setWear(playerWeapon.getWear() - 1);
        playerWeapon.save();
    }

    public boolean takeOutAmmo(Player player, PlayerWeapon playerWeapon, Weapon weapon, int ammo) {
        if (playerWeapon.getAmmo() < ammo) {
            return false;
        }
        giveAmmo(player, weapon, ammo);
        playerWeapon.setAmmo(playerWeapon.getAmmo() - ammo);
        playerWeapon.save();
        return true;
    }

    public void giveWeaponToCabinet(Player player, de.polo.core.utils.enums.Weapon weapon, int ammo, int wear) {
        PlayerData playerData = playerManager.getPlayerData(player);
        PlayerWeapon existingWeapon = playerData.getWeapons().stream().filter(x -> x.getWeaponType() == WeaponType.NORMAL && x.getWeapon() == weapon).findFirst().orElse(null);
        if (existingWeapon != null) {
            existingWeapon.setWear(existingWeapon.getWear() + wear);
            existingWeapon.setAmmo(existingWeapon.getAmmo() + ammo);
            existingWeapon.save();
            return;
        }
        PlayerWeapon playerWeapon = new PlayerWeapon(weapon, wear, ammo, WeaponType.NORMAL);
        addWeaponToGunCabinet(player, playerWeapon);
    }

    private void addWeaponToGunCabinet(Player player, PlayerWeapon playerWeapon) {
        PlayerData playerData = playerManager.getPlayerData(player);
        playerData.giveWeapon(playerWeapon);
        Main.getInstance()
                .getCoreDatabase()
                .insertAndGetKeyAsync(
                        "INSERT INTO player_gun_cabinet (uuid, weapon, wear, ammo) VALUES (?, ?, ?, ?)",
                        player.getUniqueId().toString(),
                        playerWeapon.getWeapon().name(),
                        playerWeapon.getWear(),
                        playerWeapon.getAmmo()
                )
                .thenApply(generatedKey -> {
                    generatedKey.ifPresent(playerWeapon::setId);
                    return null;
                });
    }
}
