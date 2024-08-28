package de.polo.voidroleplay.game.base.housing;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.RegisteredBlock;
import de.polo.voidroleplay.game.base.crypto.Miner;
import de.polo.voidroleplay.game.events.MinuteTickEvent;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Housing implements CommandExecutor, Listener {
    public static final Map<Integer, House> houseDataMap = new HashMap<>();
    private final PlayerManager playerManager;
    private final BlockManager blockManager;
    private final LocationManager locationManager;

    public Housing(PlayerManager playerManager, BlockManager blockManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.blockManager = blockManager;
        this.locationManager = locationManager;
        Main.registerCommand("houseaddon", this);
        try {
            loadHousing();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadHousing() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM housing");
        while (locs.next()) {
            House houseData = new House(locs.getInt("maxServer"), locs.getInt("maxMiner"));
            houseData.setId(locs.getInt(1));
            houseData.setOwner(locs.getString(2));
            houseData.setNumber(locs.getInt(3));
            houseData.setPrice(locs.getInt(4));
            houseData.setTotalMoney(locs.getInt(7));
            houseData.setMiner(locs.getInt("miner"));
            houseData.setServer(locs.getInt("server"));
            houseData.setServerRoom(locs.getBoolean("hasServerRoom"));

            JSONObject object = new JSONObject(locs.getString(5));
            HashMap<String, Integer> map = new HashMap<>();
            for (String key : object.keySet()) {
                int value = (int) object.get(key);
                map.put(key, value);
            }
            houseData.setRenter(map);
            houseData.setMoney(locs.getInt(6));
            houseDataMap.put(locs.getInt(3), houseData);
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
            Statement statement = Main.getInstance().mySQL.getStatement();
            JSONObject object = new JSONObject(houseData.getRenter());
            statement.executeUpdate("UPDATE `housing` SET `renter` = '" + object + "' WHERE `number` = " + number);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<House> getAccessedHousing(Player player) {
        List<House> access = new ArrayList<>();
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

    public Collection<House> getHouses(Player player) {
        List<House> access = new ArrayList<>();
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
        Statement statement = Main.getInstance().mySQL.getStatement();
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
            System.out.println(block.getType().toString());
            if (block.getType().toString().contains("SIGN")) {
                Sign sign = (Sign) block.getState();
                try {
                    House houseData = Housing.houseDataMap.get(house);
                    System.out.println("HOUSE: " + houseData.getOwner());
                    houseData.setOwner(null);
                    sign.setLine(2, "§aZu Verkaufen");
                    sign.update();
                    Statement statement = Main.getInstance().mySQL.getStatement();
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
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.CHEST, 1, 0, "§6Haus " + house.getNumber(), Arrays.asList("§8 ➥ §eServer-Raum§8: " + (house.isServerRoom() ? "§cNein" : "§aJa"), "§8 ➥ §eCrypto-Miner§8: §7" + house.getMiner() + "§8/§7" + house.getMaxMiner(), "§8 ➥ §eServer§8: §7" + house.getServer() + "§8/§7" + house.getMaxServer(), "§8 ➥ §eMieterslots§8: §7" + house.getTotalSlots()))) {
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
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.CHEST, 1, 0, "§6Server-Raum" + (house.isServerRoom() ? " §8[§cGekauft§8]" : ""), "§8 ➥ §a" + Utils.toDecimalFormat(serverRoomPrice) + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (house.isServerRoom()) return;
                player.closeInventory();
                if (playerData.getBargeld() < serverRoomPrice) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei.");
                    return;
                }
                playerData.removeMoney(serverRoomPrice, "Kauf Server-Raum (" + house.getNumber() + ")");
                house.setServerRoom(true);
                house.save();
            }
        });
        int minerPrice = ServerManager.getPayout("miner");
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§6Crypto-Miner" + (house.getMiner() >= house.getMaxMiner() ? " §8[§cKein Platz§8]" : ""), "§8 ➥ §a" + Utils.toDecimalFormat(minerPrice) + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (house.isServerRoom()) {
                    player.sendMessage(Prefix.ERROR + "Du hast keinen Server-Raum!");
                    return;
                }
                if (house.getMiner() >= house.getMaxMiner()) return;
                if (playerData.getBargeld() < minerPrice) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei.");
                    return;
                }
                playerData.removeMoney(serverRoomPrice, "Kauf Miner (" + house.getNumber() + ")");
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
                return;/*
                if (house.isServerRoom()) {
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
    }

    public void openHouseServerRoom(Player player, House house) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Server-Raum (Haus " + house.getNumber() + ")");
        int active = 0;
        float kWh = 0;
        for (Miner miner : house.getActiveMiner()) {
            if (miner.isActive()) active++;
            kWh += miner.getKWh();
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
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§eMiner", Arrays.asList("§8 ➥ §aAktiv§8: " + (miner.isActive() ? "§aAktiv" : "§cInaktiv"), "§8 ➥ §bVerbrauch§8: §7" + miner.getKWh() + " kWh", "§8 ➥ §eCoins§8: §7" + miner.getCoins()))) {
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

    @EventHandler
    public void onMinute(MinuteTickEvent event) {
        if (event.getMinute() % 90 != 0) return;
        doCryptoTick();
    }
}
