package de.polo.voidroleplay.utils.playerUtils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.Corpse;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.RegisteredBlock;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class DeathUtils {
    private final HashMap<String, Boolean> deathPlayer = new HashMap<>();
    private final HashMap<String, Item> deathSkulls = new HashMap<>();

    private final List<Corpse> corpses = new ArrayList<>();

    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final LocationManager locationManager;

    public DeathUtils(PlayerManager playerManager, AdminManager adminManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.locationManager = locationManager;
    }

    public static boolean isDead(Player player) throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        String uuid = player.getUniqueId().toString();
        ResultSet result = statement.executeQuery("SELECT `isDead` FROM `players` WHERE `uuid` = '" + uuid + "'");
        boolean res = false;
        if (result.next()) {
            res = result.getBoolean(1);
        }
        return res;
    }

    public Item getDeathSkull(String UUID) {
        return deathSkulls.get(UUID);
    }

    public void addDeathSkull(String UUID, Item item) {
        deathSkulls.put(UUID, item);
    }

    public void removeDeathSkull(String UUID) {
        deathSkulls.remove(UUID);
    }

    public void startDeathTimer(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!playerData.isDead()) {
            deathPlayer.put(player.getUniqueId().toString(), true);
            try {
                Statement statement = Main.getInstance().mySQL.getStatement();
                statement.executeUpdate("UPDATE `players` SET `isDead` = true WHERE `uuid` = '" + player.getUniqueId() + "'");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setHitmanDeath(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setDeathTime(playerData.getDeathTime() + 300);
        playerData.setHitmanDead(true);
    }

    public void setGangwarDeath(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setDeathTime(playerData.getDeathTime() - 180);
    }

    public void killPlayer(Player player) {
        player.setHealth(0);
    }

    public void revivePlayer(Player player, boolean effects) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setCanInteract(false);
        playerData.setDead(false);
        playerData.setCuffed(false);
        playerData.setDeathTime(300);
        deathPlayer.remove(player.getUniqueId().toString());
        adminManager.send_message(player.getName() + " wurde wiederbelebt.", null);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setFlySpeed(0.1F);
        if (player.isSleeping()) player.wakeup(true);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
        playerData.setHitmanDead(false);
        playerData.setStabilized(false);
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            assert statement != null;
            String uuid = player.getUniqueId().toString();
            statement.executeUpdate("UPDATE `players` SET `isDead` = false, isStabilized = false, isHitmanDead = false, `deathTime` = 300 WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (deathSkulls.get(player.getUniqueId().toString()) != null) {
            Item skull = deathSkulls.get(player.getUniqueId().toString());
            player.teleport(skull.getLocation());
            skull.remove();
            deathSkulls.remove(player.getUniqueId().toString());
        }
        for (Player players : Bukkit.getOnlinePlayers()) {
            players.showPlayer(Main.plugin, player);
        }
        playerManager.setPlayerMove(player, true);
        if (playerData.getVariable("gangwar") != null) {
            Main.getInstance().utils.gangwarUtils.respawnPlayer(player);
            return;
        }
        if (effects) {
            Main.getInstance().weaponManager.weaponUsages.put(player.getUniqueId(), Utils.getTime().plusMinutes(3));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 6, 2, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 6, -10, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 6, 0, true, false));
        }
    }

    public void despawnPlayer(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        deathPlayer.remove(player.getUniqueId().toString());
        player.setGameMode(GameMode.SURVIVAL);
        playerData.setDeathTime(300);
        playerData.setDead(false);
        playerData.setFFADead(playerData.getVariable("ffa") != null);
        if (playerData.isFFADead()) {
            Main.getInstance().gamePlay.getFfa().respawnPlayer(player);
        }
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            assert statement != null;
            String uuid = player.getUniqueId().toString();
            statement.executeUpdate("UPDATE `players` SET `isDead` = false, `deathTime` = 300 WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (playerData.isFFADead()) return;
        if (playerData.getVariable("gangwar") != null) {
            Main.getInstance().utils.gangwarUtils.respawnPlayer(player);
        } else {
            spawnCorpse(player);
            if (playerData.getSpawn() == null) {
                locationManager.useLocation(player, "Krankenhaus");
            } else {
                try {
                    int id = Integer.parseInt(playerData.getSpawn());
                    for (RegisteredBlock registeredBlock : Main.getInstance().blockManager.getBlocks()) {
                        if (registeredBlock.getInfo() == null) continue;
                        if (registeredBlock.getInfoValue() == null) continue;
                        if (registeredBlock.getInfo().equalsIgnoreCase("house")) {
                            if (Integer.parseInt(registeredBlock.getInfoValue()) == id) {
                                player.teleport(registeredBlock.getLocation());
                            }
                        }
                    }
                } catch (Exception e) {
                    locationManager.useLocation(player, playerData.getSpawn());
                }
            }
            player.sendMessage(Prefix.MAIN + "Du bist im Krankenhaus aufgewacht.");
            playerData.setDead(false);
            playerData.setDeathTime(0);
            player.getInventory().clear();
        }
        Item skull = deathSkulls.get(player.getUniqueId().toString());
        skull.remove();
        deathSkulls.remove(player.getUniqueId().toString());
        playerManager.setPlayerMove(player, true);
        playerData.save();
        ItemManager.addCustomItem(player, RoleplayItem.SMARTPHONE, 1);
    }

    private void spawnCorpse(Player player) {
        ItemStack skull = new ItemStack(Material.WITHER_SKELETON_SKULL);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§8Leiche");
        skull.setItemMeta(meta);
        Item item = player.getLocation().getWorld().dropItemNaturally(player.getLocation(), skull);
        Corpse corpse = new Corpse(item, player.getUniqueId(), Utils.getTime());
        corpses.add(corpse);
    }

    public void cleanUpCorpses() {
        for (Corpse corpse : corpses) {
            corpse.getSkull().remove();
        }
    }

    public Corpse getNearbyCorpse(Location location, int range) {
        Corpse nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Corpse corpse : corpses) {
            double distance = corpse.getSkull().getLocation().distance(location);

            if (distance > range) continue;

            if (nearest == null || distance < nearestDistance) {
                nearest = corpse;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    public void jobPickupCorpse(Corpse corpse, Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getVariable("job::corpse") == null || playerData.getVariable("job::corpse") != corpse) {
            player.sendMessage(Prefix.ERROR + "Diese Leiche gehört nicht zu deinem Job!");
            return;
        }
        corpse.getSkull().remove();
        playerData.setVariable("job::corpse::pickedup", true);
        Main.getInstance().utils.navigationManager.createNaviByLocation(player, "undertaker");
        player.sendMessage(Prefix.MAIN + "Du hast eine Leiche aufgesammelt, begib dich nun zum Bestatter.");
    }

    public void removeCorpse(Corpse corpse) {
        corpses.remove(corpse);
    }

    public Collection<Corpse> getCorbses() {
        return corpses;
    }
}
