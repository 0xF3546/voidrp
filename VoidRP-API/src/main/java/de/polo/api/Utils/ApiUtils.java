package de.polo.api.Utils;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class ApiUtils {
    private static final Map<String, NamedTextColor> COLOR_CODE_MAP = Map.ofEntries(
            Map.entry("0", BLACK),
            Map.entry("1", DARK_BLUE),
            Map.entry("2", DARK_GREEN),
            Map.entry("3", DARK_AQUA),
            Map.entry("4", DARK_RED),
            Map.entry("5", DARK_PURPLE),
            Map.entry("6", GOLD),
            Map.entry("7", GRAY),
            Map.entry("8", DARK_GRAY),
            Map.entry("9", BLUE),
            Map.entry("a", GREEN),
            Map.entry("b", AQUA),
            Map.entry("c", RED),
            Map.entry("d", LIGHT_PURPLE),
            Map.entry("e", YELLOW),
            Map.entry("f", WHITE)
    );

    @Getter
    private static final Color[] colors = new Color[]{
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.ORANGE,
            Color.PURPLE,
            Color.BLACK,
            Color.WHITE,
            Color.GRAY,
            Color.LIME,
            Color.AQUA,
            Color.FUCHSIA
    };

    public static Color getColorFromString(String color) {
        return switch (color.toLowerCase()) {
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "green" -> Color.GREEN;
            case "yellow" -> Color.YELLOW;
            case "orange" -> Color.ORANGE;
            case "purple" -> Color.PURPLE;
            case "black" -> Color.BLACK;
            case "white" -> Color.WHITE;
            case "gray" -> Color.GRAY;
            case "lime" -> Color.LIME;
            case "aqua" -> Color.AQUA;
            case "fuchsia" -> Color.FUCHSIA;
            default -> null;
        };
    }

    public static String getColorString(Color color) {
        if (color == null) return "§f"; // fallback

        if (color.equals(Color.RED)) return "red";
        if (color.equals(Color.BLUE)) return "blue";
        if (color.equals(Color.GREEN)) return "green";
        if (color.equals(Color.YELLOW)) return "yellow";
        if (color.equals(Color.ORANGE)) return "orange";
        if (color.equals(Color.PURPLE)) return "purple";
        if (color.equals(Color.BLACK)) return "black";
        if (color.equals(Color.WHITE)) return "white";
        if (color.equals(Color.GRAY)) return "gray";
        if (color.equals(Color.LIME)) return "lime";
        if (color.equals(Color.AQUA)) return "aqua";
        if (color.equals(Color.FUCHSIA)) return "fuchsia";

        return null;
    }


    public static NamedTextColor getColorFromCode(String code) {
        return COLOR_CODE_MAP.getOrDefault(code.toLowerCase(), GRAY);
    }
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

    public static int getMatchingInventorySize(int entries) {
        if (entries > 54) {
            return 54;
        }
        int size = ((entries + 8) / 9) * 9;
        return Math.min(size, 54);
    }

    public static LocalDateTime getTime() {
        return LocalDateTime.now(ZoneId.of("Europe/Berlin"));
    }

}
