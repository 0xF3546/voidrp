package de.polo.void_roleplay.PlayerUtils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {
    public static  void clickSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_STONECUTTER_SELECT_RECIPE, 1, 0);
    }
    public static void successSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
    }
}
