package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.handler.CommandBase;
import de.polo.api.player.VoidPlayer;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.Weapon;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@CommandBase.CommandMeta(name = "premium")
public class PremiumCommand extends CommandBase {
    public PremiumCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (playerData.getLastPremiumBonus() != null
                && playerData.getLastPremiumBonus().plusWeeks(1).isAfter(Utils.getTime())) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du kannst deine Belohnung erst nächste Woche wieder abholen."));
            return;
        }

        Main.getWeaponManager().giveWeaponToCabinet(player.getPlayer(), Weapon.MARKSMAN, 20, 1);
        playerData.addBankMoney(1000, "Premium-Belohnung");
        Main.playerManager.addEXPBoost(player.getPlayer(), 1);

        playerData.setLastPremiumBonus(Utils.getTime());
        Main.getInstance().getCoreDatabase().updateAsync(
                "UPDATE players SET lastPremiumBonus = ? WHERE uuid = ?",
                Utils.getTime().toString(),
                player.getUuid().toString()
        );

        player.sendMessage(Component.text(Prefix.MAIN + "Du hast deinen Bonus erfolgreich abgeholt."));
    }

}
