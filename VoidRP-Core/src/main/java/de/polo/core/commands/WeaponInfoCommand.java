package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.PlayerWeapon;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.enums.Weapon;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WeaponInfoCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public WeaponInfoCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("weaponinfo", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        player.sendMessage(Component.text("§7   ===§8[§2Waffenschrank§8]===§7"));
        for (PlayerWeapon playerWeapon : playerData.getWeapons()) {
            Weapon weapon = playerWeapon.getWeapon();
            player.sendMessage("§8 - §b" + weapon.getClearName() + "§8:§7 A: " + playerWeapon.getAmmo() + " V: " + playerWeapon.getWear());
        }
        return false;
    }
}
