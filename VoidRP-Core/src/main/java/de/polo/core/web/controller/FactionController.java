package de.polo.core.web.controller;

import de.polo.api.VoidAPI;
import de.polo.core.VoidSpringApplication;
import de.polo.core.faction.entity.Faction;
import de.polo.core.faction.service.FactionService;
import de.polo.core.web.dto.FactionDto;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class FactionController {
    @GetMapping("/")
    public CompletableFuture<ResponseEntity<List<FactionDto>>> getFactions() {
        return CompletableFuture.supplyAsync(() -> {
            FactionService factionService = VoidAPI.getService(FactionService.class);
            List<FactionDto> factions = new ObjectArrayList<>();
            factionService.getFactions().stream().map(faction -> {
                return new FactionDto(faction.getId(), faction.getName(), faction.getFullname());
            }).forEach(factions::add);
            return ResponseEntity.ok(factions);
        });
    }

    @GetMapping("/:faction")
    public CompletableFuture<ResponseEntity<FactionDto>> getFactions(String faction) {
        return CompletableFuture.supplyAsync(() -> {
            FactionService factionService = VoidAPI.getService(FactionService.class);
            Faction f = factionService.getFactions().stream().filter(x -> x.getName().equalsIgnoreCase(faction)).findFirst().orElse(null);
            if (f == null) {
                return ResponseEntity.notFound().build();
            }
            FactionDto factionDto = new FactionDto(f.getId(), f.getName(), f.getFullname());
            return ResponseEntity.ok(factionDto);
        });
    }
}
