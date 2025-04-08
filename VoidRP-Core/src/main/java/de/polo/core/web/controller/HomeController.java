package de.polo.core.web.controller;

import de.polo.core.database.utility.Result;
import de.polo.core.player.services.PlayerService;
import de.polo.core.web.dto.StatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

import static de.polo.core.Main.database;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
public class HomeController {
    private final PlayerService playerService;
    @Autowired
    public HomeController(final PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/statistics")
    public CompletableFuture<ResponseEntity<StatsDto>> home() {
        CompletableFuture<Result> playerCountFuture = database.queryThreaded("SELECT COUNT(*) FROM players");
        CompletableFuture<Result> companyCountFuture = database.queryThreaded("SELECT COUNT(*) FROM companies");
        CompletableFuture<Result> factionCountFuture = database.queryThreaded("SELECT COUNT(*) FROM factions");

        return CompletableFuture.allOf(playerCountFuture, companyCountFuture, factionCountFuture)
                .thenApply(v -> {
                    try {
                        int playerCount = extractCount(playerCountFuture.get());
                        int companyCount = extractCount(companyCountFuture.get());
                        int factionCount = extractCount(factionCountFuture.get());

                        StatsDto statsDto = new StatsDto(factionCount, companyCount, playerCount);
                        return ResponseEntity.ok(statsDto);
                    } catch (Exception e) {
                        throw new RuntimeException("Error fetching statistics", e);
                    }
                });
    }

    private int extractCount(Result result) {
        try {
            ResultSet resultSet = result.resultSet();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            result.close();
        }
    }
}
