package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            player.sendMessage(Main.error + "Du hast keinen Job angenommen.");
        }
        return false;
    }

    private void handleEquipDrop(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        int amount = Main.random(150, 300);
        playerData.setVariable("job", null);
        factionData.setEquipPoints(factionData.getEquipPoints() + amount);
        factionManager.sendCustomMessageToFaction(playerData.getFaction(), "§8[§6Equip§8]§7 " + player.getName() + " hat das Lager aufgefüllt. (§6+" + amount + "§7, L: §6" + factionData.getEquipPoints() + "§7)");
        factionData.save();
    }
}
