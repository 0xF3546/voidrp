package de.polo.metropiacity.playerUtils;

import de.polo.metropiacity.dataStorage.ATM;
import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.RegisteredBlock;
import de.polo.metropiacity.utils.*;
import de.polo.metropiacity.utils.events.SubmitChatEvent;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BankingUtils implements Listener {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final List<ATM> atmList = new ArrayList<>();
    @SneakyThrows
    public BankingUtils(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM atm LEFT JOIN blocks ON atm.blockId = blocks.id");
        while (result.next()) {
            ATM atm = new ATM();
            atm.setId(result.getInt("atm.id"));
            atm.setName(result.getString("atm.name"));
            atm.setLocation(new Location(Bukkit.getWorld(result.getString("blocks.world")), result.getDouble("blocks.x"), result.getDouble("blocks.y"), result.getDouble("blocks.z")));
            atmList.add(atm);
        }
    }

    public Collection<ATM> getATMs() {
        return atmList;
    }
    public void sendKontoauszug(Player player) {
        player.sendMessage(" ");
        player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §aBankguthaben§8:§7 " + playerManager.bank(player) + "$");
        player.sendMessage(" ");
    }

    public void sendBankChangeReason(Player player, String reason) {
        player.sendMessage(" ");
        player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
        player.sendMessage(" ");
        player.sendMessage("§8 ➜ §3Kontoveränderung§8:§b " + reason);
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §aBankguthaben§8:§7 " + playerManager.bank(player) + "$");
        player.sendMessage(" ");
    }

    public void openBankMenu(Player player, ATM atm) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("atm", atm);
        playerData.setVariable("current_inventory", "atm");
        playerData.setVariable("atm_name", atm.getName());
        Inventory inv = Bukkit.createInventory(player, 45, "§8 » §aBankautomat " + atm.getId());
        inv.setItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjhkNWEzOGQ2YmZjYTU5Nzg2NDE3MzM2M2QyODRhOGQzMjljYWFkOTAxOGM2MzgxYjFiNDI5OWI4YjhiOTExYyJ9fX0=", 1, 0, "§cAuszahlen", null));
        inv.setItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBmZmFkMzNkMjkzYjYxNzY1ZmM4NmFiNTU2MDJiOTU1YjllMWU3NTdhOGU4ODVkNTAyYjNkYmJhNTQyNTUxNyJ9fX0=", 1, 0, "§bKontostand", Arrays.asList("§8 ➥ §a" + new DecimalFormat("#,###").format(playerData.getBank()) + "$")));
        inv.setItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19", 1, 0, "§aEinzahlen", null));
        inv.setItem(29, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNmZjkxZGM5OWQ1ODI4MDIzZWVkZjg3Mzc5OWQyNTUzNWRhZGU2NGEyZTE2YTNiNDk4YjQxMTNlYWZkNDk2NiJ9fX0=", 1, 0, "§cAlles auszahlen", null));
        inv.setItem(33, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTI1MGIzY2NlNzY2MzVlZjRjN2E4OGIyYzU5N2JkMjc0OTg2OGQ3OGY1YWZhNTY2MTU3YzI2MTJhZTQxMjAifX19", 1, 0, "§aAlles einzahlen", null));
        inv.setItem(31, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjc2ZGNiMjc0ZTJlMjMxNDlmNDcyNzgxNjA1YjdjNmY4Mzk5MzFhNGYxZDJlZGJkMWZmNTQ2M2FiN2M0MTI0NiJ9fX0=", 1, 0, "§7Geld überweisen", null));
        for (int i = 0; i < 45; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
        }
        if (playerData.getFaction() != null && !playerData.getFaction().equals("Zivilist")) {
            inv.setItem(44, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGVmMzU2YWQyYWE3YjE2NzhhZWNiODgyOTBlNWZhNWEzNDI3ZTVlNDU2ZmY0MmZiNTE1NjkwYzY3NTE3YjgifX19", 1, 0, "§aFraktionskonto", null));
        }
        player.openInventory(inv);
    }

    public void openFactionBankMenu(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        playerData.setVariable("current_inventory", "atm_frak");
        Inventory inv = Bukkit.createInventory(player, 45, "§8 » §" + factionData.getPrimaryColor() + "Fraktionskonto");
        inv.setItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBmZmFkMzNkMjkzYjYxNzY1ZmM4NmFiNTU2MDJiOTU1YjllMWU3NTdhOGU4ODVkNTAyYjNkYmJhNTQyNTUxNyJ9fX0=", 1, 0, "§bKontostand", Arrays.asList("§8 ➥ §a" + new DecimalFormat("#,###").format(factionData.getBank()) + "$")));
        inv.setItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19", 1, 0, "§aEinzahlen", null));
        inv.setItem(33, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTI1MGIzY2NlNzY2MzVlZjRjN2E4OGIyYzU5N2JkMjc0OTg2OGQ3OGY1YWZhNTY2MTU3YzI2MTJhZTQxMjAifX19", 1, 0, "§aAlles einzahlen", null));
        if (playerData.getFactionGrade() >= 7) {
            inv.setItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjhkNWEzOGQ2YmZjYTU5Nzg2NDE3MzM2M2QyODRhOGQzMjljYWFkOTAxOGM2MzgxYjFiNDI5OWI4YjhiOTExYyJ9fX0=", 1, 0, "§cAuszahlen", null));
            inv.setItem(29, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNmZjkxZGM5OWQ1ODI4MDIzZWVkZjg3Mzc5OWQyNTUzNWRhZGU2NGEyZTE2YTNiNDk4YjQxMTNlYWZkNDk2NiJ9fX0=", 1, 0, "§cAlles auszahlen", null));
        }
        for (int i = 0; i < 45; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
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
                if (playerManager.money(player) >= amount) {
                    playerManager.removeMoney(player, amount, "Bankeinzahlung(" + event.getPlayerData().getVariable("atm_name") + ")");
                    playerManager.addBankMoney(player, amount, "Bankeinzahlung(" + event.getPlayerData().getVariable("atm_name") + ")");
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
                if (playerManager.bank(player) >= amount) {
                    playerManager.removeBankMoney(player, amount, "Bankauszahlung (" + event.getPlayerData().getVariable("atm_name") + ")");
                    playerManager.addMoney(player, amount);
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
            Player targetplayer = Bukkit.getPlayer(event.getPlayerData().getVariable("transfer_player").toString());
            if (!targetplayer.isOnline()) {
                player.sendMessage(Main.error + "Der Spieler konnte nicht gefunden werden.");
                event.end();
            } else {
                event.getPlayerData().setVariable("transfer_player", null);
                int amount = Integer.parseInt(event.getMessage());
                if (amount >= 1) {
                    if (playerManager.bank(player) >= amount) {
                        playerManager.removeBankMoney(player, amount, "Überweisung an " + targetplayer.getName());
                        playerManager.addMoney(targetplayer, amount);
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
                if (playerManager.money(player) >= amount) {
                    playerManager.removeMoney(player, amount, "Bankauszahlung");
                    factionManager.addFactionMoney(event.getPlayerData().getFaction(), amount, "Bankeinzahlung " + player.getName());
                    player.sendMessage("§8[§aATM§8]§a Du hast " + Utils.toDecimalFormat(amount) + "$ eingezahlt.");
                    factionManager.sendMessageToFaction(event.getPlayerData().getFaction(), player.getName() + " hat " + Utils.toDecimalFormat(amount) + "$ auf das Fraktionskonto eingezahlt.");
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
                if (factionManager.factionBank(event.getPlayerData().getFaction()) >= amount) {
                    factionManager.removeFactionMoney(event.getPlayerData().getFaction(), amount, "Bankauszahlung " + player.getName());
                    playerManager.addMoney(player, amount);
                    player.sendMessage("§8[§aATM§8]§a Du hast " + Utils.toDecimalFormat(amount) + "$ ausgezahlt.");
                    factionManager.sendMessageToFaction(event.getPlayerData().getFaction(), player.getName() + " hat " + Utils.toDecimalFormat(amount) + "$ vom Fraktionskonto ausgezahlt.");
                } else {
                    player.sendMessage(Main.error + "Du hast nicht genug Geld auf der Bank.");
                }
            }
            event.end();
        }
    }
}
