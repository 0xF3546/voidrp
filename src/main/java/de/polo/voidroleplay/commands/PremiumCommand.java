package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.Weapon;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
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
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (playerData.getLastPremiumBonus() != null
                && playerData.getLastPremiumBonus().plusWeeks(1).isAfter(Utils.getTime())) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du kannst deine Belohnung erst nächste Woche wieder abholen."));
            return;
        }

        Main.getInstance().getWeaponManager().giveWeaponToCabinet(player, Weapon.MARKSMAN, 20, 1);
        playerData.addBankMoney(1000, "Premium-Belohnung");
        Main.getInstance().playerManager.addEXPBoost(player, 1);

        playerData.setLastPremiumBonus(Utils.getTime());
        Main.getInstance().getMySQL().updateAsync(
                "UPDATE players SET lastPremiumBonus = ? WHERE uuid = ?",
                Utils.getTime().toString(),
                player.getUniqueId().toString()
        );

        player.sendMessage(Component.text(Prefix.MAIN + "Du hast deinen Bonus erfolgreich abgeholt."));
    }

}
