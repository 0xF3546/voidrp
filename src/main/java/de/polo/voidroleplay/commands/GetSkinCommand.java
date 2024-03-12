package de.polo.voidroleplay.commands;
/*
import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;

public class GetSkinCommand implements CommandExecutor {
    private PlayerManager playerManager;
    public GetSkinCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 60) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /getskin [Spieler]");
            return false;
        }
        if (args.length == 1) {
            OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
            if (offlinePlayer == null) {
                player.sendMessage(Main.error + "Der Spieler konnte nicht gefunden werden.");
                return false;
            }
            PlayerProfile playerProfile = player.getPlayerProfile();
            PlayerProfile targetplayerProfile = offlinePlayer.getPlayerProfile();
            playerProfile.getTextures().setSkin(targetplayerProfile.getTextures().getSkin(), targetplayerProfile.getTextures().getSkinModel());
            playerProfile.update();
            player.sendMessage(Main.admin_prefix + "Du hast nun den Skin von " + offlinePlayer.getName() + " an.");
        }
        return false;
    }
}
*/