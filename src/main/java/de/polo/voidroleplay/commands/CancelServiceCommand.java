package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.ServiceData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.StaatUtil;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelServiceCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;
    public CancelServiceCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("cancelservice", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        ServiceData serviceData = StaatUtil.serviceDataMap.get(player.getUniqueId().toString());
        if (serviceData != null) {
            utils.staatUtil.cancelService(player);
        } else {
            player.sendMessage(Main.error + "Du hast keinen Service offen.");
        }
        return false;
    }
}
