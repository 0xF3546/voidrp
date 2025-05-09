package de.polo.core.commands;

import de.polo.api.player.VoidPlayer;
import de.polo.api.player.enums.License;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "licenses")
public class LicensesCommand extends CommandBase {
    public LicensesCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        player.sendMessage(Component.text("§7   ===§8[§6Lizenzen§8]§7==="));
        for (License license : License.values()) {
            player.sendMessage(Component.text("§8 ➥ §e" + license.getName() + "§8: " + (playerData.hasLicense(license) ? "§aVorhanden" : "§cNicht Vorhanden")));
        }
    }
}
