package de.polo.void_roleplay.commands;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.void_roleplay.DataStorage.HouseData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.Utils.Housing;
import de.polo.void_roleplay.Utils.LocationManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class buyhouseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            int centerX = player.getLocation().getBlockX();
            int centerY = player.getLocation().getBlockY();
            int centerZ = player.getLocation().getBlockZ();
            World world = player.getWorld();
            for (int x = centerX - 6; x <= centerX + 6; x++) {
                for (int y = centerY - 6; y <= centerY + 6; y++) {
                    for (int z = centerZ - 6; z <= centerZ + 6; z++) {
                        Location location = new Location(world, x, y, z);
                        Block block = location.getBlock();
                        if (block.getType().toString().contains("SIGN")) {
                            Sign sign = (Sign) block.getState();
                            NamespacedKey value = new NamespacedKey(Main.plugin, "value");
                            PersistentDataContainer container = new CustomBlockData(block, Main.plugin);
                            System.out.println(container.get(value, PersistentDataType.INTEGER));
                            if (container.get(value, PersistentDataType.INTEGER) != null) {
                                if (Integer.parseInt(args[0]) == Objects.requireNonNull(container.get(value, PersistentDataType.INTEGER))) {
                                    HouseData houseData = Housing.houseDataMap.get(Integer.parseInt(args[0]));
                                    if (houseData.getOwner() == null) {
                                        if (playerData.getBargeld() >= houseData.getPrice()) {
                                            try {
                                                PlayerManager.removeMoney(player, houseData.getPrice(), "Hauskauf " + houseData.getNumber());
                                                Statement statement = MySQL.getStatement();
                                                statement.executeUpdate("UPDATE `housing` SET `owner` = '" + player.getUniqueId().toString() + "' WHERE `number` = " + houseData.getNumber());
                                                houseData.setOwner(player.getUniqueId().toString());
                                                sign.setLine(2, "§0" + player.getName());
                                                sign.update();
                                                player.sendMessage("§8[§6Haus§8]§a Du hast Haus " + houseData.getNumber() + " für " + houseData.getPrice() + "$ gekauft!");
                                            } catch (SQLException e) {
                                                player.sendMessage(Main.error + "Ein Fehler ist unterlaufen, versuche es später erneut.");
                                                throw new RuntimeException(e);
                                            }
                                        } else {
                                            player.sendMessage(Main.error + "Du hast nicht genug Bargeld (" + houseData.getPrice() + "$).");
                                        }
                                    } else {
                                        player.sendMessage(Main.error + "Dieses Haus ist bereits verkauft.");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /buyhouse [Haus]");
        }
        return false;
    }
}
