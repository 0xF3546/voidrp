package de.polo.core.faction.commands;

import de.polo.api.VoidAPI;
import de.polo.api.jobs.TransportJob;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static de.polo.core.Main.*;

public class EquipTransportCommand implements CommandExecutor, TransportJob {
    private final PlayerManager playerManager;

    public EquipTransportCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("equiptransport", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData == null) return false;
        if (playerData.getBargeld() < 12200) {
            player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei. (12.200$)");
            return false;
        }
        startJob(VoidAPI.getPlayer(player));
        return false;
    }

    @Override
    public void startJob(VoidPlayer player) {
        player.setVariable("job", "equip");
        player.getData().removeMoney(12200, "Equip-Transport");
        player.sendMessage(Prefix.MAIN + "Du hast den Transport gestartet, begib dich zu deinem Equip-Punkt und nutze /drop");

    }

    @Override
    public void endJob(VoidPlayer player) {
        playerService.handleJobFinish(player, MiniJob.EQUIP_TRANSPORT, 0, 0);
    }

    @Override
    public void handleDrop(VoidPlayer player) {
        PlayerData playerData = playerManager.getPlayerData(player.getPlayer());
        if (playerData == null) return;
        if (locationManager.getDistanceBetweenCoords(player, "equip_" + playerData.getFaction()) > 5) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du bist nicht in der nähe deiner Fraktion."));
            return;
        }
        Faction factionData = factionManager.getFactionData(playerData.getFaction());
        int amount = Utils.random(100, 150);
        playerData.setVariable("job", null);
        factionData.setEquipPoints(factionData.getEquipPoints() + amount);
        factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§6Equip§8]§7 " + player.getName() + " hat das Lager aufgefüllt. (§6+" + amount + "§7, L: §6" + factionData.getEquipPoints() + "§7)");
        factionData.save();
    }
}
