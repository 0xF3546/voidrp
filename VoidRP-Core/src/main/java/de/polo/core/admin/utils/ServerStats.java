package de.polo.core.admin.utils;

import de.polo.core.utils.GlobalStats;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public final class ServerStats {
    @Getter
    @Setter
    private static LocalDateTime startTime;

    @Getter
    private static int peakPlayers;

    public static void setPeakPlayers(int players) {
        peakPlayers = players;
        int realPeak = Integer.parseInt(GlobalStats.getValue("peakPlayers"));
        if (players > realPeak) {
            GlobalStats.setValue("peakPlayers", String.valueOf(players), true);
        }
    }
}
