package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.WeaponData;
import de.polo.voidroleplay.dataStorage.WeaponType;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.game.events.SubmitChatEvent;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;

public class EquipCommand implements CommandExecutor, Listener {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final LocationManager locationManager;
    private final Weapons weapons;

    public EquipCommand(PlayerManager playerManager, FactionManager factionManager, LocationManager locationManager, Weapons weapons) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        this.weapons = weapons;
        Main.registerCommand("equip", this);
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Main.error + "Du bist in keiner Fraktion.");
            return false;
        }
        if (locationManager.getDistanceBetweenCoords(player, "equip_" + playerData.getFaction()) > 5) {
            player.sendMessage(Main.error + "Du bist nicht in der nähe deines Equip-Punktes.");
            return false;
        }
        openMain(player, playerData);
        return false;
    }

    private void openMain(Player player, PlayerData playerData) {
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §" + factionData.getPrimaryColor() + factionData.getName() + " Equip", true, true);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, "§cWaffen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openWeaponShop(player, playerData, factionData);
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.ARROW, 1, 0, "§cMunition")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openAmmoShop(player, playerData, factionData);
            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.LEATHER_CHESTPLATE, 1, 0, "§cExtra")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openExtraShop(player, playerData, factionData);
            }
        });
    }

    private void openWeaponShop(Player player, PlayerData playerData, FactionData factionData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cWaffen", true, true);
        int sturmgewehrPrice = (int) (factionData.equip.getSturmgewehr() * (100 - factionData.upgrades.getWeapon()) / 100);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, "§cSturmgewehr", "§8 ➥ §a" + sturmgewehrPrice + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (event.isLeftClick()) {
                    int priceForFaction = (int) (ServerManager.getPayout("equip_sturmgewehr") * (100 - factionData.upgrades.getWeapon()) / 100);
                    if (factionData.getBank() < priceForFaction) {
                        player.sendMessage(Main.error + "Deine Fraktion hat nicht genug Geld um diese Waffe zu kaufen.");
                        return;
                    }
                    if (playerData.getBank() < factionData.equip.getSturmgewehr()) {
                        player.sendMessage(Main.error + "Du hast nicht genug Geld.");
                        return;
                    }
                    factionData.removeFactionMoney(priceForFaction, "Waffenkauf " + player.getName());
                    factionData.addBankMoney(sturmgewehrPrice, "Waffenkauf " + player.getName());
                    playerData.removeBankMoney(sturmgewehrPrice, "Waffenkauf");
                    weapons.giveWeaponToPlayer(player, Material.DIAMOND_HORSE_ARMOR, WeaponType.NORMAL);
                } else {
                    if (playerData.getFactionGrade() < 7) {
                        player.sendMessage(Main.error_nopermission);
                        return;
                    }
                    playerData.setVariable("chatblock", "changeequipprice");
                    playerData.setVariable("type", "sturmgewehr");
                    player.sendMessage("§7Gib nun gewünschten Preis ein.");
                    player.closeInventory();
                }
            }
        });
    }

    private void openAmmoShop(Player player, PlayerData playerData, FactionData factionData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cMunition", true, true);
        int sturmgewehrPrice = (int) (factionData.equip.getSturmgewehr_ammo() * (100 - factionData.upgrades.getWeapon()) / 100);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, "§cSturmgewehr-Munition", "§8 ➥ §a" + sturmgewehrPrice + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                int priceForFaction = (int) (ServerManager.getPayout("equip_sturmgewehr_ammo") * (100 - factionData.upgrades.getWeapon()) / 100);
                if (factionData.getBank() < priceForFaction) {
                    player.sendMessage(Main.error + "Deine Fraktion ht nicht genug Geld um Munition zu kaufen.");
                    return;
                }
                if (event.isLeftClick()) {
                    if (playerData.getBank() < factionData.equip.getSturmgewehr_ammo()) {
                        player.sendMessage(Main.error + "Du hast nicht genug Geld.");
                        return;
                    }
                    WeaponData weaponData = weapons.getWeaponData(player.getEquipment().getItemInMainHand().getType());
                    if (weaponData == null) {
                        player.sendMessage(Main.error + "Halte die Waffe in der Hand.");
                        return;
                    }
                    weapons.giveWeaponAmmoToPlayer(player, player.getEquipment().getItemInMainHand(), weaponData.getMaxAmmo());
                    factionData.removeFactionMoney(priceForFaction, "Waffenkauf " + player.getName());
                    factionData.addBankMoney(sturmgewehrPrice, "Munitionskauf " + player.getName());
                    playerData.removeBankMoney(sturmgewehrPrice, "Munitionskauf");
                } else {
                    if (playerData.getFactionGrade() < 7) {
                        player.sendMessage(Main.error_nopermission);
                        return;
                    }
                    playerData.setVariable("chatblock", "changeequipprice");
                    playerData.setVariable("type", "sturmgewehr_ammo");
                    player.sendMessage("§7Gib nun gewünschten Preis ein.");
                    player.closeInventory();
                }
            }
        });
    }

    private void openExtraShop(Player player, PlayerData playerData, FactionData factionData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cExtra", true, true);
        int sturmgewehrPrice = ServerManager.getPayout("cuffs");
        if (playerData.getFaction().equalsIgnoreCase("Medic")) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.PAPER, 1, 0, "§c§lIboprofen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (factionData.getBank() < 75) {
                        player.sendMessage(Main.error + "Deine Fraktion ht nicht genug Geld um Munition zu kaufen.");
                        return;
                    }
                    if (playerData.getBank() < factionData.equip.getSturmgewehr_ammo()) {
                        player.sendMessage(Main.error + "Du hast nicht genug Geld.");
                        return;
                    }
                    playerData.removeBankMoney(75, "Munitionskauf");
                    player.getInventory().addItem(ItemManager.createItem(Material.PAPER, 1, 0, "§c§lIboprofen"));
                }
            });
        }
        if (playerData.getFaction().equalsIgnoreCase("Polizei") || playerData.getFaction().equalsIgnoreCase("FBI")) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(RoleplayItem.CUFF.getMaterial(), 1, 0, RoleplayItem.CUFF.getDisplayName(), "§8 ➥ §a" + (ServerManager.getPayout("cuffs") + "$"))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    int priceForFaction = (int) (ServerManager.getPayout("cuffs"));
                    if (factionData.getBank() < priceForFaction) {
                        player.sendMessage(Main.error + "Deine Fraktion ht nicht genug Geld um Munition zu kaufen.");
                        return;
                    }
                    if (playerData.getBank() < factionData.equip.getSturmgewehr_ammo()) {
                        player.sendMessage(Main.error + "Du hast nicht genug Geld.");
                        return;
                    }
                    factionData.removeFactionMoney(priceForFaction, "Waffenkauf " + player.getName());
                    factionData.addBankMoney(priceForFaction, "Munitionskauf " + player.getName());
                    playerData.removeBankMoney(priceForFaction, "Munitionskauf");
                    player.getInventory().addItem(ItemManager.createItem(RoleplayItem.CUFF.getMaterial(), 1, 0, RoleplayItem.CUFF.getDisplayName()));
                }
            });
            inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(RoleplayItem.ANTIBIOTIKUM.getMaterial(), 1, 0, RoleplayItem.ANTIBIOTIKUM.getDisplayName(), "§8 ➥ §a" + (ServerManager.getPayout("antibiotikum") + "$"))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    int priceForFaction = (int) (ServerManager.getPayout("antibiotikum"));
                    if (factionData.getBank() < priceForFaction) {
                        player.sendMessage(Main.error + "Deine Fraktion ht nicht genug Geld um Munition zu kaufen.");
                        return;
                    }
                    if (playerData.getBank() < factionData.equip.getSturmgewehr_ammo()) {
                        player.sendMessage(Main.error + "Du hast nicht genug Geld.");
                        return;
                    }
                    factionData.removeFactionMoney(priceForFaction, "ANTIBIOTIKUM " + player.getName());
                    factionData.addBankMoney(priceForFaction, "ANTIBIOTIKUM " + player.getName());
                    playerData.removeBankMoney(priceForFaction, "ANTIBIOTIKUM");
                    player.getInventory().addItem(ItemManager.createItem(RoleplayItem.ANTIBIOTIKUM.getMaterial(), 1, 0, RoleplayItem.ANTIBIOTIKUM.getDisplayName()));
                }
            });
            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(RoleplayItem.TAZER.getMaterial(), 1, 0, RoleplayItem.TAZER.getDisplayName(), "§8 ➥ §a" + (ServerManager.getPayout("tazer") + "$"))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    int priceForFaction = (int) (ServerManager.getPayout("tazer"));
                    if (factionData.getBank() < priceForFaction) {
                        player.sendMessage(Main.error + "Deine Fraktion ht nicht genug Geld um einen Tazer zu kaufen.");
                        return;
                    }
                    if (playerData.getBank() < factionData.equip.getSturmgewehr_ammo()) {
                        player.sendMessage(Main.error + "Du hast nicht genug Geld.");
                        return;
                    }
                    factionData.removeFactionMoney(priceForFaction, "TAZER " + player.getName());
                    factionData.addBankMoney(priceForFaction, "TAZER " + player.getName());
                    playerData.removeBankMoney(priceForFaction, "TAZER");
                    player.getInventory().addItem(ItemManager.createItem(RoleplayItem.TAZER.getMaterial(), 1, 0, RoleplayItem.TAZER.getDisplayName()));
                }
            });
            if (playerData.getFactionGrade() >= 5 && playerData.getFaction().equalsIgnoreCase("Polizei")) {
                inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(RoleplayItem.SWAT_SHIELD.getMaterial(), 1, 0, RoleplayItem.SWAT_SHIELD.getDisplayName(), "§8 ➥ §a" + (ServerManager.getPayout("swat_shield") + "$"))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        int priceForFaction = (int) (ServerManager.getPayout("swat_shield"));
                        if (factionData.getBank() < priceForFaction) {
                            player.sendMessage(Main.error + "Deine Fraktion ht nicht genug Geld um Munition zu kaufen.");
                            return;
                        }
                        if (playerData.getBank() < factionData.equip.getSturmgewehr_ammo()) {
                            player.sendMessage(Main.error + "Du hast nicht genug Geld.");
                            return;
                        }
                        factionData.removeFactionMoney(priceForFaction, "Munitionskauf " + player.getName());
                        factionData.addBankMoney(sturmgewehrPrice, "Munitionskauf " + player.getName());
                        playerData.removeBankMoney(sturmgewehrPrice, "Munitionskauf");
                        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.SWAT_SHIELD.getMaterial(), 1, 0, RoleplayItem.SWAT_SHIELD.getDisplayName()));
                    }
                });
            }
            if (playerData.getFactionGrade() >= 5 && playerData.getFaction().equalsIgnoreCase("FBI")) {
                inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(RoleplayItem.ADRENALINE_INJECTION.getMaterial(), 1, 0, RoleplayItem.ADRENALINE_INJECTION.getDisplayName(), "§8 ➥ §a" + (ServerManager.getPayout("adrenaline_injection") + "$"))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        int priceForFaction = (int) (ServerManager.getPayout("adrenaline_injection"));
                        if (factionData.getBank() < priceForFaction) {
                            player.sendMessage(Main.error + "Deine Fraktion ht nicht genug Geld um Munition zu kaufen.");
                            return;
                        }
                        if (playerData.getBank() < factionData.equip.getSturmgewehr_ammo()) {
                            player.sendMessage(Main.error + "Du hast nicht genug Geld.");
                            return;
                        }
                        factionData.removeFactionMoney(priceForFaction, "Item-Kauf " + player.getName());
                        factionData.addBankMoney(sturmgewehrPrice, "Item-Kauf " + player.getName());
                        playerData.removeBankMoney(sturmgewehrPrice, "Item-Kauf");
                        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.ADRENALINE_INJECTION.getMaterial(), 1, 0, RoleplayItem.ADRENALINE_INJECTION.getDisplayName()));
                    }
                });
            }
        }
    }

    @SneakyThrows
    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) {
        if (event.getSubmitTo().equalsIgnoreCase("changeequipprice")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            FactionData factionData = factionManager.getFactionData(event.getPlayerData().getFaction());
            switch (event.getPlayerData().getVariable("type").toString()) {
                case "sturmgewehr":
                    try {
                        int id = Integer.parseInt(event.getMessage());
                        factionData.equip.setSturmgewehr(id);
                        PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("UPDATE faction_equip SET sturmgewehr = ? WHERE factionId = ?");
                        statement.setInt(1, id);
                        statement.setInt(2, factionData.getId());
                        statement.executeUpdate();
                        factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§" + factionData.getPrimaryColor() + "Equip§8]§7 " + event.getPlayer().getName() + " hat den Preis von Sturmgewehren auf " + Main.getInstance().utils.toDecimalFormat(id) + "$ gesetzt.");
                    } catch (Exception e) {
                        event.getPlayer().sendMessage(Main.error + "Dies ist keine Zahl!");
                    }
                    break;
                case "sturmgewehr_ammo":
                    try {
                        int id = Integer.parseInt(event.getMessage());
                        factionData.equip.setSturmgewehr_ammo(id);
                        PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("UPDATE faction_equip SET sturmgewehr_ammo = ? WHERE factionId = ?");
                        statement.setInt(1, id);
                        statement.setInt(2, factionData.getId());
                        statement.executeUpdate();
                        factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§" + factionData.getPrimaryColor() + "Equip§8]§7 " + event.getPlayer().getName() + " hat den Preis von Sturmgewehr-Munition auf " + Main.getInstance().utils.toDecimalFormat(id) + "$ gesetzt.");
                    } catch (Exception e) {
                        event.getPlayer().sendMessage(Main.error + "Dies ist keine Zahl!");
                    }
                    break;
            }
        }
    }
}
