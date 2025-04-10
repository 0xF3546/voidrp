package de.polo.api.Utils;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class ApiUtils {
    public static String getProgressBar(int current, int max, int totalBars) {
        int progress = (int) ((double) current / max * totalBars);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < totalBars; i++) {
            bar.append(i < progress ? "§3█" : "§7█");
        }
        return bar.toString();
    }

    public static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String colorToTextColor(Color color) {
        return TextColor.color(color.getRed(), color.getGreen(), color.getBlue()).toString();
    }

    public static String colorToLegacyCode(Color color) {
        if (color == null) return "§f"; // fallback

        if (color.equals(Color.BLACK)) return "§0";
        if (color.equals(Color.NAVY)) return "§1";          // Dark Blue
        if (color.equals(Color.GREEN)) return "§2";         // Dark Green
        if (color.equals(Color.TEAL)) return "§3";          // Dark Aqua
        if (color.equals(Color.MAROON)) return "§4";        // Dark Red
        if (color.equals(Color.PURPLE)) return "§5";        // Dark Purple
        if (color.equals(Color.ORANGE)) return "§6";        // Gold
        if (color.equals(Color.SILVER)) return "§7";          // Gray
        if (color.equals(Color.GRAY)) return "§8";     // Optional custom if defined
        if (color.equals(Color.BLUE)) return "§9";          // Blue
        if (color.equals(Color.LIME)) return "§a";          // Green
        if (color.equals(Color.AQUA)) return "§b";          // Aqua
        if (color.equals(Color.RED)) return "§c";           // Red
        if (color.equals(Color.FUCHSIA)) return "§d";       // Light Purple
        if (color.equals(Color.YELLOW)) return "§e";        // Yellow
        if (color.equals(Color.WHITE)) return "§f";         // White

        if (color.equals(Color.OLIVE)) return "§2";

        return "§f";
    }
}
