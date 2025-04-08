package de.polo.core.game.base.housing;

import de.polo.core.Main;
import de.polo.core.location.services.impl.LocationManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.PlayerWeapon;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.storage.Weapon;
import de.polo.core.game.base.crypto.Miner;
import de.polo.core.game.base.extra.Storage;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.game.events.SubmitChatEvent;
import de.polo.core.manager.*;
import de.polo.core.utils.inventory.CustomItem;
import de.polo.core.utils.inventory.InventoryManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.housing.enums.HouseType;
import de.polo.core.utils.enums.RoleplayItem;
import de.polo.core.utils.enums.StorageType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class HouseManager implements CommandExecutor, Listener {
    public static final Map<Integer, House> houseDataMap = new HashMap<>();
    private final PlayerManager playerManager;
    private final BlockManager blockManager;
    private final LocationManager locationManager;

    public HouseManager(PlayerManager playerManager, BlockManager blockManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.blockManager = blockManager;
        this.locationManager = locationManager;
        Main.registerCommand("houseaddon", this);
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        try {
            loadHousing();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadHousing() throws SQLException {
        Statement statement = Main.getInstance().coreDatabase.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM housing");
        while (locs.next()) {
            House houseData = new House(locs.getInt("number"), locs.getInt("maxServer"), locs.getInt("maxMiner"));
            houseData.setId(locs.getInt("id"));
            houseData.setOwner(locs.getString("owner"));
            houseData.setPrice(locs.getInt("price"));
            houseData.setTotalMoney(locs.getInt("money"));
            houseData.setMiner(locs.getInt("miner"));
            houseData.setServer(locs.getInt("server"));
            houseData.setServerRoom(locs.getBoolean("hasServerRoom"));
            houseData.setHouseType(HouseType.valueOf(locs.getString("type")));
            houseData.setMieterSlots(locs.getInt("mieterSlot"));

            JSONObject object = new JSONObject(locs.getString(5));
            HashMap<String, Integer> map = new HashMap<>();
            for (String key : object.keySet()) {
                int value = (int) object.get(key);
                map.put(key, value);
            }
            houseData.setRenter(map);
            houseData.setMoney(locs.getInt("money"));
            houseDataMap.put(locs.getInt("number"), houseData);
        }
    }

    public House getHouse(int houseNumber) {
        return houseDataMap.get(houseNumber);
    }

    public void addHouse(House house) {
        houseDataMap.put(house.getNumber(), house);
    }

    public boolean isPlayerOwner(Player player, int number) {
        House houseData = houseDataMap.get(number);
        return player.getUniqueId().toString().equals(houseData.getOwner());
    }

    public boolean canPlayerInteract(Player player, int number) {
        House houseData = houseDataMap.get(number);
        if (!Objects.equals(houseData.getOwner(), player.getUniqueId().toString())) {
            System.out.println("spieler ist kein owner");
            System.out.println(houseData.getRenter().get(player.getUniqueId().toString()));
            return houseData.getRenter().get(player.getUniqueId().toString()) != null;
        } else {
            return true;
        }
    }

    public String getHouseAccessAsString(PlayerData playerData) {
        if (playerData == null || playerData.getUuid() == null) {
            return "";
        }

        StringBuilder returnVal = null;
        for (House houseData : houseDataMap.values()) {
            if (houseData != null && houseData.getOwner() != null) {
                String playerUuid = playerData.getUuid().toString();
                if (houseData.getOwner().equalsIgnoreCase(playerUuid) || (houseData.getRenter() != null && houseData.getRenter().get(playerUuid) != null)) {
                    if (returnVal == null) {
                        returnVal = new StringBuilder(String.valueOf(houseData.getNumber()));
                    } else {
                        returnVal.append(", ").append(houseData.getNumber());
                    }
                }
            }
        }
        if (returnVal == null) {
            return "";
        }
        return returnVal.toString();
    }

    public void updateRenter(int number) {
        House houseData = houseDataMap.get(number);
        try {
            Statement statement = Main.getInstance().coreDatabase.getStatement();
            JSONObject object = new JSONObject(houseData.getRenter());
            statement.executeUpdate("UPDATE `housing` SET `renter` = '" + object + "' WHERE `number` = " + number);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<House> getAccessedHousing(Player player) {
        List<House> access = new ObjectArrayList<>();
        for (House houseData : houseDataMap.values()) {
            if (!Objects.equals(houseData.getOwner(), player.getUniqueId().toString())) {
                if (houseData.getRenter().get(player.getUniqueId().toString()) != null) {
                    access.add(houseData);
                }
            } else {
                access.add(houseData);
            }
        }
        return access;
    }

    public House getNearestHouse(Location loc, int range) {
        int centerX = loc.getBlockX();
        int centerY = loc.getBlockY();
        int centerZ = loc.getBlockZ();
        World world = loc.getWorld();

        House nearestHouse = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int x = centerX - range; x <= centerX + range; x++) {
            for (int y = centerY - range; y <= centerY + range; y++) {
                for (int z = centerZ - range; z <= centerZ + range; z++) {
                    Location location = new Location(world, x, y, z);
                    Block block = location.getBlock();

                    if (block.getType().name().contains("SIGN")) {
                        Sign sign = (Sign) block.getState();
                        RegisteredBlock registeredBlock = blockManager.getBlockAtLocation(location);
                        if (registeredBlock == null) continue;
                        if (registeredBlock.getInfoValue() == null) continue;
                        if (!registeredBlock.getInfo().equalsIgnoreCase("house")) continue;

                        String houseIdString = sign.getLine(0);
                        try {
                            House houseData = houseDataMap.get(Integer.parseInt(registeredBlock.getInfoValue()));
                            if (houseData != null) {
                                double distance = loc.distance(location);
                                if (distance < nearestDistance) {
                                    nearestDistance = distance;
                                    nearestHouse = houseData;
                                }
                            }
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        }

        return nearestHouse;
    }

    public Collection<House> getHouses(Player player) {
        List<House> access = new ObjectArrayList<>();
        for (House houseData : houseDataMap.values()) {
            if (!Objects.equals(houseData.getOwner(), player.getUniqueId().toString())) continue;

            access.add(houseData);
        }
        return access;
    }

    @SneakyThrows
    public void addHausSlot(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        System.out.println("hausslot hinzugefügt");
        playerData.setHouseSlot(playerData.getHouseSlot() + 1);
        Statement statement = Main.getInstance().coreDatabase.getStatement();
        statement.executeUpdate("UPDATE `players` SET `houseSlot` = " + playerData.getHouseSlot() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
    }

    public boolean resetHouse(Player player, int house) {
        for (RegisteredBlock rBlock : blockManager.getBlocks()) {
            if (rBlock.getInfo() == null) continue;
            if (rBlock.getInfoValue() == null) continue;

            if (!rBlock.getInfo().equalsIgnoreCase("house")) continue;
            if (!rBlock.getInfoValue().equalsIgnoreCase(String.valueOf(house))) continue;

            System.out.println("Haus: " + house);
            System.out.println(rBlock.getInfo() + " - " + rBlock.getInfoValue());

            Block block = rBlock.getLocation().getBlock();
            System.out.println(block.getType());
            if (block.getType().toString().contains("SIGN")) {
                Sign sign = (Sign) block.getState();
                try {
                    House houseData = HouseManager.houseDataMap.get(house);
                    System.out.println("HOUSE: " + houseData.getOwner());
                    houseData.setOwner(null);
                    sign.setLine(2, "§aZu Verkaufen");
                    sign.update();
                    Statement statement = Main.getInstance().coreDatabase.getStatement();
                    statement.executeUpdate("UPDATE `housing` SET `owner` = null WHERE `number` = " + house);
                    return true;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return false;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;
        if (locationManager.getDistanceBetweenCoords(player, "houseaddon") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Hausaddon-Shops.");
            return false;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Hausaddon-Shop");
        int i = 0;
        for (House house : getHouses(player)) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.CHEST, 1, 0, "§6Haus " + house.getNumber(), Arrays.asList("§8 ➥ §eServer-Raum§8: " + (!house.isServerRoom() ? "§cNein" : "§aJa"), "§8 ➥ §eCrypto-Miner§8: §7" + house.getActiveMiner().size() + "§8/§7" + house.getMaxMiner(), "§8 ➥ §eServer§8: §7" + house.getServer() + "§8/§7" + house.getMaxServer(), "§8 ➥ §eMieterslots§8: §7" + house.getTotalSlots()))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openHouseAddonMenu(player, house);
                }
            });
            i++;
        }
        return false;
    }

    private void openHouseAddonMenu(Player player, House house) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Hausaddon-Shop");
        int serverRoomPrice = ServerManager.getPayout("serverroom");
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.IRON_BLOCK, 1, 0, "§6Server-Raum" + (house.isServerRoom() ? " §8[§cGekauft§8]" : ""), "§8 ➥ §a" + Utils.toDecimalFormat(serverRoomPrice) + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (house.isServerRoom()) return;
                player.closeInventory();
                if (playerData.getBargeld() < serverRoomPrice) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei.");
                    return;
                }
                player.sendMessage("§8[§6Hausaddon§8]§a Du hast das Addon \"Server-Raum\" gekauft.");
                playerData.removeMoney(serverRoomPrice, "Kauf Server-Raum (" + house.getNumber() + ")");
                house.setServerRoom(true);
                house.save();
            }
        });
        int minerPrice = ServerManager.getPayout("miner");
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§6Crypto-Miner" + (house.getMiner() >= house.getMaxMiner() ? " §8[§cKein Platz§8]" : ""), "§8 ➥ §a" + Utils.toDecimalFormat(minerPrice) + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!house.isServerRoom()) {
                    player.sendMessage(Prefix.ERROR + "Du hast keinen Server-Raum!");
                    return;
                }
                if (house.getActiveMiner().size() >= house.getMaxMiner()) return;
                if (playerData.getBargeld() < minerPrice) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei.");
                    return;
                }
                player.sendMessage("§8[§6Hausaddon§8]§a Du hast das Addon \"Crypto-Miner\" gekauft.");
                playerData.removeMoney(minerPrice, "Kauf Miner (" + house.getNumber() + ")");
                house.setMiner(house.getMiner() + 1);
                house.addMiner(new Miner());
                house.save();
            }
        });
        int serverPrice = ServerManager.getPayout("server");
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§6Server" + (house.getServer() >= house.getMaxServer() ? " §8[§cKein Platz§8]" : ""), "§8 ➥ §a" + Utils.toDecimalFormat(serverPrice) + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.sendMessage(Prefix.ERROR + "Aktuell haben wir keine Server zu verkaufen.");
                /*
                if (!house.isServerRoom()) {
                    player.sendMessage(Prefix.ERROR + "Du hast keinen Server-Raum!");
                    return;
                }
                if (house.getServer() >= house.getMaxServer()) return;
                if (playerData.getBargeld() < minerPrice) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei.");
                    return;
                }
                playerData.removeMoney(serverRoomPrice, "Kauf Server (" + house.getNumber() + ")");
                house.setServer(house.getServer() + 1);
                house.save();*/
            }
        });
        int mieterslot = ServerManager.getPayout("mieterslot");
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§6Mieterslot", "§8 ➥ §a" + Utils.toDecimalFormat(mieterslot) + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < mieterslot) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei.");
                    return;
                }
                player.sendMessage("§8[§6Hausaddon§8]§a Du hast das Addon \"Mieterslot\" gekauft.");
                playerData.removeMoney(mieterslot, "Kauf Mieterslot (" + house.getNumber() + ")");
                setMieterSlot(house.getNumber(), house.getMieterSlots() + 1);
            }
        });
    }

    public void openHouseServerRoom(Player player, House house) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Server-Raum (Haus " + house.getNumber() + ")");
        int active = 0;
        float kWh = 0;
        if (!house.getActiveMiner().isEmpty()) {
            for (Miner miner : house.getActiveMiner()) {
                if (miner.isActive()) active++;
                kWh += miner.getKWh();
            }
        }
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§eMiner", Arrays.asList("§8 ➥ §aAktiv§8: §7" + active + "§8/§7" + house.getMiner(), "§8 ➥ §bVerbrauch§8: §7" + kWh + " kWh"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCryptoRoom(player, house);
            }
        });

        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.CHEST, 1, 0, "§7Server")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
    }

    public void doCryptoTick() {
        for (House house : houseDataMap.values()) {
            if (!house.isServerRoom()) continue;
            if (house.getActiveMiner().isEmpty()) continue;
            for (Miner miner : house.getActiveMiner()) {
                miner.doTick();
            }
        }
    }

    private void openCryptoRoom(Player player, House house) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Server-Raum (Haus " + house.getNumber() + ") §8-§e Crypto");
        int i = 0;
        for (Miner miner : house.getActiveMiner()) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§eMiner", Arrays.asList("§8 ➥ §aStatus§8: " + (miner.isActive() ? "§aAktiv" : "§cInaktiv"), "§8 ➥ §bVerbrauch§8: §7" + miner.getKWh() + " kWh", "§8 ➥ §eCoins§8: §7" + miner.getCoins()))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCryptoMiner(player, house, miner);
                }
            });
            i++;
        }
    }

    private void openCryptoMiner(Player player, House house, Miner miner) {
        InventoryManager inventoryManager = new InventoryManager(player, 9, "§8 » §7Server-Raum (Haus " + house.getNumber() + ") §8-§e Miner " + miner.getId());
        inventoryManager.setItem(new CustomItem(3, ItemManager.createItem(Material.PAPER, 1, 0, miner.isActive() ? "§aAktiv" : "§cInaktiv")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                miner.setActive(!miner.isActive());
                miner.save();
                openCryptoMiner(player, house, miner);
            }
        });
        inventoryManager.setItem(new CustomItem(6, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + miner.getCoins() + " Coins")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerManager.getPlayerData(player).addCrypto(miner.getCoins(), "Ertrag Miner " + miner.getId(), false);
                miner.setCoins(0);
                miner.save();
                openCryptoMiner(player, house, miner);
            }
        });
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCryptoRoom(player, house);
            }
        });
    }

    public void openCookMenu(Player player, House house) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§bKochmenü");
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(RoleplayItem.CRYSTAL.getMaterial(), 1, 0, "§7Lager öffnen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                Storage s = Storage.getStorageByTypeAndPlayer(StorageType.CRYSTAL_LABORATORY, player, house.getNumber());
                if (s == null) {
                    s = new Storage(StorageType.CRYSTAL_LABORATORY);
                    s.setPlayer(player.getUniqueId().toString());
                    s.setHouseNumber(house.getNumber());
                    s.create();
                }
                s.open(player);
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(RoleplayItem.CRYSTAL.getMaterial(), 1, 0, house.isCookActive() ? "§cStoppen" : "§aStarten")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                house.setCookActive(!house.isCookActive());
                ItemStack item = event.getCurrentItem();
                if (item == null || item.getItemMeta() == null) return;
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(house.isCookActive() ? "§cStoppen" : "§aStarten");
                item.setItemMeta(meta);
            }
        });

    }

    public void openGunCabinet(Player player, House house) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§cWaffenschrank");
        int i = 0;
        for (PlayerWeapon playerWeapon : playerData.getWeapons()) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(playerWeapon.getWeapon().getMaterial(), 1, 0, playerWeapon.getWeapon().getName(), Arrays.asList("§8 ➥ §c" + playerWeapon.getAmmo() + " Schuss", "§8 ➥ §c" + playerWeapon.getWear() + " Verschleiss", "", "§8[§6Linksklick§8]§7 Waffe entnehmen", "§8[§6Rechtsklick§8]§7 Munition entnehmen"))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    if (event.isLeftClick()) {
                        for (ItemStack item : player.getInventory().getContents()) {
                            if (item == null) continue;
                            if (item.getType() == playerWeapon.getWeapon().getMaterial()
                                    && item.getItemMeta().getDisplayName().equalsIgnoreCase(playerWeapon.getWeapon().getName())) {
                                player.sendMessage(Prefix.ERROR + "Du hast diese Waffe bereits bei dir.");
                                return;
                            }
                        }
                        if (playerWeapon.getWeapon() == de.polo.core.utils.enums.Weapon.SHOTGUN && playerData.getPermlevel() < 20) {
                            player.sendMessage(Component.text(Prefix.ERROR + "Für diese Waffe benötigst du Premium!"));
                            return;
                        }
                        if (playerWeapon.getWear() < 1) {
                            player.sendMessage(Component.text(Prefix.ERROR + "Die Waffe hat nicht genug Verschleiss."));
                            return;
                        }
                        Main.getInstance().getWeaponManager().takeOutWeapon(player, playerWeapon);
                    } else {
                        playerData.setVariable("chatblock", "weaponammo");
                        playerData.setVariable("playerWeapon", playerWeapon);
                        player.sendMessage(Component.text(Prefix.MAIN + "Gib an wie viel Munition du entnehmen willst."));
                    }
                }
            });
            i++;
        }
    }

    public void openHouseTreasury(Player player, House house) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 9, "§6Hauskasse");
        inventoryManager.setItem(new CustomItem(3, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19", 1, 0, "§aEinzahlen", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                playerData.setVariable("chatblock", "house::putin");
                playerData.setVariable("house", house);
                player.sendMessage("§aGib den Betrag an, welchen du in die Hauskasse legen möchtest.");
            }
        });
        inventoryManager.setItem(new CustomItem(4, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§a" + Utils.toDecimalFormat(house.getMoney()) + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(5, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjhkNWEzOGQ2YmZjYTU5Nzg2NDE3MzM2M2QyODRhOGQzMjljYWFkOTAxOGM2MzgxYjFiNDI5OWI4YjhiOTExYyJ9fX0=", 1, 0, "§cAuszahlen", null)) {

            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                playerData.setVariable("chatblock", "house::putout");
                playerData.setVariable("house", house);
                player.sendMessage("§aGib den Betrag an, welchen du aus der Hauskasse enthnehmen möchtest.");
            }
        });
    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) {
        if (event.getSubmitTo().equalsIgnoreCase("house::putin")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            House house = event.getPlayerData().getVariable("house");
            try {
                int amount = Integer.parseInt(event.getMessage());
                if (event.getPlayerData().getBargeld() < amount) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "Du hast nicht genug Bargeld.");
                    return;
                }
                if (amount < 1) {
                    return;
                }
                if (house.getMoney() + amount > 15000) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "Du kannst nicht mehr als 15.000$ in der Hauskasse haben.");
                    return;
                }
                event.getPlayer().sendMessage(Prefix.MAIN + "Du hast " + Utils.toDecimalFormat(amount) + "$ in die Hauskasse eingezahlt.");
                house.addMoney(amount, "Einzahlung " + event.getPlayer().getName(), true);
                event.getPlayerData().removeMoney(amount, "Hauskasse" + house.getNumber());
                event.end();
            } catch (Exception e) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Die Anzahl muss numerisch sein.");
            }
        }
        if (event.getSubmitTo().equalsIgnoreCase("house::putout")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            House house = event.getPlayerData().getVariable("house");
            try {
                int amount = Integer.parseInt(event.getMessage());
                if (amount < 1) {
                    return;
                }
                if (house.getMoney() < amount) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "In der Hauskasse liegt nicht genug Geld.");
                    return;
                }
                event.getPlayer().sendMessage(Prefix.MAIN + "Du hast " + Utils.toDecimalFormat(amount) + "$ aus der Hauskasse ausgezahlt.");
                house.removeMoney(amount, "Auszahlung " + event.getPlayer().getName(), true);
                event.getPlayerData().addMoney(amount, "Hauskasse" + house.getNumber());
                event.end();
            } catch (Exception e) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Die Anzahl muss numerisch sein.");
            }
        }
        if (event.getSubmitTo().equalsIgnoreCase("weaponammo")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            try {
                int amount = Integer.parseInt(event.getMessage());
                PlayerWeapon playerWeapon = event.getPlayerData().getVariable("playerWeapon");
                for (ItemStack item : event.getPlayer().getInventory().getContents()) {
                    if (item == null) continue;
                    System.out.println(item.getItemMeta().displayName());
                    if (item.getType() == playerWeapon.getWeapon().getMaterial()
                            && item.getItemMeta().getDisplayName().equalsIgnoreCase(playerWeapon.getWeapon().getName())) {
                        Weapon weapon = Main.getInstance().getWeaponManager().getWeaponFromItemStack(item);
                        if (weapon == null) continue;
                        if (Main.getInstance().getWeaponManager().takeOutAmmo(event.getPlayer(), playerWeapon, weapon, amount)) {
                            event.getPlayer().sendMessage(Prefix.MAIN + "Du hast deine Waffe mit " + amount + " beladen.");
                        } else {
                            event.getPlayer().sendMessage(Prefix.MAIN + "Deine Waffe hat nicht genug Schuss.");
                        }
                        event.end();
                        return;
                    }
                }
                event.getPlayer().sendMessage(Prefix.ERROR + "Du hast diese Waffe nicht dabei.");
                event.end();
            } catch (Exception e) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Die Anzahl muss numerisch sein.");
                e.printStackTrace();
            }
        }
    }

    public void setMieterSlot(int houseNumber, int mieter) {
        House house = getHouse(houseNumber);
        if (house == null) return;
        house.setMieterSlots(mieter);
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE housing SET mieterSlot = ? WHERE number = ?", house.getMieterSlots(), house.getNumber());
    }

    @EventHandler
    public void onMinute(MinuteTickEvent event) {
        if (event.getMinute() % 90 != 0) return;
        doCryptoTick();
    }


}

