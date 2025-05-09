package de.polo.core.utils.player;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.location.services.LocationService;
import de.polo.core.location.services.NavigationService;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.storage.Corpse;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.RoleplayItem;
import de.polo.core.utils.enums.Weapon;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static de.polo.core.Main.*;

public class DeathUtils {
    private final HashMap<String, Boolean> deathPlayer = new HashMap<>();
    private final HashMap<String, Item> deathSkulls = new HashMap<>();

    private final List<Corpse> corpses = new ObjectArrayList<>();

    private final PlayerManager playerManager;

    public DeathUtils(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public static boolean isDead(Player player) throws SQLException {
        Statement statement = Main.getInstance().coreDatabase.getStatement();
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
                Statement statement = Main.getInstance().coreDatabase.getStatement();
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
        if (playerData.getVariable("inventory::base") != null) {
            player.getInventory().setContents(playerData.getVariable("inventory::base"));
        }
        AdminService adminService = VoidAPI.getService(AdminService.class);
        playerData.setCanInteract(false);
        playerData.setDead(false);
        playerData.setCuffed(false);
        playerData.setDeathTime(300);
        deathPlayer.remove(player.getUniqueId().toString());
        adminService.sendMessage(player.getName() + " wurde wiederbelebt.", null);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setFlySpeed(0.1F);
        if (player.isSleeping()) player.wakeup(true);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        //player.setGameMode(GameMode.SURVIVAL);
        if (playerData.getDeathLocation() != null && !playerData.isJailed())
            player.teleport(playerData.getDeathLocation());
        playerData.setDeathLocation(null);
        playerData.setHitmanDead(false);
        playerData.setStabilized(false);
        if (playerData.isJailed()) {
            LocationService locationService = VoidAPI.getService(LocationService.class);
            locationService.useLocation(player, "gefaengnis");
        }
        try {
            Statement statement = Main.getInstance().coreDatabase.getStatement();
            assert statement != null;
            String uuid = player.getUniqueId().toString();
            statement.executeUpdate("UPDATE `players` SET `isDead` = false, isStabilized = false, isHitmanDead = false, `deathTime` = 300 WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (deathSkulls.get(player.getUniqueId().toString()) != null) {
            Item skull = deathSkulls.get(player.getUniqueId().toString());
            skull.remove();
            deathSkulls.remove(player.getUniqueId().toString());
        }
        for (Player players : Bukkit.getOnlinePlayers()) {
            players.showPlayer(Main.getInstance(), player);
        }
        playerManager.setPlayerMove(player, true);
        if (playerData.getVariable("gangwar") != null) {
            utils.gangwarUtils.respawnPlayer(player);
            return;
        }
        if (effects) {
            weaponManager.weaponUsages.put(player.getUniqueId(), Utils.getTime().plusMinutes(3));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 6, 2, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 6, -10, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 6, 0, true, false));
        }
        if (playerData.isJailed()) removeWeapons(player);
    }

    private void removeWeapons(Player player) {
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            for (Weapon weaponData : Weapon.values()) {
                if (weaponData.getMaterial() == null) continue;
                if (weaponData.getMaterial().equals(stack.getType())) {
                    player.getInventory().removeItem(stack);
                }
            }
        }
    }

    public void despawnPlayer(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("inventory::base", null);
        deathPlayer.remove(player.getUniqueId().toString());
        player.setGameMode(GameMode.SURVIVAL);
        playerData.setDeathTime(300);
        playerData.setDead(false);
        if (!playerData.isFFADead() && playerData.getVariable("gangwar") != null) playerData.setFFADead(playerData.getVariable("ffa") != null);
        playerData.removeMoney(playerData.getBargeld(), "Despawn");
        if (playerData.isFFADead()) {
            gamePlay.getFfa().respawnPlayer(player);
            playerData.setFFADead(false);
            return;
        }
        try {
            Statement statement = Main.getInstance().coreDatabase.getStatement();
            assert statement != null;
            String uuid = player.getUniqueId().toString();
            statement.executeUpdate("UPDATE `players` SET `isDead` = false, `deathTime` = 300 WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (playerData.getVariable("gangwar") != null) {
            utils.gangwarUtils.respawnPlayer(player);
        } else {
            spawnCorpse(player);
            int despawnPrice = (int) (playerData.getBank() * 0.01);
            switch (playerData.getHealthInsurance()) {
                case BASIC ->
                        player.sendMessage(Component.text("§8[§cKrankenhaus§8]§7 Dir wurden " + Utils.toDecimalFormat(despawnPrice) + "$ Krankenhauskosten angerechnet."));
                case PLUS -> {
                    player.sendMessage(Component.text("§8[§cKrankenhaus§8]§7 Dir wurden " + Utils.toDecimalFormat(despawnPrice) + "$ Krankenhauskosten angerechnet, 50% davon übernimmt deine Krankenkasse."));
                    despawnPrice = despawnPrice / 2;
                }
                case FULL -> {
                    player.sendMessage(Component.text("§8[§cKrankenhaus§8]§7 Dir wurden " + Utils.toDecimalFormat(despawnPrice) + "$ Krankenhauskosten angerechnet, deine Krankenkasse kommt jedoch dafür auf."));
                    despawnPrice = 0;
                }
            }
            playerData.removeBankMoney(despawnPrice, "Krankenhauskosten");
            LocationService locationService = VoidAPI.getService(LocationService.class);
            if (playerData.getSpawn() == null) {
                locationService.useLocation(player, "Krankenhaus");
            } else {
                try {
                    int id = Integer.parseInt(playerData.getSpawn());
                    for (RegisteredBlock registeredBlock : blockManager.getBlocks()) {
                        if (registeredBlock.getInfo() == null) continue;
                        if (registeredBlock.getInfoValue() == null) continue;
                        if (registeredBlock.getInfo().equalsIgnoreCase("house")) {
                            if (Integer.parseInt(registeredBlock.getInfoValue()) == id) {
                                player.teleport(registeredBlock.getLocation());
                            }
                        }
                    }
                } catch (Exception e) {
                    locationService.useLocation(player, playerData.getSpawn());
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
        if (playerData.isJailed()) {
            LocationService locationService = VoidAPI.getService(LocationService.class);
            locationService.useLocation(player, "gefaengnis");
            removeWeapons(player);
        }
    }

    private void spawnCorpse(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        ItemStack skull = new ItemStack(Material.WITHER_SKELETON_SKULL);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§8Leiche");
        skull.setItemMeta(meta);
        Item item = playerData.getDeathLocation().getWorld().dropItemNaturally(playerData.getDeathLocation(), skull);
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
        NavigationService navigationService = VoidAPI.getService(NavigationService.class);
        corpse.getSkull().remove();
        playerData.setVariable("job::corpse::pickedup", true);
        navigationService.createNaviByLocation(player, "undertaker");
        player.sendMessage(Prefix.MAIN + "Du hast eine Leiche aufgesammelt, begib dich nun zum Bestatter.");
    }

    public void removeCorpse(Corpse corpse) {
        corpses.remove(corpse);
    }

    public Collection<Corpse> getCorbses() {
        return corpses;
    }
}
