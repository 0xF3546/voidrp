package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.faction.entity.FactionData;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.WeaponType;
import de.polo.voidroleplay.game.events.SubmitChatEvent;
import de.polo.voidroleplay.manager.*;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.utils.enums.Weapon;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
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
    private final WeaponManager weaponManager;

    public EquipCommand(PlayerManager playerManager, FactionManager factionManager, LocationManager locationManager, WeaponManager weaponManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        this.weaponManager = weaponManager;
        Main.registerCommand("equip", this);
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR + "Du bist in keiner Fraktion.");
            return false;
        }
        if (locationManager.getDistanceBetweenCoords(player, "equip_" + playerData.getFaction()) > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe deines Equip-Punktes.");
            return false;
        }
        if (playerData.getFaction().equalsIgnoreCase("Kirche")) {
            openChurch(player, playerData);
            return false;
        }
        if (playerData.getFaction().equalsIgnoreCase("Medic")) {
            openMedic(player, playerData);
            return false;
        }
        if (playerData.getFaction().equalsIgnoreCase("News")) {
            openNews(player, playerData);
            return false;
        }
        openMain(player, playerData);
        return false;
    }

    private void openNews(Player player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Equip (News)", true, true);
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(Material.WRITABLE_BOOK, 1, 0, "§7Buch")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < 15) {
                    player.sendMessage(Prefix.ERROR + "Du benötigst 15$!");
                    return;
                }
                playerData.removeMoney(15, "Buch");
                player.getInventory().addItem(ItemManager.createItem(Material.WRITABLE_BOOK, 1, 0, "§7Buch"));
            }
        });
    }

    private void openMedic(Player player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Equip (Medic)", true, true);
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(RoleplayItem.BANDAGE.getMaterial(), 1, 0, RoleplayItem.BANDAGE.getDisplayName())) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < 20) {
                    player.sendMessage(Prefix.ERROR + "Du benötigst 20$!");
                    return;
                }
                playerData.removeMoney(20, "Kauf BANDAGE");
                ItemManager.addCustomItem(player, RoleplayItem.BANDAGE, 1);
            }
        });
        inventoryManager.setItem(new CustomItem(1, ItemManager.createItem(RoleplayItem.IBOPROFEN.getMaterial(), 1, 0, RoleplayItem.IBOPROFEN.getDisplayName())) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < 20) {
                    player.sendMessage(Prefix.ERROR + "Du benötigst 20$!");
                    return;
                }
                playerData.removeMoney(20, "Kauf Iboprofen");
                ItemManager.addCustomItem(player, RoleplayItem.IBOPROFEN, 1);
            }
        });
    }

    private void openChurch(Player player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Equip (Kirche)", true, true);
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(Material.WATER, 1, 0, "§fKirchquell Wasser")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < 3) {
                    player.sendMessage(Prefix.ERROR + "Du benötigst 3$!");
                    return;
                }
                playerData.removeMoney(3, "Kauf Kirchquell Wasser");
                player.getInventory().addItem(ItemManager.createItem(Material.WATER, 1, 0, "§fKirchquell Wasser"));
            }
        });
        inventoryManager.setItem(new CustomItem(1, ItemManager.createItem(Material.LINGERING_POTION, 1, 0, "§cRoter Wein")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < 3) {
                    player.sendMessage(Prefix.ERROR + "Du benötigst 3$!");
                    return;
                }
                playerData.removeMoney(3, "Kauf Roter Wein");
                player.getInventory().addItem(ItemManager.createItem(Material.LINGERING_POTION, 1, 0, "§cRoter Wein"));
            }
        });
        inventoryManager.setItem(new CustomItem(2, ItemManager.createItem(Material.BREAD, 16, 0, "§fLeib Christi")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < 3) {
                    player.sendMessage(Prefix.ERROR + "Du benötigst 15$!");
                    return;
                }
                playerData.removeMoney(15, "Kauf Leib Christi");
                player.getInventory().addItem(ItemManager.createItem(Material.BREAD, 16, 0, "§fLeib Christi"));
            }
        });
        inventoryManager.setItem(new CustomItem(3, ItemManager.createItem(Material.DIAMOND, 1, 0, "§bEhering")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < 1000) {
                    player.sendMessage(Prefix.ERROR + "Du benötigst 1000$!");
                    return;
                }
                playerData.removeMoney(1000, "Kauf Ehering");
                player.getInventory().addItem(ItemManager.createItem(Material.DIAMOND, 1, 0, "§bEhering"));
            }
        });
    }

    private void openMain(Player player, PlayerData playerData) {
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §" + factionData.getPrimaryColor() + factionData.getName() + " Equip", true, true);
        String ERROR_NOT_ENOUGH_EQUIP = Prefix.ERROR + "Deine Fraktion hat nicht genug Equip-Punkte.";
        int i = 0;
        inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.BULLETPROOF.getMaterial(), 1, 0, RoleplayItem.BULLETPROOF.getDisplayName())) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (factionData.getEquipPoints() < 8) {
                    player.sendMessage(ERROR_NOT_ENOUGH_EQUIP);
                    return;
                }
                if (ItemManager.hasPlayerBulletproof(player)) {
                    player.sendMessage(Component.text(Prefix.ERROR + "Du hast bereits eine Schutzweste dabei."));
                    return;
                }
                ItemManager.addCustomItem(player, RoleplayItem.BULLETPROOF, 1);
                factionData.setEquipPoints(factionData.getEquipPoints() - 8);
                factionData.save();
                logBuy(player, "Schutzweste", 8);
            }
        });
        i++;
        if (playerData.getFactionGrade() >= 3) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.HEAVY_BULLETPROOF.getMaterial(), 1, 0, RoleplayItem.HEAVY_BULLETPROOF.getDisplayName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (factionData.getEquipPoints() < 12) {
                        player.sendMessage(ERROR_NOT_ENOUGH_EQUIP);
                        return;
                    }
                    if (ItemManager.hasPlayerBulletproof(player)) {
                        player.sendMessage(Component.text(Prefix.ERROR + "Du hast bereits eine Schutzweste dabei."));
                        return;
                    }
                    ItemManager.addCustomItem(player, RoleplayItem.HEAVY_BULLETPROOF, 1);
                    factionData.setEquipPoints(factionData.getEquipPoints() - 12);
                    factionData.save();
                    logBuy(player, "Schwere Schutzweste", 12);
                }
            });
            i++;
        }
        if (playerData.getFaction().equalsIgnoreCase("FBI") && playerData.getFactionGrade() >= 4) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Weapon.SNIPER.getMaterial(), 1, 0, Weapon.SNIPER.getName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (factionData.getEquipPoints() < 10) {
                        player.sendMessage(ERROR_NOT_ENOUGH_EQUIP);
                        return;
                    }
                    weaponManager.giveWeapon(player, Weapon.SNIPER, WeaponType.NORMAL);
                    factionData.setEquipPoints(factionData.getEquipPoints() - 10);
                    factionData.save();
                    logBuy(player, "Sniper", 10);
                }
            });
            i++;
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.MAGAZIN.getMaterial(), 1, 0, Weapon.SNIPER.getName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (factionData.getEquipPoints() < 10) {
                        player.sendMessage(ERROR_NOT_ENOUGH_EQUIP);
                        return;
                    }
                    // weaponManager.giveAmmo(player, Weapon.SNIPER, 10);
                    factionData.setEquipPoints(factionData.getEquipPoints() - 1);
                    factionData.save();
                    weaponManager.giveAmmo(player, Weapon.SNIPER, Weapon.SNIPER.getMaxAmmo());
                    logBuy(player, "Sniper Munition", 10);
                }
            });
            i++;
        }
        if (playerManager.isInStaatsFrak(player)) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.PFEFFERSPRAY.getMaterial(), 1, 0, RoleplayItem.PFEFFERSPRAY.getDisplayName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (factionData.getEquipPoints() < 1) {
                        player.sendMessage(ERROR_NOT_ENOUGH_EQUIP);
                        return;
                    }
                    ItemManager.addCustomItem(player, RoleplayItem.PFEFFERSPRAY, 1);
                    factionData.setEquipPoints(factionData.getEquipPoints() - 1);
                    factionData.save();
                    logBuy(player, "Pfefferspray", 1);
                }
            });
            i++;
        }
        if (playerData.isExecutiveFaction()) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.CUFF.getMaterial(), 1, 0, RoleplayItem.CUFF.getDisplayName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (factionData.getEquipPoints() < 1) {
                        player.sendMessage(ERROR_NOT_ENOUGH_EQUIP);
                        return;
                    }
                    ItemManager.addCustomItem(player, RoleplayItem.CUFF, 2);
                    factionData.setEquipPoints(factionData.getEquipPoints() - 1);
                    factionData.save();
                    logBuy(player, "Handschellen", 1);
                }
            });
            i++;
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.TAZER.getMaterial(), 1, 0, RoleplayItem.TAZER.getDisplayName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (factionData.getEquipPoints() < 1) {
                        player.sendMessage(ERROR_NOT_ENOUGH_EQUIP);
                        return;
                    }
                    ItemManager.addCustomItem(player, RoleplayItem.TAZER, 1);
                    factionData.setEquipPoints(factionData.getEquipPoints() - 1);
                    factionData.save();
                    logBuy(player, "Tazer", 1);
                }
            });
            i++;
            if (playerData.getFactionGrade() >= 3) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.WINGSUIT.getMaterial(), 1, 0, RoleplayItem.WINGSUIT.getDisplayName())) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (factionData.getEquipPoints() < 2) {
                            player.sendMessage(ERROR_NOT_ENOUGH_EQUIP);
                            return;
                        }
                        ItemManager.addCustomItem(player, RoleplayItem.WINGSUIT, 1);
                        factionData.setEquipPoints(factionData.getEquipPoints() - 2);
                        factionData.save();
                        logBuy(player, "Wingsuit", 2);
                    }
                });
                i++;
            }
        }

        if (playerData.getSubTeam() != null) {
            if (playerData.getSubTeam().getName().equalsIgnoreCase("Feuerwehr")) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.FEUERLÖSCHER.getMaterial(), 1, 0, RoleplayItem.FEUERLÖSCHER.getDisplayName())) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (factionData.getEquipPoints() < 1) {
                            player.sendMessage(ERROR_NOT_ENOUGH_EQUIP);
                            return;
                        }
                        ItemManager.addCustomItem(player, RoleplayItem.FEUERLÖSCHER, 1);
                        factionData.setEquipPoints(factionData.getEquipPoints() - 1);
                        factionData.save();
                        logBuy(player, "Feuerlöscher", 1);
                    }
                });
                i++;
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.FEUERWEHR_AXT.getMaterial(), 1, 0, RoleplayItem.FEUERWEHR_AXT.getDisplayName())) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (factionData.getEquipPoints() < 1) {
                            player.sendMessage(ERROR_NOT_ENOUGH_EQUIP);
                            return;
                        }
                        ItemManager.addCustomItem(player, RoleplayItem.FEUERWEHR_AXT, 1);
                        factionData.setEquipPoints(factionData.getEquipPoints() - 1);
                        factionData.save();
                        logBuy(player, "Feuerwehr-Axt", 1);
                    }
                });
                i++;
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.SPRUNGTUCH.getMaterial(), 1, 0, RoleplayItem.SPRUNGTUCH.getDisplayName())) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (factionData.getEquipPoints() < 1) {
                            player.sendMessage(ERROR_NOT_ENOUGH_EQUIP);
                            return;
                        }
                        ItemManager.addCustomItem(player, RoleplayItem.SPRUNGTUCH, 1);
                        factionData.setEquipPoints(factionData.getEquipPoints() - 1);
                        factionData.save();
                        logBuy(player, "Sprungtuch", 1);
                    }
                });
                i++;
            }
            if (playerData.getSubTeam().getName().equalsIgnoreCase("Notfallmedizin")) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.SPRUNGTUCH.getMaterial(), 1, 0, RoleplayItem.SPRUNGTUCH.getDisplayName())) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (factionData.getEquipPoints() < 1) {
                            player.sendMessage(ERROR_NOT_ENOUGH_EQUIP);
                            return;
                        }
                        ItemManager.addCustomItem(player, RoleplayItem.SPRUNGTUCH, 1);
                        factionData.setEquipPoints(factionData.getEquipPoints() - 1);
                        factionData.save();
                        logBuy(player, "Sprungtuch", 1);
                    }
                });
                i++;
            }

            if (playerData.getFaction().equalsIgnoreCase("Polizei") && playerData.getSubTeam().getName().equalsIgnoreCase("SWAT")) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(RoleplayItem.SWAT_SHIELD.getMaterial(), 1, 0, RoleplayItem.SWAT_SHIELD.getDisplayName())) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (factionData.getEquipPoints() < 4) {
                            player.sendMessage(ERROR_NOT_ENOUGH_EQUIP);
                            return;
                        }
                        ItemManager.addCustomItem(player, RoleplayItem.SWAT_SHIELD, 1);
                        factionData.setEquipPoints(factionData.getEquipPoints() - 4);
                        factionData.save();
                        logBuy(player, "SWAT-Schild", 4);
                    }
                });
                i++;
            }
        }
    }

    private void logBuy(Player player, String item, int equipPoints) {
        PlayerData playerData = playerManager.getPlayerData(player);
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        Main.getInstance().getCoreDatabase().insertAsync("INSERT INTO faction_equip_logs (player, item, factionId, itemPoints) VALUES (?, ?, ?, ?)", player.getUniqueId().toString(), item, factionData.getId(), equipPoints);
    }

    private void openExtraShop(Player player, PlayerData playerData, FactionData factionData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cExtra", true, true);
        if (playerData.getFaction().equalsIgnoreCase("Terroristen")) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(RoleplayItem.SPRENGSTOFF.getMaterial(), 1, 0, RoleplayItem.SPRENGSTOFF.getDisplayName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getFactionGrade() < 4) {
                        player.sendMessage(Prefix.ERROR + "Du musst mindestens rang 4 sein um einen Sprengstoff zu kaufen!");
                        return;
                    }
                    if (factionData.getBank() < 2500) {
                        player.sendMessage(Prefix.ERROR + "Deine Fraktion hat nicht genug Geld um einen Sprengstoff zu kaufen.");
                        return;
                    }
                    if (playerData.getBank() < 2500) {
                        player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld.");
                        return;
                    }
                    playerData.removeBankMoney(2500, "Sprengstoff-Kauf");
                    ItemManager.addCustomItem(player, RoleplayItem.SPRENGSTOFF, 1);
                }
            });
            inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(RoleplayItem.GRANATE.getMaterial(), 1, 0, RoleplayItem.GRANATE.getDisplayName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getFactionGrade() < 3) {
                        player.sendMessage(Prefix.ERROR + "Du musst mindestens rang 3 sein um eine Granate zu kaufen!");
                        return;
                    }
                    if (factionData.getBank() < 1500) {
                        player.sendMessage(Prefix.ERROR + "Deine Fraktion hat nicht genug Geld um eine Splittergranate zu kaufen.");
                        return;
                    }
                    if (playerData.getBank() < 1500) {
                        player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld.");
                        return;
                    }
                    playerData.removeBankMoney(1500, "Splittergranaten-Kauf");
                    ItemManager.addCustomItem(player, RoleplayItem.GRANATE, 1);
                }
            });
            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(RoleplayItem.SPRENGGUERTEL.getMaterial(), 1, 0, RoleplayItem.SPRENGGUERTEL.getDisplayName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getFactionGrade() < 4) {
                        player.sendMessage(Prefix.ERROR + "Du musst mindestens rang 4 sein um einen Sprenggürtel zu kaufen!");
                        return;
                    }
                    if (factionData.getBank() < 5000) {
                        player.sendMessage(Prefix.ERROR + "Deine Fraktion hat nicht genug Geld um einen Sprenggürtel zu kaufen.");
                        return;
                    }
                    if (playerData.getBank() < 5000) {
                        player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld.");
                        return;
                    }
                    playerData.removeBankMoney(5000, "Sprenggürtel-Kauf");
                    player.getInventory().addItem(ItemManager.createItem(RoleplayItem.SPRENGGUERTEL.getMaterial(), 1, 0, RoleplayItem.SPRENGGUERTEL.getDisplayName()));
                }
            });
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
            try {
                int money = Integer.parseInt(event.getMessage());
                if (money < 1) {
                    event.getPlayer().sendMessage(Prefix.ERROR + "Du kannst das Equip nicht auf weniger als 1$ setzen.");
                    return;
                }
            } catch (Exception ex) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Es ist ein Fehler unterlaufen.");
                return;
            }
            switch (event.getPlayerData().getVariable("type").toString()) {
                case "sturmgewehr":
                    try {
                        int id = Integer.parseInt(event.getMessage());
                        factionData.equip.setSturmgewehr(id);
                        PreparedStatement statement = Main.getInstance().coreDatabase.getConnection().prepareStatement("UPDATE faction_equip SET sturmgewehr = ? WHERE factionId = ?");
                        statement.setInt(1, id);
                        statement.setInt(2, factionData.getId());
                        statement.executeUpdate();
                        factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§" + factionData.getPrimaryColor() + "Equip§8]§7 " + event.getPlayer().getName() + " hat den Preis von Sturmgewehren auf " + Utils.toDecimalFormat(id) + "$ gesetzt.");
                    } catch (Exception e) {
                        event.getPlayer().sendMessage(Prefix.ERROR + "Dies ist keine Zahl!");
                    }
                    break;
                case "sturmgewehr_ammo":
                    try {
                        int id = Integer.parseInt(event.getMessage());
                        factionData.equip.setSturmgewehr_ammo(id);
                        PreparedStatement statement = Main.getInstance().coreDatabase.getConnection().prepareStatement("UPDATE faction_equip SET sturmgewehr_ammo = ? WHERE factionId = ?");
                        statement.setInt(1, id);
                        statement.setInt(2, factionData.getId());
                        statement.executeUpdate();
                        factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§" + factionData.getPrimaryColor() + "Equip§8]§7 " + event.getPlayer().getName() + " hat den Preis von Sturmgewehr-Munition auf " + Utils.toDecimalFormat(id) + "$ gesetzt.");
                    } catch (Exception e) {
                        event.getPlayer().sendMessage(Prefix.ERROR + "Dies ist keine Zahl!");
                    }
                    break;
            }
        }
    }
}
