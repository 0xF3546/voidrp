package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InviteCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;
    public InviteCommand(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        Main.registerCommand("invite", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFaction() == null) {
            player.sendMessage(Main.error + "Du bist in keiner Fraktion.");
            return false;
        }
        String playerfac = factionManager.faction(player);
        FactionData factionData = factionManager.getFactionData(playerfac);
        if (factionManager.faction_grade(player) < 7) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (!(args.length >= 1)) {
            player.sendMessage(Main.error + "Syntax-Fehler: /invite [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Main.error + args[0] + " ist nicht online.");
            return false;
        }
        if (player.getLocation().distance(targetplayer.getLocation()) >= 5) {
            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner nähe.");
            return false;
        }
        PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        if (targetplayerData.getFaction() != null) {
            player.sendMessage("§8[§" + factionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8] §c" + targetplayer.getName() + "§7 ist bereits in einer Fraktion.");
            return false;
        }
        if (factionManager.getMemberCount(playerfac) >= factionData.getMaxMember()) {
            player.sendMessage(Main.error + "Deine Fraktion ist voll!");
            return false;
        }
        if (VertragUtil.setVertrag(player, targetplayer, "faction_invite", playerfac)) {
            player.sendMessage("§8[§" + factionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8] §7" + targetplayer.getName() + " wurde in die Fraktion §aeingeladen§7.");
            targetplayer.sendMessage("§6" + player.getName() + " hat dich in die Fraktion §" + factionManager.getFactionPrimaryColor(playerfac) + factionData.getFullname() + "§6 eingeladen.");
            utils.vertragUtil.sendInfoMessage(targetplayer);
        } else {
            player.sendMessage("§8[§" + factionManager.getFactionPrimaryColor(playerfac) + playerfac + "§8] §7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
        }
        return false;
    }
}
