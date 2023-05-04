package de.polo.void_roleplay.commands;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
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
                    if (args.length >= 2) {
                        if (args[0].equalsIgnoreCase("int")) {
                            NamespacedKey value = new NamespacedKey(Main.plugin, "value");
                            if (!(block.getState() instanceof TileState)) {
                                player.sendMessage(Main.gamedesign_prefix + "Fehler. TileState nicht gefunden.");
                                BlockState state = block.getState();
                                if (state.getType().name().endsWith("_DOOR")) {
                                    Door door = (Door) block.getBlockData();
                                    PersistentDataContainer container = new CustomBlockData(block, Main.plugin);
                                    container.set(value, PersistentDataType.INTEGER, Integer.valueOf(args[1]));
                                    player.sendMessage(Main.gamedesign_prefix + "Door gesettet.");
                                    state.update();
                                } else {
                                    player.sendMessage(Main.gamedesign_prefix + "Door konnte leider nicht gesettet werden.");
                                }
                                return false;
                            }
                            BlockData data = block.getBlockData();
                            TileState state = (TileState) block.getState();
                            PersistentDataContainer container = new CustomBlockData(block, Main.plugin);
                            container.set(value, PersistentDataType.INTEGER, Integer.valueOf(args[1]));
                            block.setBlockData(data, true);
                            player.sendMessage(Main.gamedesign_prefix + "value " + args[1] + " auf block " + block.getType().name() + " gesetzt.");
                            state.update();
                            if (state instanceof Sign) {
                                Sign sign = (Sign) state;
                                sign.setEditable(false);
                                sign.setLine(1, "== §6Haus " + args[1] + " §0==");
                                sign.setLine(2, "§2Zu Verkaufen");
                                sign.update();
                            }
                        } else if (args[0].equalsIgnoreCase("string")) {
                            NamespacedKey value = new NamespacedKey(Main.plugin, "value");
                            if (!(block.getState() instanceof TileState)) {
                                player.sendMessage(Main.gamedesign_prefix + "Fehler. TileState nicht gefunden.");
                                BlockState state = block.getState();
                                if (state.getType().name().endsWith("_DOOR")) {
                                    Door door = (Door) block.getBlockData();
                                    PersistentDataContainer container = new CustomBlockData(block, Main.plugin);
                                    container.set(value, PersistentDataType.STRING, args[1]);
                                    player.sendMessage(Main.gamedesign_prefix + "Door gesettet.");
                                    state.update();
                                } else {
                                    player.sendMessage(Main.gamedesign_prefix + "Door konnte leider nicht gesettet werden.");
                                }
                                return false;
                            }
                            BlockData data = block.getBlockData();
                            TileState state = (TileState) block.getState();
                            PersistentDataContainer container = new CustomBlockData(block, Main.plugin);
                            container.set(value, PersistentDataType.STRING, args[1]);
                            block.setBlockData(data, true);
                            player.sendMessage(Main.gamedesign_prefix + "value " + args[1] + " auf block " + block.getType().name() + " gesetzt.");
                            state.update();
                        } else {
                            player.sendMessage(Main.gamedesign_prefix + "Syntax-Fehler: /setblockvalue [string/int] [Wert]");
                        }
                    } else {
                        player.sendMessage(Main.gamedesign_prefix + "Syntax-Fehler: /setblockvalue [string/int] [Wert]");
                    }
                } else {
                    player.sendMessage(Main.gamedesign_prefix + "Block ist nicht solide.");
                }
            }
        }
        return false;
    }
}
