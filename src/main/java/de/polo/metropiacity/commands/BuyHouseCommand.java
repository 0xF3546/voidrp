package de.polo.metropiacity.commands;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.metropiacity.dataStorage.HouseData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.Game.Housing;
import de.polo.metropiacity.utils.PlayerManager;
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

public class BuyHouseCommand implements CommandExecutor {
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
                                            int houes = 0;
                                            for (HouseData houseData1 : Housing.houseDataMap.values()) {
                                                if (houseData1.getOwner() != null) {
                                                    if (houseData1.getOwner().equals(player.getUniqueId().toString())) {
                                                        houes++;
                                                    }
                                                }
                                            }
                                            try {
                                                if (playerData.getHouseSlot() > houes) {
                                                    PlayerManager.removeMoney(player, houseData.getPrice(), "Hauskauf " + houseData.getNumber());
                                                    Statement statement = Main.getInstance().mySQL.getStatement();
                                                    statement.executeUpdate("UPDATE `housing` SET `owner` = '" + player.getUniqueId() + "' WHERE `number` = " + houseData.getNumber());
                                                    houseData.setOwner(player.getUniqueId().toString());
                                                    sign.setLine(2, "§0" + player.getName());
                                                    sign.update();
                                                    player.sendMessage("§8[§6Haus§8]§a Du hast Haus " + houseData.getNumber() + " für " + houseData.getPrice() + "$ gekauft!");
                                                } else {
                                                    player.sendMessage(Main.error + "Du hast nicht genug Haus-Slots.");
                                                }
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
