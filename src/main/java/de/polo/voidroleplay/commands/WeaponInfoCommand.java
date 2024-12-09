package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.PlayerWeapon;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.enums.Weapon;
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
        for (PlayerWeapon playerWeapon : playerData.getWeapons()) {
            Weapon weapon = playerWeapon.getWeapon();
            player.sendMessage("§8 - §b" + weapon.getClearName() + "§8:§7 A: " + playerWeapon.getAmmo() + " V: " + playerWeapon.getWear());
        }
        return false;
    }
}
