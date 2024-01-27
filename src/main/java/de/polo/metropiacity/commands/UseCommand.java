package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.GamePlay.GamePlay;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.enums.Drug;
import de.polo.metropiacity.utils.enums.RoleplayItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UseCommand implements CommandExecutor {
    private final GamePlay gamePlay;
    public UseCommand(GamePlay gamePlay) {
        this.gamePlay = gamePlay;
        Main.registerCommand("use", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /use [Kokain/Joint]");
            return false;
        }
        int cocaineCount = ItemManager.getCustomItemCount(player, RoleplayItem.COCAINE);
        int jointCount = ItemManager.getCustomItemCount(player, RoleplayItem.NOBLE_JOINT);
        String errorMsg = "§cDu hast nicht genug Drogen.";
        switch (args[0].toLowerCase()) {
            case "kokain":
                if (cocaineCount >= 1) {
                    GamePlay.useDrug(player, Drug.COCAINE);
                } else {
                    player.sendMessage(errorMsg);
                }
                break;
            case "joint":
                if (jointCount >= 1) {
                    GamePlay.useDrug(player, Drug.JOINT);
                } else {
                    player.sendMessage(errorMsg);
                }
                break;
            default:
                player.sendMessage(Main.error + "Syntax-Fehler: /use [Kokain/Joint]");
                break;
        }
        return false;
    }
}
