package de.polo.api.Utils;

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
}
