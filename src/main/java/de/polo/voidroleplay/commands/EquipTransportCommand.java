package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EquipTransportCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    public EquipTransportCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("equiptransport", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData == null) return false;
        if (playerData.getBargeld() < 12200) {
            player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei. (12.200$)");
            return false;
        }
        playerData.setVariable("job", "equip");
        playerData.removeMoney(122000,"Equip-Transport");
        player.sendMessage(Prefix.MAIN + "Du hast den Transport gestartet, begib dich zu deinem Equip-Punkt und nutze /drop");
        return false;
    }
}
