package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.RegisteredBlock;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.manager.BlockManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
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
public class FixBlockCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final BlockManager blockManager;

    public FixBlockCommand(PlayerManager playerManager, BlockManager blockManager) {
        this.playerManager = playerManager;
        this.blockManager = blockManager;

        Main.registerCommand("fixblock", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 90) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }

        Block block = player.getTargetBlock(null, 10);

        RegisteredBlock registeredBlock = blockManager.getBlockAtLocation(block.getLocation());
        if (registeredBlock.getInfo().equalsIgnoreCase("atm")) {
            TileState state = (TileState) block.getState();
            if (state instanceof Sign) {
                Sign sign = (Sign) state;
                sign.setEditable(false);
                sign.setLine(1, "");
                sign.setLine(2, "Bankautomat");
                sign.update();
                player.sendMessage(Prefix.GAMEDESIGN + "Haus gefixt.");
            }
        }
        if (registeredBlock.getInfo().equalsIgnoreCase("house")) {
            House house = Main.getInstance().houseManager.getHouse(Integer.parseInt(registeredBlock.getInfoValue()));
            if (house.getOwner() == null) {
                TileState state = (TileState) block.getState();
                if (state instanceof Sign) {
                    Sign sign = (Sign) state;
                    sign.setEditable(false);
                    sign.setLine(1, "== §6Haus " + house.getNumber() + " §0==");
                    sign.setLine(2, "§2Zu Verkaufen");
                    sign.update();
                    player.sendMessage(Prefix.GAMEDESIGN + "Haus gefixt.");
                }
            } else {
                TileState state = (TileState) block.getState();
                OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(house.getOwner());
                if (offlinePlayer == null) return false;
                if (offlinePlayer.getName() == null) return false;
                if (state instanceof Sign) {
                    Sign sign = (Sign) state;
                    sign.setEditable(false);
                    sign.setLine(1, "== §6Haus " + house.getNumber() + " §0==");
                    sign.setLine(2, offlinePlayer.getName());
                    sign.update();
                    player.sendMessage(Prefix.GAMEDESIGN + "Haus gefixt.");
                }
            }
        }
        return false;
    }
}
