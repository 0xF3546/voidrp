package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.SubGroup;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.game.events.SubmitChatEvent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class SubGroupCommand implements CommandExecutor, Listener {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final LocationManager locationManager;
    public SubGroupCommand(PlayerManager playerManager, FactionManager factionManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        Main.registerCommand("subgroup", this);
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (args.length < 1) {
            return false;
        }
        if (args[0].equalsIgnoreCase("manage")) {
            if (locationManager.getDistanceBetweenCoords(player, "subgroup_manage") > 5) {
                player.sendMessage(Main.error + "Du bist nicht in der nähe der Gruppierungsverwaltung");
                return false;
            }
            openSubGroupMangeGUI(player);
        }
        return false;
    }

    private void openSubGroupMangeGUI(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getSubGroupId() == 0) {
            InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Gruppierung erstellen");
            if (playerData.getVariable("subgroup::name") == null) {
                inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.OAK_SIGN, 1, 0, "§eName")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        player.closeInventory();
                        playerData.setVariable("chatblock", "subgroup::name");
                        player.sendMessage(Main.prefix + "Gib nun den gewünschten Namen ein.");
                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.OAK_SIGN, 1, 0, "§e" + playerData.getVariable("subgroup::name"))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        player.closeInventory();
                        playerData.setVariable("chatblock", "subgroup::name");
                        player.sendMessage(Main.prefix + "Gib nun den gewünschten Namen ein.");
                    }
                });
            }
            inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§2Gründen", "§8 ➥ §a50.000$")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    String name = playerData.getVariable("subgroup::name");
                    for (SubGroup subGroup : factionManager.subGroups.getSubGroups()) {
                        if (subGroup.getName().equalsIgnoreCase(name)) {
                            player.sendMessage(Main.error + "Es gibt bereits eine Gruppierung mit diesem Namen.");
                            return;
                        }
                    }
                    if (playerData.getBank() < 50000) {
                        player.sendMessage(Main.error + "Du hast nicht genug Geld auf der Bank (50.000$).");
                        return;
                    }
                    player.closeInventory();
                    playerData.removeBankMoney(50000, "Gruppierungs-Gründung");
                    player.sendMessage(Main.prefix + "Du hast eine Gruppierung gegründet.");
                    SubGroup subGroup = new SubGroup();
                    subGroup.setName(name);
                    factionManager.subGroups.create(subGroup);
                    playerData.setSubGroupId(subGroup.getId());
                    playerData.setSubGroupGrade(3);
                    playerData.save();
                }
            });
            return;
        }

        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §f" + playerData.getSubGroup().getName());
        String errorMessage = null;
        if (playerData.getFaction() == null) {
            errorMessage = "Du bist in keiner Fraktion.";
        } else {
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            if (!factionData.isBadFrak()) {
                errorMessage = "Dieses Feature ist nur für Bad-Fraktionen.";
            } else {
                if (playerData.getFactionGrade() < 6) {
                    errorMessage = "Dafür musst du mindestens Rang 6 sein.";
                }
            }
        }
        if (errorMessage != null) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.REDSTONE, 1, 0, "§7§mAn Fraktion binden", "§8 ➥ §c" + errorMessage)) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        } else {
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            if (factionData.getSubGroupId() == 0) {
                inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§7An Fraktion binden")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        player.closeInventory();
                        SubGroup subGroup = playerData.getSubGroup();
                        factionManager.subGroups.sendMessage("§8[§f" + subGroup.getName() + "§8] §" + factionData.getPrimaryColor() + player.getName() + " hat die Gruppierung als Offizielle " + factionData.getFullname() + " UG markiert.", subGroup);
                        factionManager.sendMessageToFaction(factionData.getName(), player.getName() + " hat die Gruppierung \"" + subGroup.getName() + "\" als Offizielle Gruppierung markiert.");
                        subGroup.setFactionId(factionData.getId());
                        subGroup.save();
                        factionData.setSubGroupId(subGroup.getId());
                        factionData.save();
                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.RED_DYE, 1, 0, "§7Von Fraktion entfernen")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        player.closeInventory();
                        SubGroup subGroup = playerData.getSubGroup();
                        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
                        factionManager.subGroups.sendMessage("§8[§f" + subGroup.getName() + "§8] §" + factionData.getPrimaryColor() + player.getName() + " hat die Gruppierung als Offizielle " + factionData.getFullname() + " UG entfernt.", subGroup);
                        factionManager.sendMessageToFaction(factionData.getName(), player.getName() + " hat die Gruppierung \"" + subGroup.getName() + "\" als Offizielle Gruppierung entfernt.");
                        subGroup.setFactionId(0);
                        subGroup.save();
                        factionData.setSubGroupId(0);
                        factionData.save();
                    }
                });
            }
        }
    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) {
        if (event.getSubmitTo() == null) return;
        if (!event.getSubmitTo().equalsIgnoreCase("subgroup::name")) return;
        if (event.isCancel()) {
            event.sendCancelMessage();
            event.end();
            return;
        }
        event.getPlayerData().setVariable("subgroup::name", event.getMessage());
        openSubGroupMangeGUI(event.getPlayer());
    }
}
