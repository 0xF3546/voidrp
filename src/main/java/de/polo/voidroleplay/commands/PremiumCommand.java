package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
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
        if (true) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du kannst aktuell deine Belohnung nicht abholen."));
            return;
        }
        Main.getInstance().getWeaponManager().giveWeaponToCabinet(player, Weapon.MARKSMAN, 20, 1);
        playerData.addBankMoney(1000, "Premium-Belohnung");
        Main.getInstance().playerManager.addEXPBoost(player, 1);
    }
}
