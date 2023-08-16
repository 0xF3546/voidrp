package de.polo.metropiacity.playerUtils;

import de.polo.metropiacity.dataStorage.*;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.*;
import de.polo.metropiacity.utils.events.SubmitChatEvent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class FFAUtils implements CommandExecutor, Listener {
    public static final Map<Integer, FFALobbyData> FFAlobbyDataMap = new HashMap<>();
    public static final Map<String, FFASpawnPoints> FFAspawnpointDataMap = new HashMap<>();

    public static void loadFFALobbys() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet lobby = statement.executeQuery("SELECT * FROM `ffa_lobbys`");
        while (lobby.next()) {
            FFALobbyData ffaLobbyData = new FFALobbyData();
            ffaLobbyData.setId(lobby.getInt(1));
            ffaLobbyData.setName(lobby.getString(2));
            ffaLobbyData.setDisplayname(lobby.getString(3));
            ffaLobbyData.setMaxPlayer(lobby.getInt(4));
            ffaLobbyData.setPlayers(0);
            FFAlobbyDataMap.put(lobby.getInt(1), ffaLobbyData);
        }

        ResultSet result = statement.executeQuery("SELECT * FROM ffa_spawnpoints");
        while (result.next()) {
            FFASpawnPoints ffaSpawnPoints = new FFASpawnPoints();
            ffaSpawnPoints.setId(result.getInt(1));
            ffaSpawnPoints.setLobby_type(result.getString(2));
            ffaSpawnPoints.setX(result.getInt(3));
            ffaSpawnPoints.setY(result.getInt(4));
            ffaSpawnPoints.setZ(result.getInt(5));
            ffaSpawnPoints.setWelt(Bukkit.getWorld(result.getString(6)));
            ffaSpawnPoints.setYaw(result.getFloat(7));
            ffaSpawnPoints.setPitch(result.getFloat(8));
            FFAspawnpointDataMap.put(result.getString(2), ffaSpawnPoints);
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("join")) {
                if (PlayerManager.playerDataMap.get(player.getUniqueId().toString()).getVariable("current_lobby") == null) {
                    if (LocationManager.getDistanceBetweenCoords(player, "ffa") < 5) {
                        openFFAMenu(player, 1);
                    } else {
                        player.sendMessage(Main.error + "Du bist nicht in der nähe der FFA-Arena!");
                    }
                } else {
                    player.sendMessage(Main.error + "Du bist bereits in einem FFA.");
                }
            } else if (args[0].equalsIgnoreCase("leave")) {
                if (PlayerManager.playerDataMap.get(player.getUniqueId().toString()).getVariable("current_lobby") != null) {
                    player.sendMessage("§8[§6FFA§8]§a Du hast die FFA-Arena verlassen.");
                    leaveFFA(player);
                } else {
                    player.sendMessage(Main.error + "Du bist nicht in FFA.");
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /ffa [join/leave]");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /ffa [join/leave]");
        }
        return false;
    }

    public static void openFFAMenu(Player player, int page) {
        if (page <= 0) return;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setVariable("current_inventory", "ffa_menu");
        playerData.setIntVariable("current_page", page);
        Inventory inv = Bukkit.createInventory(player, 27, "§8» §6FFA-Lobbys §8- §6Seite§8:§7 " + page);
        int i = 0;
        int j = 0;
        for (FFALobbyData lobbyData : FFAlobbyDataMap.values()) {
            if (i >= (18 * (page - 1)) && i < (18 * page)) {
                int slotIndex = i % 9;
                j++;
                if (j > 9) {
                    slotIndex += 9;
                }

                if (lobbyData.getPlayers() < lobbyData.getMaxPlayer()) {
                    inv.setItem(slotIndex, ItemManager.createItem(Material.LIME_DYE, 1, 0, lobbyData.getDisplayname().replace("&", "§"), "§8 ➥ §eSpieler§8:§7 " + lobbyData.getPlayers() + "/" + lobbyData.getMaxPlayer()));
                    ItemMeta meta = inv.getItem(slotIndex).getItemMeta();
                    meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER, lobbyData.getId());
                    inv.getItem(slotIndex).setItemMeta(meta);
                } else {
                    inv.setItem(slotIndex, ItemManager.createItem(Material.GRAY_DYE, 1, 0, lobbyData.getDisplayname().replace("&", "§"), "§8 ➥ §eSpieler§8:§7 " + lobbyData.getPlayers() + "/" + lobbyData.getMaxPlayer()));
                }
            }
            i++;
        }
        inv.setItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite", null));
        inv.setItem(22, ItemManager.createItem(Material.EMERALD, 1, 0, "§aLobby erstellen", null));
        inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite", null));
        player.openInventory(inv);
    }

    public static void joinLobby(Player player, int id) {
        FFALobbyData lobbyData = FFAlobbyDataMap.get(id);
        if (lobbyData.getPlayers() < lobbyData.getMaxPlayer()) {
            player.closeInventory();
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            playerData.setIntVariable("current_lobby", id);
            playerData.setVariable("current_lobby", lobbyData.getName());
            lobbyData.setPlayers(lobbyData.getPlayers() + 1);
            player.sendMessage("§8[§6FFA§8]§e Du betrittst: " + lobbyData.getDisplayname().replace("&", "§"));
            player.sendMessage("§8 ➥ §7Nutze §8/§effa leave §7um die Arena zu verlassen.");
            Weapons.giveWeaponToPlayer(player, Material.DIAMOND_HORSE_ARMOR, "FFA");
            useSpawn(player, id);
        } else {
            player.sendMessage("§8[§6FFA§8]§c Diese Lobby ist voll!");
        }
    }

    public static void leaveFFA(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        FFALobbyData lobbyData = FFAlobbyDataMap.get(playerData.getIntVariable("current_lobby"));
        lobbyData.setPlayers(lobbyData.getPlayers() - 1);
        LocationManager.useLocation(player, "ffa");
        playerData.setIntVariable("current_lobby", null);
        playerData.setVariable("current_lobby", null);
        for (ItemStack item : player.getInventory().getContents()) {
            for (WeaponData weaponData : Weapons.weaponDataMap.values()) {
                if (weaponData.getMaterial() != null && item != null) {
                    if (item.getType() == weaponData.getMaterial()) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING) != null) {
                            if (meta.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING) == "FFA") {
                                player.getInventory().remove(item);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void useSpawn(Player player, int ffa) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        List<FFASpawnPoints> keysWithIdOne = new ArrayList<>();
        for (FFASpawnPoints spawnPoints : FFAspawnpointDataMap.values()) {
            if (spawnPoints.getLobby_type().equals(playerData.getVariable("current_lobby"))) {
                keysWithIdOne.add(spawnPoints);
            }
        }

        Random random = new Random();
        int randomKeyIndex = random.nextInt(keysWithIdOne.size());
        FFASpawnPoints randomValue = keysWithIdOne.get(randomKeyIndex);
        player.teleport(new Location(randomValue.getWelt(), randomValue.getX(), randomValue.getY(), randomValue.getZ()));
    }

    public static void createLobby(Player player, int players, String password) {
        FFALobbyData ffaLobbyData = new FFALobbyData();
        ffaLobbyData.setId(FFAlobbyDataMap.size() + 1);
        ffaLobbyData.setMaxPlayer(players);
        ffaLobbyData.setName(player.getUniqueId().toString());
        ffaLobbyData.setDisplayname("§6" + player.getName() + "'s Lobby");
        if (password != null) ffaLobbyData.setPassword(password);
        FFAlobbyDataMap.put(FFAlobbyDataMap.size() + 1, ffaLobbyData);
        SoundManager.successSound(player);
        player.closeInventory();
    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) {
        Player player = event.getPlayer();
        System.out.println(event.getSubmitTo());
        if (event.getSubmitTo().equalsIgnoreCase("ffa")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            event.getPlayerData().setVariable("ffa_password", event.getMessage());
            event.end();
            event.getPlayerData().setVariable("current_inventory", "ffa_createlobby");
            Inventory inv = Bukkit.createInventory(player, 27, "§8 » §aLobby erstellen");
            inv.setItem(11, ItemManager.createItem(Material.PLAYER_HEAD, 1, 0, "§eMaximale Spieler", "Lädt..."));
            ItemMeta imeta = inv.getItem(11).getItemMeta();
            imeta.setLore(Arrays.asList("§8 ➥ §7[§6Linksklick§8]§e +1 Slot", "§8 ➥ §7[§6Rechtsklick§8]§e -1 Slot"));
            inv.getItem(11).setItemMeta(imeta);
            inv.setItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§aLobby", "Lädt..."));
            ItemMeta itemMeta = inv.getItem(13).getItemMeta();
            if (event.getPlayerData().getVariable("ffa_password") == null) {
                itemMeta.setLore(Arrays.asList("§8 ➥ §eMaximale Spieler§8:§7 " + event.getPlayerData().getIntVariable("ffa_maxplayer"), "§8 ➥ §ePasswort§8:§c Nicht vorhanden"));
            } else {
                itemMeta.setLore(Arrays.asList("§8 ➥ §eMaximale Spieler§8:§7 " + event.getPlayerData().getIntVariable("ffa_maxplayer"), "§8 ➥ §ePasswort§8:§a " + event.getPlayerData().getVariable("ffa_password")));
            }            inv.getItem(13).setItemMeta(itemMeta);
            inv.setItem(15, ItemManager.createItem(Material.CHEST, 1, 0, "§ePasswort setzen", null));
            inv.setItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück", null));
            inv.setItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aLobby erstellen", null));
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                }
            }
            player.openInventory(inv);
        }
        if (event.getSubmitTo().equalsIgnoreCase("ffa_joinpassword")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            FFALobbyData lobbyData = FFAlobbyDataMap.get(event.getPlayerData().getIntVariable("ffa_passwordlobby"));
            if (lobbyData.getPassword().equalsIgnoreCase(event.getMessage())) {
                joinLobby(player, event.getPlayerData().getIntVariable("ffa_passwordlobby"));
                event.getPlayerData().setIntVariable("ffa_passwordlobby", null);
                event.end();
            } else {
                player.sendMessage("§8[§6FFA§8]§c Das Passwort ist falsch.");
            }
        }
    }
}
