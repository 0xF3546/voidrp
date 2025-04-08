package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.game.base.housing.House;
import de.polo.core.game.base.housing.HouseManager;
import de.polo.core.manager.BlockManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class BuyHouseCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final BlockManager blockManager;

    public BuyHouseCommand(PlayerManager playerManager, BlockManager blockManager) {
        this.playerManager = playerManager;
        this.blockManager = blockManager;
        Main.registerCommand("buyhouse", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
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
                            RegisteredBlock registeredBlock = blockManager.getBlockAtLocation(location);
                            if (registeredBlock == null) continue;
                            if (registeredBlock.getInfoValue() == null) continue;
                            if (!registeredBlock.getInfo().equalsIgnoreCase("house")) continue;

                            if (Integer.parseInt(args[0]) == Integer.parseInt(registeredBlock.getInfoValue())) {
                                House houseData = HouseManager.houseDataMap.get(Integer.parseInt(args[0]));
                                if (houseData.getOwner() == null) {
                                    if (playerData.getBargeld() >= houseData.getPrice()) {
                                        int houes = 0;
                                        for (House houseData1 : HouseManager.houseDataMap.values()) {
                                            if (houseData1.getOwner() != null) {
                                                if (houseData1.getOwner().equals(player.getUniqueId().toString())) {
                                                    houes++;
                                                }
                                            }
                                        }
                                        try {
                                            if (playerData.getHouseSlot() > houes) {
                                                playerManager.removeMoney(player, houseData.getPrice(), "Hauskauf " + houseData.getNumber());
                                                Main.getInstance().getCoreDatabase().updateAsync("UPDATE housing SET owner = ? WHERE number = ?", player.getUniqueId().toString(), houseData.getNumber());
                                                houseData.setOwner(player.getUniqueId().toString());
                                                sign.setLine(2, "§0" + player.getName());
                                                sign.update();
                                                player.sendMessage("§8[§6Haus§8]§a Du hast Haus " + houseData.getNumber() + " für " + houseData.getPrice() + "$ gekauft!");
                                                Main.getInstance().beginnerpass.didQuest(player, 11);
                                            } else {
                                                player.sendMessage(Prefix.ERROR + "Du hast nicht genug Haus-Slots.");
                                            }
                                        } catch (SQLException e) {
                                            player.sendMessage(Prefix.ERROR + "Ein Fehler ist unterlaufen, versuche es später erneut.");
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        player.sendMessage(Prefix.ERROR + "Du hast nicht genug Bargeld (" + houseData.getPrice() + "$).");
                                    }
                                } else {
                                    player.sendMessage(Prefix.ERROR + "Dieses Haus ist bereits verkauft.");
                                }

                            }
                        }
                    }
                }
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /buyhouse [Haus]");
        }
        return false;
    }
}
