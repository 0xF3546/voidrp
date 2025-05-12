package de.polo.api.player;

import de.polo.api.player.enums.WantedVariation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PlayerWanted {

    int getWantedId();
    UUID getIssuer();
    LocalDateTime getIssued();
    List<WantedVariation> getVariations();
    int getId();
    void setId(int id);

    String getVariationsAsJson();
    void addVariationAsync(WantedVariation wantedVariation);
    boolean hasVariation(WantedVariation wantedVariation);
}

