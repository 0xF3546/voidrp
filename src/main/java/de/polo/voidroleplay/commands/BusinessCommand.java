package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.BusinessData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.BusinessManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BusinessCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final BusinessManager businessManager;

    public BusinessCommand(PlayerManager playerManager, BusinessManager businessManager) {
        this.playerManager = playerManager;
        this.businessManager = businessManager;
        Main.registerCommand("business", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getBusiness() == null || playerData.getBusiness() == 0) {
            if (playerData.getPermlevel() >= 20) {
                player.sendMessage("§8[§6Business§8]§a Du hast ein Business erstellt!");
                BusinessData businessData = new BusinessData();
                businessData.setOwner(player.getUniqueId());
                businessData.setActive(true);
                int id = businessManager.createBusiness(businessData);
                playerData.setBusiness(id);
                playerData.save();
                return false;
            } else {
                player.sendMessage(Prefix.ERROR + "Für ein Business benötigst du Premium.");
            }
        }
        player.sendMessage(Prefix.ERROR + "Du bist bereits in einem Business.");
        return false;
    }
}
