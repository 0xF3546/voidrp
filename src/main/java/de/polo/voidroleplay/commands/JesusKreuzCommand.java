package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class JesusKreuzCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public JesusKreuzCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("jesuskreuz", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.getFaction().equalsIgnoreCase("Kirche")) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (playerData.getFactionGrade() < 2) {
            player.sendMessage(Prefix.ERROR + "Dieser Befehl geht erst ab Rang 2!");
            return false;
        }
        if (playerData.getBargeld() < ServerManager.getPayout("jesuskreuz")) {
            player.sendMessage(Prefix.ERROR + "Du benötigst " + Utils.toDecimalFormat(ServerManager.getPayout("jesuskreuz")) + "$.");
            return false;
        }
        player.getInventory().addItem(ItemManager.createItem(RoleplayItem.JESUSKREUZ.getMaterial(), 1, 0, RoleplayItem.JESUSKREUZ.getDisplayName()));
        playerData.removeMoney(ServerManager.getPayout("jesuskreuz"), "Kauf Jesuskreuz");
        return false;
    }
}
