package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.storage.LoyaltyBonusTimer;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

@CommandBase.CommandMeta(name = "treuebonus")
public class TreuebonusCommand extends CommandBase {
    public TreuebonusCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        LoyaltyBonusTimer timer = Main.getInstance().getPlayerManager().getLoyaltyTimer(player.getUniqueId());
        long diff = Duration.between(timer.getStarted(), Utils.getTime()).toMinutes();
        diff = 120 - diff;
        player.sendMessage(Component.text("§8[§3Treuebonus§8]§b Du erhälst in " + diff + " deinen Treuebonus."));
    }
}
