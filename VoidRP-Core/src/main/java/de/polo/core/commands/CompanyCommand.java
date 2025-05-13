package de.polo.core.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.company.Company;
import de.polo.core.Main;
import de.polo.core.game.events.SubmitChatEvent;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.CompanyManager;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.storage.CoreCompany;
import de.polo.core.storage.CoreCompanyRole;
import de.polo.core.utils.Prefix;
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

public class CompanyCommand implements CommandExecutor, Listener {
    private final PlayerManager playerManager;
    private final CompanyManager companyManager;

    public CompanyCommand(PlayerManager playerManager, CompanyManager companyManager) {
        this.playerManager = playerManager;
        this.companyManager = companyManager;
        Main.registerCommand("company", this);
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        Company coreCompany = playerData.getCompany();
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("manage")) {
                LocationService locationService = VoidAPI.getService(LocationService.class);
                if (locationService.getDistanceBetweenCoords(player, "firmenverwaltung") > 5) {
                    player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe der Firmenverwaltung.");
                    return false;
                }
                if (coreCompany != null) {
                    player.sendMessage(Prefix.ERROR + "Du hast bereits eine Firma.");
                    return false;
                }
                openManageMenu(player);
            }
        }
        return false;
    }

    private void openManageMenu(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§6Firma gründen"), true, true);
        if (playerData.getVariable("company::name") == null) playerData.setVariable("company::name", "Name");
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.OAK_SIGN, 1, 0, "§6" + playerData.getVariable("company::name"), "§8 ➥ §7[§6Rechtsklick§7] Klicke um den Namen zu ändern")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("chatblock", "company::name");
                player.closeInventory();
                player.sendMessage(Prefix.MAIN + "Gib nun den Namen der Firma an.");
            }
        });
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§2Gründen (250.000$)")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBank() < 250000) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld.");
                    player.closeInventory();
                    return;
                }
                player.closeInventory();
                CoreCompany coreCompany = new CoreCompany();
                coreCompany.setOwner(player.getUniqueId());
                coreCompany.setName(playerData.getVariable("company::name"));
                coreCompany.setBank(0);
                if (companyManager.create(coreCompany)) {
                    playerData.setCompany(coreCompany);
                    playerData.removeBankMoney(250000, "Firmengründung");
                    player.sendMessage("§8[§6" + coreCompany.getName() + "§8]§a Die Firma wurde erfolgreich gegründet!");
                    playerData.save();
                    CoreCompanyRole role = new CoreCompanyRole();
                    role.addPermission("*");
                    role.setName("CEO");
                    coreCompany.createRole(role);
                    companyManager.setPlayerRole(playerData, role);
                } else {
                    player.sendMessage(Prefix.ERROR + "Es gibt diese oder eine ähnliche Firma bereits.");
                }
                player.closeInventory();
            }
        });
    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) {
        if (event.getSubmitTo().equalsIgnoreCase("company::name")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            event.getPlayerData().setVariable("company::name", event.getMessage());
            openManageMenu(event.getPlayer());
        }
    }
}
