package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class setblockvalueCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() >= 90) {
            Block block = player.getTargetBlock(null, 10);
            if (block.getType() == Material.AIR) {
                player.sendMessage(Main.gamedesign_prefix + "Nur AIR gefunden.");
            } else {
                if (block.getType().isSolid()) {
                    if (args.length >= 1) {
                        if (!(block.getState() instanceof TileState)) return false;
                        NamespacedKey value = new NamespacedKey(Main.plugin, "value");
                        BlockData data = block.getBlockData();
                        TileState state = (TileState) block.getState();
                            PersistentDataContainer container = state.getPersistentDataContainer();
                            container.set(value, PersistentDataType.INTEGER, Integer.valueOf(args[1]));
                            block.setBlockData(data, true);
                            player.sendMessage(Main.gamedesign_prefix + "value " + args[1] + " auf block " + block.getType() + " gesetzt.");
                    } else {
                        player.sendMessage(Main.gamedesign_prefix + "Syntax-Fehler: /setblockvalue [Wert]");
                    }
                } else {
                    player.sendMessage(Main.gamedesign_prefix + "Block ist nicht solide.");
                }
            }
        }
        return false;
    }
}
