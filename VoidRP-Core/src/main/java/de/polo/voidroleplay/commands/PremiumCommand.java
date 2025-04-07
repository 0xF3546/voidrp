package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.Weapon;
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

        Main.getInstance().getWeaponManager().giveWeaponToCabinet(player.getPlayer(), Weapon.MARKSMAN, 20, 1);
        playerData.addBankMoney(1000, "Premium-Belohnung");
        Main.getInstance().playerManager.addEXPBoost(player.getPlayer(), 1);

        playerData.setLastPremiumBonus(Utils.getTime());
        Main.getInstance().getCoreDatabase().updateAsync(
                "UPDATE players SET lastPremiumBonus = ? WHERE uuid = ?",
                Utils.getTime().toString(),
                player.getUuid().toString()
        );

        player.sendMessage(Component.text(Prefix.MAIN + "Du hast deinen Bonus erfolgreich abgeholt."));
    }

}
