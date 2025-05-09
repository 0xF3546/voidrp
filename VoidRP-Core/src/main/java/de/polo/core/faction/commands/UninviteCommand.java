package de.polo.core.faction.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class UninviteCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public UninviteCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("uninvite", this);
        Main.addTabCompleter("uninvite", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!playerData.isLeader()) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (!(args.length >= 1)) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /uninvite [Spieler]");
            return false;
        }
        OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
        if (offlinePlayer == null) {
            player.sendMessage(Prefix.ERROR + "Spieler wurde nicht gefunden!");
            return false;
        }
        PlayerData targetData = factionManager.getFactionOfPlayer(offlinePlayer.getUniqueId());
        if (!playerData.getFaction().equalsIgnoreCase(targetData.getFaction())) {
            player.sendMessage(Prefix.ERROR + offlinePlayer.getName() + " ist nicht in deiner Fraktion.");
            return false;
        }
        if (targetData.getFactionGrade() >= playerData.getFactionGrade()) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }

        AdminService adminService = VoidAPI.getService(AdminService.class);
        adminService.sendMessage(player.getName() + " hat " + offlinePlayer.getName() + " aus der Fraktion \"" + targetData.getFaction() + "\" geworfen.", Color.PURPLE);
        factionManager.removePlayerFromFrak(offlinePlayer.getUniqueId());
        factionManager.sendMessageToFaction(playerData.getFaction(), player.getName() + " hat " + offlinePlayer.getName() + " aus der Fraktion geworfen!");
        if (offlinePlayer.isOnline()) {
            Player target = Bukkit.getPlayer(offlinePlayer.getUniqueId());
            if (target == null) return false;
            target.sendMessage("§8 » §7Du wurdest von " + player.getName() + " aus der Fraktion geworfen!");
        }
        return false;
    }

    @SneakyThrows
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            PlayerData playerData = playerManager.getPlayerData((Player) sender);
            Connection connection = Main.getInstance().coreDatabase.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT player_name FROM players WHERE faction = ?");
            statement.setString(1, playerData.getFaction());
            ResultSet result = statement.executeQuery();
            List<String> names = new ObjectArrayList<>();
            while (result.next()) {
                names.add(result.getString("player_name"));
            }
            connection.close();
            statement.close();
            result.close();
            return names;
        }
        return null;
    }
}
