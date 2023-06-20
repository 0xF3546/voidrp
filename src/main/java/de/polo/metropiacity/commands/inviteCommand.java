package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.FactionData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.ItemManager;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.VertragUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.Objects;

public class inviteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String playerfac = FactionManager.faction(player);
        FactionData factionData = FactionManager.factionDataMap.get(playerfac);
        if (FactionManager.faction_grade(player) >= 7) {
            if (args.length >= 1) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                assert targetplayer != null;
                if (player.getLocation().distance(targetplayer.getLocation()) <= 5) {
                    if (Objects.equals(FactionManager.faction(targetplayer), "Zivilist") || FactionManager.faction(targetplayer) == null) {
                        if (FactionManager.getMemberCount(playerfac) < factionData.getMaxMember()) {
                            try {
                                if (VertragUtil.setVertrag(player, targetplayer, "faction_invite", playerfac)) {
                                    player.sendMessage("§8[§" + FactionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8] §7" + targetplayer.getName() + " wurde in die Fraktion §aeingeladen§7.");
                                    /*Inventory inv = Bukkit.createInventory(targetplayer, 9, "§8» §7" + player.getName() + " hat dich in §" + FactionManager.getFactionPrimaryColor(playerfac) + playerfac + "§7 eingeladen.");
                                    inv.setItem(2, ItemManager.createItem(Material.EMERALD, 1, 0, "§aAnnehmen", null));
                                    inv.setItem(6, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cAblehnen", null));
                                    for (int i = 0; i < 9; i++) {
                                        if (inv.getItem(i) == null) {
                                            inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                                        }
                                    }
                                    targetplayer.openInventory(inv);*/
                                    targetplayer.sendMessage("§6" + player.getName() + " hat dich in die Fraktion " + FactionManager.getFactionPrimaryColor(playerfac) + playerfac + "§6 eingeladen.");
                                    VertragUtil.sendInfoMessage(player);
                                    PlayerData tplayerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
                                    //tplayerData.setVariable("current_inventory", "faction_invite");
                                } else {
                                    player.sendMessage("§" + FactionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8 » §7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
                                }
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            player.sendMessage(Main.error + "Deine Fraktion ist voll!");
                        }
                    } else {
                        player.sendMessage("§" + FactionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8 » §c" + targetplayer.getName() + "§7 ist bereits in einer Fraktion.");
                    }
                } else {
                    player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner nähe.");
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /invite [Spieler]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
