package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.VoidAPI;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.game.base.housing.HouseManager;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.Agreement;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.RegisteredBlock;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.jobs.enums.LongTermJob;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static de.polo.voidroleplay.Main.*;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "sellhouse", usage = "/sellhouse [Haus] [Spieler] [Preis]")
public class SellHouseCommand extends CommandBase {
    public SellHouseCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (playerData.getLongTermJob() != LongTermJob.REAL_ESTATE_BROKER) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du bist kein Makler."));
            return;
        }
        if (args.length < 3) {
            showSyntax(player);
            return;
        }
        int houseNumber;
        try {
            houseNumber = Integer.parseInt(args[0]);
        } catch (Exception ex) {
            player.sendMessage(Component.text(Prefix.ERROR + "Die Hausnummer muss numerisch sein."));
            return;
        }
        House house = houseManager.getHouse(houseNumber);
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Spieler wurde nicht gefunden."));
            return;
        }
        if (player.getLocation().distance(target.getLocation()) > 5) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Spieler ist nicht in der nähe"));
            return;
        }
        int price;
        try {
            price = Integer.parseInt(args[2]);
        } catch (Exception ex) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Hauspreis muss numerisch sein."));
            return;
        }
        if (price < house.getPrice()) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Preis darf nicht den Mindestpreis des Haus unterschreiten."));
            return;
        }
        Player owner = Bukkit.getPlayer(UUID.fromString(house.getOwner()));
        if (owner == null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Besitzer ist nicht online."));
            return;
        }
        if (player.getLocation().distance(owner.getLocation()) > 5) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Besitzer ist nicht in der nähe"));
            return;
        }
        if (owner == player && target == player) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du kannst dir nichts selbst verkaufen."));
            return;
        }
        PlayerData targetData = playerManager.getPlayerData(target);

        int centerX = player.getLocation().getBlockX();
        int centerY = player.getLocation().getBlockY();
        int centerZ = player.getLocation().getBlockZ();
        World world = player.getLocation().getWorld();
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

                        if (houseNumber == Integer.parseInt(registeredBlock.getInfoValue())) {
                            if (house.getOwner() == null) {
                                player.sendMessage(Component.text(Prefix.ERROR + "Dieses Haus gehört niemandem."));
                                return;
                            }
                            int houes = 0;
                            for (House houseData1 : HouseManager.houseDataMap.values()) {
                                if (houseData1.getOwner() != null) {
                                    if (houseData1.getOwner().equals(target.getUniqueId().toString())) {
                                        houes++;
                                    }
                                }
                            }
                            if (targetData.getHouseSlot() > houes) {
                                owner.sendMessage(Component.text("§8[§6Haus " + houseNumber + "§8]§7 Makler " + player.getName() + " möchte dein Haus für " + Utils.toDecimalFormat(price) + "$ an " + target.getName() + " verkaufen."));
                                utils.vertragUtil.sendInfoMessage(owner);
                                target.sendMessage(Component.text("§8[§6Haus " + houseNumber + "§8]§7 Makler " + player.getName() + " ein Haus von " + target.getName() + " für " + Utils.toDecimalFormat(price) + "$ an dich verkaufen."));
                                Agreement agreement = new Agreement(VoidAPI.getPlayer(target), VoidAPI.getPlayer(owner), "housebuy",
                                        () -> {
                                            PlayerData ownerData = playerManager.getPlayerData(owner);
                                            int maklerPrice = (int) (price * 0.005);
                                            int sellPrice = price - maklerPrice;
                                            targetData.removeMoney(price, "Hauskauf " + houseNumber);
                                            ownerData.addMoney(sellPrice, "Hauskauf " + houseNumber);
                                            playerData.addMoney(maklerPrice, "Provision " + houseNumber);
                                            player.sendMessage(Component.text("§8[§6Haus " + houseNumber + "§8]§7 Du erhälst " + Utils.toDecimalFormat(maklerPrice) + "$ Provision."));
                                            target.sendMessage(Component.text("§8[§6Haus " + houseNumber + "§8]§7 Du hast das Haus für " + Utils.toDecimalFormat(price) + "$ erworben."));
                                            owner.sendMessage(Component.text("§8[§6Haus " + houseNumber + "§8]§7 Du hast das Haus für " + Utils.toDecimalFormat(sellPrice) + "$ (" + Utils.toDecimalFormat(maklerPrice) + "$ Makler-Provision) verkauft."));

                                            Main.getInstance().getCoreDatabase().updateAsync("UPDATE housing SET owner = ? WHERE number = ?", target.getUniqueId().toString(), houseNumber);
                                            house.setOwner(target.getUniqueId().toString());
                                            sign.setLine(2, "§0" + target.getName());
                                            sign.update();
                                        },
                                        () -> {
                                            player.sendMessage(Component.text("§8[§6Haus " + houseNumber + "§8]§7 " + target.getName() + " hat den Antrag abgelehnt."));
                                            target.sendMessage(Component.text("§8[§6Haus " + houseNumber + "§8]§7 Du hast den Antrag abgelehnt."));
                                            owner.sendMessage(Component.text("§8[§6Haus " + houseNumber + "§8]§7 " + target.getName() + " hat den Antrag abgelehnt."));
                                        });
                                utils.vertragUtil.setAgreement(target, owner, agreement);
                            } else {
                                player.sendMessage(Prefix.ERROR + "Du hast nicht genug Haus-Slots.");
                            }
                            return;
                        }
                    }
                }
            }
        }
        player.sendMessage(Component.text(Prefix.ERROR + "Du bist nicht in der nähe des Hauses."));
    }
}
