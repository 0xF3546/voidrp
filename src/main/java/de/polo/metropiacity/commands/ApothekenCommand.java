package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.Apotheke;
import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.GamePlay.GamePlay;
import de.polo.metropiacity.utils.PlayerManager;
import jdk.vm.ci.meta.Local;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;

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
                FactionData factionData = factionManager.getFactionData(apotheke.getOwner());
                LocalDateTime now = LocalDateTime.now();
                Duration duration = Duration.between(apotheke.getLastAttack(), now);
                long minutesDifference = duration.toMinutes();
                String attackable = "";
                if (minutesDifference < 60) {
                    attackable = "§8 - §c" + minutesDifference + "min";
                }
                if (apotheke.isStaat()) {
                    player.sendMessage("§8 ➥ §eApotheke-" + apotheke.getId() + "§8 | §" + factionData.getPrimaryColor() + factionData.getName() + " §8[§9Staatsschutz§8]" + attackable);
                } else {
                    player.sendMessage("§8 ➥ §eApotheke-" + apotheke.getId() + "§8 | §" + factionData.getPrimaryColor() + factionData.getName() + attackable);
                }
            }
        }
        return false;
    }
}
