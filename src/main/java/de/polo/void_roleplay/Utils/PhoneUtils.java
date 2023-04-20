package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.PlayerUtils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PhoneUtils implements Listener {
    public static HashMap<String, Boolean> phoneCallIsCreated = new HashMap<>();
    public static HashMap<String, String> phoneCallConnection = new HashMap<>();
    public static HashMap<String, Boolean> isInCallConnection = new HashMap<>();
    public static String error_nophone = "§6Handy §8 » §cDu hast kein Handy dabei.";
    public static String error_flightmode = "§6Handy §8 » §cDu bist im Flugmodus.";

    @EventHandler
    public void onPhoneUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.IRON_NUGGET && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            openPhone(player);
        }
    }

    public static void openPhone(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        Inventory inv = Bukkit.createInventory(player, 9, "§8» §eHandy");
        if (playerData.isFlightmode()) {
            inv.setItem(0, ItemManager.createItem(Material.GREEN_STAINED_GLASS_PANE, 1, 0, "§aFlugmodus abschalten", null));
        } else {
            inv.setItem(0, ItemManager.createItem(Material.RED_STAINED_GLASS_PANE, 1, 0, "§cFlugmodus einschalten", null));
        }
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i) == null) {
                inv .setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
            }
        }
        playerData.setVariable("current_inventory", "handy");
        playerData.setVariable("current_app", null);
        player.openInventory(inv);
    }

    public static boolean inPhoneCall(Player player) {
        return phoneCallIsCreated.get(player.getUniqueId().toString()) != null;
    }
    public static void createPhoneConnection(Player player, String reason) {
        phoneCallIsCreated.put(player.getUniqueId().toString(), true);
    }

    public static void deleteTicket(Player player) {
        if (inPhoneCall(player)) {
            phoneCallIsCreated.remove(player.getUniqueId().toString());
        }
    }

    public static void createTicketConnection(Player player, Player targetplayer) {
        phoneCallConnection.put(player.getUniqueId().toString(), targetplayer.getUniqueId().toString());
        phoneCallConnection.put(targetplayer.getUniqueId().toString(), player.getUniqueId().toString());
        isInCallConnection.put(player.getUniqueId().toString(), true);
        isInCallConnection.put(targetplayer.getUniqueId().toString(), true);
    }
    public static void deleteTicketConnection(Player player, Player targetplayer) {
        phoneCallConnection.remove(player.getUniqueId().toString());
        phoneCallConnection.remove(targetplayer.getUniqueId().toString());
        deleteTicket(targetplayer);
        isInCallConnection.remove(targetplayer.getUniqueId().toString());
        isInCallConnection.remove(player.getUniqueId().toString());
    }
    public static String getConnection(Player player) {
        return phoneCallConnection.get(player.getUniqueId().toString());
    }

    public static boolean isInConnection(Player player) {
        return isInCallConnection.get(player.getUniqueId().toString()) != null;
    }

    public static void addNumberToContacts(Player player, Player targetplayer) throws SQLException {
        String uuid = player.getUniqueId().toString();
        Statement statement = MySQL.getStatement();
        PlayerData playerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
        statement.executeQuery("INSERT INTO `phone_contacts` (`uuid`, `contact_name`, `contact_number`, `contact_uuid`) VALUES ('" + player.getUniqueId().toString() + "', '" + targetplayer.getName() + "', " + playerData.getNumber() + ", '" + targetplayer.getUniqueId().toString() + "')");
    }

    public static void callNumber(Player player, int number) throws SQLException {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getNumber() == number) {
                if (VertragUtil.setVertrag(player, players, "phonecall", players.getUniqueId().toString())) {
                    if (PhoneUtils.hasPhone(players)) {
                        ChatUtils.sendGrayMessageAtPlayer(players, players.getName() + "'s Handy klingelt...");
                        ChatUtils.sendGrayMessageAtPlayer(player, players.getName() + " wählt eine Nummer auf dem Handy.");
                        player.sendMessage("§6Handy §8» §eDu rufst §l" + number + "§e an.");
                        players.sendMessage("§6Handy §8» §eDu wirst von §l" + playerData.getNumber() + "§e angerufen.");
                        playerData.setVariable("calling", players.getUniqueId().toString());
                        VertragUtil.sendInfoMessage(players);
                        players.playSound(players.getLocation(), Sound.MUSIC_CREATIVE, 1, 0);
                    } else {
                        player.sendMessage(Main.error + "Die gewünschte Rufnummer ist zurzeit nicht erreichbar.");
                    }
                } else {
                    player.sendMessage(Main.error + "Dein Handy konnte keine Verbindung aufbauen. §o(Systemfehler)");
                }
            }
        }
    }

    public static void sendSMS(Player player, int number, StringBuilder message) {
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getNumber() == number) {
                if (PhoneUtils.hasPhone(players)) {
                    players.sendMessage("§6SMS §8» §e" + player.getName() + "§8: §7" + message);
                    player.sendMessage("§6SMS §8» §e" + player.getName() + "§8: §7" + message);
                    player.playSound(player.getLocation(), Sound.BLOCK_WEEPING_VINES_STEP, 1, 0);
                    players.playSound(players.getLocation(), Sound.BLOCK_WEEPING_VINES_STEP, 1, 0);
                } else {
                    player.sendMessage(Main.error + "§6Handy §8» §cAuto-Response§8:§7 Die SMS konnte zugestellt werden, jedoch nicht gelesen.");
                }
            }
        }
    }

    public static void acceptCall(Player player, String targetuuid) {
        player.stopSound(Sound.MUSIC_CREATIVE);
        if (PhoneUtils.hasPhone(player)) {
            Player targetplayer = Bukkit.getPlayer(UUID.fromString(targetuuid));
            assert targetplayer != null;
            if (PhoneUtils.hasPhone(targetplayer)) {
                targetplayer.sendMessage("§6Handy §8» §7" + player.getName() + " hat dein Anruf angenommen");
                targetplayer.sendMessage("§6Handy §8» §7Du hast den Anruf von " + player.getName() + " angenommen");
                isInCallConnection.put(targetuuid, true);
                isInCallConnection.put(player.getUniqueId().toString(), true);
                phoneCallConnection.put(player.getUniqueId().toString(), targetuuid);
                phoneCallConnection.put(targetuuid, player.getUniqueId().toString());
                player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1, 0);
                targetplayer.playSound(targetplayer.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1, 0);
            }
        } else {
            player.sendMessage(PhoneUtils.error_nophone);
        }
    }

    public static void denyCall(Player player, String targetuuid) {
        player.stopSound(Sound.MUSIC_CREATIVE);
        if (PhoneUtils.hasPhone(player)) {
            Player targetplayer = Bukkit.getPlayer(UUID.fromString(targetuuid));
            assert targetplayer != null;
            if (PhoneUtils.hasPhone(targetplayer)) {
                targetplayer.sendMessage("§6Handy §8» §7" + player.getName() + " hat dein Anruf abgelehnt");
                player.sendMessage("§6Handy §8» §7Du hast den Anruf von " + player.getName() + " abgelehnt");
                player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
                targetplayer.playSound(targetplayer.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
            }
        } else {
            player.sendMessage(PhoneUtils.error_nophone);
        }
    }

    public static void closeCall(Player player) {
        player.stopSound(Sound.MUSIC_CREATIVE);
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (isInCallConnection.get(player.getUniqueId().toString()) != null) {
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                if (Objects.equals(phoneCallConnection.get(player1.getUniqueId().toString()), player.getUniqueId().toString())) {
                    isInCallConnection.remove(player1.getUniqueId().toString());
                    isInCallConnection.remove(player.getUniqueId().toString());
                    phoneCallConnection.remove(player1.getUniqueId().toString());
                    phoneCallConnection.remove(player.getUniqueId().toString());
                    player.sendMessage("§6Handy §8»§7 Du hast aufgelegt.");
                    player1.sendMessage("§6Handy §8»§7 " + player.getName() + " hat aufgelegt.");
                    player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
                    player1.playSound(player1.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
                }
            }
        } else if (playerData.getVariable("calling") != null) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (playerData.getVariable("calling").equals(players.getUniqueId().toString())) {
                    VertragUtil.deleteVertrag(player);
                    VertragUtil.deleteVertrag(players);
                    player.sendMessage("§6Handy §8»§7 Du hast aufgelegt.");
                    players.sendMessage("§6Handy §8»§7§l " + playerData.getNumber() + "§7 hat aufgelegt.");
                }
            }
        } else {
            player.sendMessage("§6Handy §8»§7 Du bist in keinem Anruf.");
        }
    }

    public static boolean hasPhone(Player player) {
        Inventory inv = player.getInventory();
        Material phone = Material.IRON_NUGGET;
        boolean returnval = false;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() == phone) {
                returnval = true;
            }
        }
        return returnval;
    }

}
