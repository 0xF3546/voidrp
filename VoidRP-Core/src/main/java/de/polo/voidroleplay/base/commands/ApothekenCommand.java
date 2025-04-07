package de.polo.voidroleplay.base.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.faction.entity.Faction;
import de.polo.voidroleplay.game.faction.apotheke.Apotheke;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.gameplay.GamePlay;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ApothekenCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final GamePlay gamePlay;
    private final FactionManager factionManager;

    public ApothekenCommand(PlayerManager playerManager, GamePlay gamePlay, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.gamePlay = gamePlay;
        this.factionManager = factionManager;
        Main.registerCommand("apotheken", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        player.sendMessage("   §7===§8[§cApotheken§8]§7===");
        for (Apotheke apotheke : gamePlay.apotheke.getApotheken()) {
            if (apotheke.getOwner().equalsIgnoreCase("staat")) {
                player.sendMessage("§8 ➥ §eApotheke-" + apotheke.getId() + "§8 | §9Staat");
            } else {
                Faction factionData = factionManager.getFactionData(apotheke.getOwner());
                String attackable = "";
                long minutesDifference = gamePlay.apotheke.getMinuteDifference(apotheke);
                if (minutesDifference < 60 && minutesDifference >= 0) {
                    attackable = "§8 - §c" + minutesDifference + "min";
                }
                if (apotheke.isStaat()) {
                    player.sendMessage("§8 ➥ §eApotheke-" + apotheke.getId() + "§8 | §" + factionData.getPrimaryColor() + factionData.getFullname() + " §8[§9Staatsschutz§8]" + attackable);
                } else {
                    player.sendMessage("§8 ➥ §eApotheke-" + apotheke.getId() + "§8 | §" + factionData.getPrimaryColor() + factionData.getFullname() + attackable);
                }
            }
        }
        return false;
    }
}
