package de.polo.metropiacity.commands;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.dataStorage.RegisteredBlock;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.BlockManager;
import de.polo.metropiacity.utils.PlayerManager;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class RegisterblockCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final MySQL mySQL;
    private final BlockManager blockManager;
    public RegisterblockCommand(PlayerManager playerManager, MySQL mySQL, BlockManager blockManager) {
        this.playerManager = playerManager;
        this.mySQL = mySQL;
        this.blockManager = blockManager;
        Main.registerCommand("registerblock", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 90) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }

        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /registerblock [Typ] [Extra]");
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
            if (state instanceof Sign) {
                Sign sign = (Sign) state;
                sign.setEditable(false);
                sign.setLine(1, "== §6Haus " + args[0] + " §0==");
                sign.setLine(2, "§2Zu Verkaufen");
                sign.update();
                registeredBlock.setInfo("house");
                registeredBlock.setInfoValue(args[0]);
                player.sendMessage(Main.gamedesign_prefix + "Haus regestriert.");
            }
        } catch (Exception e) {
        }

        int id = blockManager.addBlock(registeredBlock);

        player.sendMessage(Main.prefix + "Du hast ein/e/n " + block.getType() + " registriert als " + args[0] + " (#" + id + ").");

        return false;
    }
}
