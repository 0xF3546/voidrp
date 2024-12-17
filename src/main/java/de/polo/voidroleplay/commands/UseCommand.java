package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.TabCompletion;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.Prescription;
import de.polo.voidroleplay.utils.gameplay.GamePlay;
import de.polo.voidroleplay.utils.enums.Drug;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class UseCommand implements CommandExecutor, TabCompleter {
    private final GamePlay gamePlay;

    public UseCommand(GamePlay gamePlay) {
        this.gamePlay = gamePlay;
        Main.registerCommand("use", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /use [Droge]");
            return false;
        }
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player);
        String errorMsg = "§cDu hast nicht genug Drogen.";
        Drug drug = null;
        for (Drug d : Drug.values()) {
            if (d.name().equalsIgnoreCase(args[0]) || d.getItem().getClearName().equalsIgnoreCase(args[0]) || d.getItem().name().equalsIgnoreCase(args[0])) {
                drug = d;
            }
        }
        if (drug == null) {
            player.sendMessage(Prefix.ERROR + "Die Droge wurde nicht gefunden.");
            return false;
        }
        int count = playerData.getInventory().getByTypeOrEmpty(drug.getItem()).getAmount();
        if (count < 1 && playerData.getVariable("gangwar") == null && playerData.getVariable("ffa") == null) {
            player.sendMessage(Prefix.ERROR + "Du hast nicht genug dabei.");
            return false;
        }
        GamePlay.useDrug(player, drug);
        playerData.getInventory().removeItem(drug.getItem(), 1);
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, Arrays.stream(Drug.values())
                        .map(drug -> drug.getItem().getClearName())
                        .toList())
                .build();
    }
}
