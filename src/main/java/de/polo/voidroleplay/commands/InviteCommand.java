package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.BusinessData;
import de.polo.voidroleplay.storage.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.BusinessManager;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.VertragUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InviteCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final BusinessManager businessManager;
    private final Utils utils;

    public InviteCommand(PlayerManager playerManager, FactionManager factionManager, Utils utils, BusinessManager businessManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        this.businessManager = businessManager;
        Main.registerCommand("invite", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFaction() == null && playerData.getCompany() == null) {
            player.sendMessage(Prefix.ERROR + "Du kannst niemanden einladen.");
            return false;
        }
        if (!(args.length >= 1)) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /invite [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Prefix.ERROR + args[0] + " ist nicht online.");
            return false;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 9, "§3" + targetplayer.getName() + " einladen", true, true);
        int i = 0;
        if (playerData.getCompany() != null) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.BOOK, 1, 0, "§6In Firma einladen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    inviteToCompany(player, targetplayer);
                    player.closeInventory();
                }
            });
            i++;
        }
        if (playerData.getFaction() != null && playerData.isLeader()) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§6In Fraktion einladen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    inviteToFaction(player, targetplayer);
                    player.closeInventory();
                }
            });
            i++;
        }
        if ((playerData.getBusiness() != null || playerData.getBusiness() != 0) && playerData.getBusiness_grade() >= 4) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.GLOWSTONE_DUST, 1, 0, "§6In Business einladen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    inviteToBusiness(player, targetplayer);
                    player.closeInventory();
                }
            });
            i++;
        }
        if (playerData.getSubGroup() != null && playerData.getSubGroupGrade() >= 2) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.LAPIS_LAZULI, 1, 0, "§6In Untergruppierung einladen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    inviteToSubGroup(player, targetplayer);
                    player.closeInventory();
                }
            });
            i++;
        }
        return false;
    }

    private void inviteToBusiness(Player player, Player targetplayer) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerManager.getPlayerData(targetplayer.getUniqueId()).getBusiness() != 0) {
            player.sendMessage("§8[§6Business§8] §c" + targetplayer.getName() + " ist bereits in einem Business.");
            return;
        }
        BusinessData businessData = businessManager.getBusinessData(playerData.getBusiness());
        if (BusinessManager.getMemberCount(playerData.getBusiness()) >= businessData.getMaxMember()) {
            player.sendMessage(Prefix.ERROR + "Dein Business ist voll!");
            return;
        }
        if (VertragUtil.setVertrag(player, targetplayer, "business_invite", playerData.getBusiness())) {
            player.sendMessage("§8[§6Business§8] §7" + targetplayer.getName() + " wurde in das Business §aeingeladen§7.");
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(businessData.getOwner());
            targetplayer.sendMessage("§6" + player.getName() + " hat dich in das Business von §e" + offlinePlayer.getName() + "§6 eingeladen.");
            utils.vertragUtil.sendInfoMessage(targetplayer);
            PlayerData tplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        } else {
            player.sendMessage("§8[§6Business§8]§8 §7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
        }
    }

    private void inviteToCompany(Player player, Player targetplayer) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerManager.getPlayerData(targetplayer.getUniqueId()).getCompany() != null) {
            player.sendMessage("§8[§6Firma§8] §c" + targetplayer.getName() + " ist bereits in einer Firma.");
            return;
        }
        if (VertragUtil.setVertrag(player, targetplayer, "company_invite", playerData.getCompany().getId())) {
            player.sendMessage("§8[§6Firma§8] §7" + targetplayer.getName() + " wurde in die Firma §aeingeladen§7.");
            targetplayer.sendMessage("§6" + player.getName() + " hat dich in die Firma §e" + playerData.getCompany().getName() + "§6 eingeladen.");
            utils.vertragUtil.sendInfoMessage(targetplayer);
            PlayerData tplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        } else {
            player.sendMessage("§8[§6Firma§8]§8 §7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
        }
    }

    private void inviteToFaction(Player player, Player targetplayer) {
        String playerfac = factionManager.faction(player);
        FactionData factionData = factionManager.getFactionData(playerfac);
        if (player.getLocation().distance(targetplayer.getLocation()) >= 5) {
            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in deiner nähe.");
            return;
        }
        PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        if (targetplayerData.getFaction() != null) {
            player.sendMessage("§8[§" + factionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8] §c" + targetplayer.getName() + " ist bereits in einer Fraktion.");
            return;
        }
        if (targetplayerData.getFactionCooldown() != null && Utils.getTime().isBefore(targetplayerData.getFactionCooldown())) {
            player.sendMessage(Prefix.ERROR + "Der Spieler kann noch nicht invited werden!");
            return;
        }
        if (factionManager.getMemberCount(playerfac) >= factionData.getMaxMember()) {
            player.sendMessage(Prefix.ERROR + "Deine Fraktion ist voll!");
            return;
        }
        if (VertragUtil.setVertrag(player, targetplayer, "faction_invite", playerfac)) {
            player.sendMessage("§8[§" + factionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8] §7" + targetplayer.getName() + " wurde in die Fraktion §aeingeladen§7.");
            targetplayer.sendMessage("§6" + player.getName() + " hat dich in die Fraktion §" + factionManager.getFactionPrimaryColor(playerfac) + factionData.getFullname() + "§6 eingeladen.");
            utils.vertragUtil.sendInfoMessage(targetplayer);
        } else {
            player.sendMessage("§8[§" + factionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8] §7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
        }
    }

    private void inviteToSubGroup(Player player, Player targetplayer) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerManager.getPlayerData(targetplayer.getUniqueId()).getSubGroup() != null) {
            player.sendMessage("§8[§f" + playerData.getSubGroup().getName() + "§8] §3" + targetplayer.getName() + " ist bereits in einer Untergruppierung.");
            return;
        }
        if (VertragUtil.setVertrag(player, targetplayer, "subgroup_invite", playerData.getCompany().getId())) {
            player.sendMessage("§8[§f" + playerData.getSubGroup().getName() + "§8] §3" + targetplayer.getName() + " wurde in die Firma §aeingeladen§3.");
            targetplayer.sendMessage("§6" + player.getName() + " hat dich in die Untergruppierung §e" + playerData.getSubGroup().getName() + "§6 eingeladen.");
            utils.vertragUtil.sendInfoMessage(targetplayer);
            PlayerData tplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        } else {
            player.sendMessage("§8[§6Firma§8]§8 §7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
        }
    }
}
