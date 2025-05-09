package de.polo.core.admin.commands;

import de.polo.core.Main;
import de.polo.core.manager.BlockManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.utils.Prefix;
import lombok.SneakyThrows;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RegisterblockCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final BlockManager blockManager;

    public RegisterblockCommand(PlayerManager playerManager, BlockManager blockManager) {
        this.playerManager = playerManager;
        this.blockManager = blockManager;
        Main.registerCommand("registerblock", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 90) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }

        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /registerblock [Typ] [Extra]");
            return false;
        }

        Block block = player.getTargetBlock(null, 10);

        RegisteredBlock registeredBlock = new RegisteredBlock();
        registeredBlock.setBlock(block);
        registeredBlock.setInfo(args[0]);
        registeredBlock.setLocation(block.getLocation());
        if (args.length >= 2) {
            registeredBlock.setInfoValue(args[1]);
        }
        try {
            TileState state = (TileState) block.getState();
            if (state instanceof Sign sign) {
                sign.setEditable(false);
                if (!args[0].equalsIgnoreCase("atm")) {
                    sign.setLine(1, "== §6Haus " + args[0] + " §0==");
                    sign.setLine(2, "§2Zu Verkaufen");
                    sign.update();
                    registeredBlock.setInfo("house");
                    registeredBlock.setInfoValue(args[0]);
                    player.sendMessage(Prefix.GAMEDESIGN + "Haus regestriert.");
                } else {
                    sign.setLine(0, "================");
                    sign.setLine(1, "Bankautomat");
                    sign.setLine(2, "§8[Rechtsklick]");
                    sign.setLine(3, "================");
                    sign.update();
                    registeredBlock.setInfo("atm");
                }
            }
        } catch (Exception e) {
        }

        int id = blockManager.addBlock(registeredBlock);

        player.sendMessage(Prefix.MAIN + "Du hast ein/e/n " + block.getType() + " registriert als " + args[0] + " (#" + id + ").");

        return false;
    }
}
