package de.polo.metropiacity.PlayerUtils;

import de.polo.metropiacity.DataStorage.FactionData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.ItemManager;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.Events.SubmitChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.text.DecimalFormat;

public class BankingUtils implements Listener {
    public static void sendKontoauszug(Player player) {
        player.sendMessage(" ");
        player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §aBankguthaben§8:§7 " + PlayerManager.bank(player) + "$");
        player.sendMessage(" ");
    }

    public static void sendBankChangeReason(Player player, String reason) {
        player.sendMessage(" ");
        player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
        player.sendMessage(" ");
        player.sendMessage("§8 ➜ §3Kontoveränderung§8:§b " + reason);
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §aBankguthaben§8:§7 " + PlayerManager.bank(player) + "$");
        player.sendMessage(" ");
    }

    public static void openBankMenu(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setVariable("current_inventory", "atm");
        Inventory inv = Bukkit.createInventory(player, 45, "§8 » §aBankautomat");
        inv.setItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjhkNWEzOGQ2YmZjYTU5Nzg2NDE3MzM2M2QyODRhOGQzMjljYWFkOTAxOGM2MzgxYjFiNDI5OWI4YjhiOTExYyJ9fX0=", 1, 0, "§cAuszahlen", null));
        inv.setItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBmZmFkMzNkMjkzYjYxNzY1ZmM4NmFiNTU2MDJiOTU1YjllMWU3NTdhOGU4ODVkNTAyYjNkYmJhNTQyNTUxNyJ9fX0=", 1, 0, "§bKontostand", "§8 ➥ §a" + new DecimalFormat("#,###").format(playerData.getBank()) + "$"));
        inv.setItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19", 1, 0, "§aEinzahlen", null));
        inv.setItem(29, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNmZjkxZGM5OWQ1ODI4MDIzZWVkZjg3Mzc5OWQyNTUzNWRhZGU2NGEyZTE2YTNiNDk4YjQxMTNlYWZkNDk2NiJ9fX0=", 1, 0, "§cAlles auszahlen", null));
        inv.setItem(33, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTI1MGIzY2NlNzY2MzVlZjRjN2E4OGIyYzU5N2JkMjc0OTg2OGQ3OGY1YWZhNTY2MTU3YzI2MTJhZTQxMjAifX19", 1, 0, "§aAlles einzahlen", null));
        inv.setItem(31, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjc2ZGNiMjc0ZTJlMjMxNDlmNDcyNzgxNjA1YjdjNmY4Mzk5MzFhNGYxZDJlZGJkMWZmNTQ2M2FiN2M0MTI0NiJ9fX0=", 1, 0, "§7Geld überweisen", null));
        for (int i = 0; i < 45; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
        }
        if (playerData.getFaction() != null && !playerData.getFaction().equals("Zivilist")) {
            inv.setItem(44, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGVmMzU2YWQyYWE3YjE2NzhhZWNiODgyOTBlNWZhNWEzNDI3ZTVlNDU2ZmY0MmZiNTE1NjkwYzY3NTE3YjgifX19", 1, 0, "§aFraktionskonto", null));
        }
        player.openInventory(inv);
    }

    public static void openFactionBankMenu(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
        playerData.setVariable("current_inventory", "atm_frak");
        Inventory inv = Bukkit.createInventory(player, 45, "§8 » §" + factionData.getPrimaryColor() + "Fraktionskonto");
        inv.setItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBmZmFkMzNkMjkzYjYxNzY1ZmM4NmFiNTU2MDJiOTU1YjllMWU3NTdhOGU4ODVkNTAyYjNkYmJhNTQyNTUxNyJ9fX0=", 1, 0, "§bKontostand", "§8 ➥ §a" + new DecimalFormat("#,###").format(factionData.getBank()) + "$"));
        inv.setItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19", 1, 0, "§aEinzahlen", null));
        inv.setItem(33, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTI1MGIzY2NlNzY2MzVlZjRjN2E4OGIyYzU5N2JkMjc0OTg2OGQ3OGY1YWZhNTY2MTU3YzI2MTJhZTQxMjAifX19", 1, 0, "§aAlles einzahlen", null));
        if (playerData.getFactionGrade() >= 7) {
            inv.setItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjhkNWEzOGQ2YmZjYTU5Nzg2NDE3MzM2M2QyODRhOGQzMjljYWFkOTAxOGM2MzgxYjFiNDI5OWI4YjhiOTExYyJ9fX0=", 1, 0, "§cAuszahlen", null));
            inv.setItem(29, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNmZjkxZGM5OWQ1ODI4MDIzZWVkZjg3Mzc5OWQyNTUzNWRhZGU2NGEyZTE2YTNiNDk4YjQxMTNlYWZkNDk2NiJ9fX0=", 1, 0, "§cAlles auszahlen", null));
        }
        for (int i = 0; i < 45; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
        }
        inv.setItem(44, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjUzNDc0MjNlZTU1ZGFhNzkyMzY2OGZjYTg1ODE5ODVmZjUzODlhNDU0MzUzMjFlZmFkNTM3YWYyM2QifX19", 1, 0, "§aPrivates Konto", null));
        player.openInventory(inv);
    }

    @EventHandler
    public void onSubmit(SubmitChatEvent event) throws SQLException {
        System.out.println("submit event trigger");
        Player player = event.getPlayer();
        if (event.getSubmitTo().equals("atm_einzahlen")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int amount = Integer.parseInt(event.getMessage());
            if (amount >= 1) {
                if (PlayerManager.money(player) >= amount) {
                    PlayerManager.removeMoney(player, amount, "Bankeinzahlung");
                    PlayerManager.addBankMoney(player, amount, "Bankeinzahlung");
                    player.sendMessage("§8[§aATM§8]§a Du hast " + amount + "$ eingezahlt.");
                } else {
                    player.sendMessage(Main.error + "Du hast nicht genug Geld dabei.");
                }
            }
            event.end();
        }
        if (event.getSubmitTo().equals("atm_auszahlen")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int amount = Integer.parseInt(event.getMessage());
            if (amount >= 1) {
                if (PlayerManager.bank(player) >= amount) {
                    PlayerManager.removeBankMoney(player, amount, "Bankauszahlung");
                    PlayerManager.addMoney(player, amount);
                    player.sendMessage("§8[§aATM§8]§a Du hast " + amount + "$ ausgezahlt.");
                } else {
                    player.sendMessage(Main.error + "Du hast nicht genug Geld auf der Bank.");
                }
            }
            event.end();
        }
        if (event.getSubmitTo().equals("atm_transfer_player")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            Player targetplayer = Bukkit.getPlayer(event.getMessage());
            if (!targetplayer.isOnline()) {
                player.sendMessage(Main.error + "Der Spieler konnte nicht gefunden werden.");
                event.end();
            } else {
                event.end();
                player.sendMessage("§8[§aATM§8]§7 Gib nun an, wie viel Geld " + targetplayer.getName() + " erhalten soll.");
                event.getPlayerData().setVariable("chatblock", "atm_transfer_amount");
                event.getPlayerData().setVariable("transfer_player", targetplayer.getName());
            }
        }
        if (event.getSubmitTo().equals("atm_transfer_amount")) {
            System.out.println("transfer is raus");
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            if (event.getMessage().equals(event.getPlayerData().getVariable("transfer_player"))) return;
            Player targetplayer = Bukkit.getPlayer(event.getPlayerData().getVariable("transfer_player"));
            if (!targetplayer.isOnline()) {
                player.sendMessage(Main.error + "Der Spieler konnte nicht gefunden werden.");
                event.end();
            } else {
                event.getPlayerData().setVariable("transfer_player", null);
                int amount = Integer.parseInt(event.getMessage());
                if (amount >= 1) {
                    if (PlayerManager.bank(player) >= amount) {
                        PlayerManager.removeBankMoney(player, amount, "Überweisung an " + targetplayer.getName());
                        PlayerManager.addMoney(targetplayer, amount);
                        player.sendMessage("§8[§aATM§8]§a Du hast " + amount + "$ an " + targetplayer.getName() + " überwiesen.");
                        targetplayer.sendMessage("§8[§6Bank§8]§a " + player.getName() + " hat dir " + amount + "$ überwiesen.");
                    } else {
                        player.sendMessage(Main.error + "Du hast nicht genug Geld auf der Bank.");
                    }
                }
                event.end();
            }
        }
        if (event.getSubmitTo().equals("atm_frak_einzahlen")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int amount = Integer.parseInt(event.getMessage());
            if (amount >= 1) {
                if (PlayerManager.money(player) >= amount) {
                    PlayerManager.removeMoney(player, amount, "Bankauszahlung");
                    FactionManager.addFactionMoney(event.getPlayerData().getFaction(), amount, "Bankeinzahlung " + player.getName());
                    player.sendMessage("§8[§aATM§8]§a Du hast " + amount + "$ eingezahlt.");
                    FactionManager.sendMessageToFaction(event.getPlayerData().getFaction(), player.getName() + " hat " + amount + "$ auf das Fraktionskonto eingezahlt.");
                } else {
                    player.sendMessage(Main.error + "Du hast nicht genug Geld dabei.");
                }
            }
            event.end();
        }
        if (event.getSubmitTo().equals("atm_frak_auszahlen")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int amount = Integer.parseInt(event.getMessage());
            if (amount >= 1) {
                if (FactionManager.factionBank(event.getPlayerData().getFaction()) >= amount) {
                    FactionManager.removeFactionMoney(event.getPlayerData().getFaction(), amount, "Bankauszahlung " + player.getName());
                    PlayerManager.addMoney(player, amount);
                    player.sendMessage("§8[§aATM§8]§a Du hast " + amount + "$ ausgezahlt.");
                    FactionManager.sendMessageToFaction(event.getPlayerData().getFaction(), player.getName() + " hat " + amount + "$ vom Fraktionskonto ausgezahlt.");
                } else {
                    player.sendMessage(Main.error + "Du hast nicht genug Geld auf der Bank.");
                }
            }
            event.end();
        }
    }
}
