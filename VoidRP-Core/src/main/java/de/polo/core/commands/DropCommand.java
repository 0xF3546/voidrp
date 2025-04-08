package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static de.polo.core.Main.locationManager;

public class DropCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public DropCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("drop", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getVariable("job") != null) {
            switch (playerData.getVariable("job").toString()) {
                case "lieferant":
                    Main.getInstance().commands.lebensmittelLieferantCommand.dropLieferung(player);
                    break;
                case "weizenlieferant":
                    Main.getInstance().commands.farmerCommand.dropTransport(player);
                    break;
                case "pfeifentransport":
                    Main.getInstance().commands.pfeifenTransport.dropTransport(player);
                    break;
                case "equip":
                    handleEquipDrop(player);
                    break;
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Du hast keinen Job angenommen.");
        }
        return false;
    }

    private void handleEquipDrop(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) return;
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
