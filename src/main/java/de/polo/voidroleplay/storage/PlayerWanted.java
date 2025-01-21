package de.polo.voidroleplay.storage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.enums.WantedVariation;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerWanted {
    private static final Gson GSON = new Gson();

    @Getter
    private final int wantedId;
    @Getter
    private List<WantedVariation> variations;
    @Getter
    private final UUID issuer;
    @Getter
    private final LocalDateTime issued;
    @Getter
    @Setter
    private int id;

    public PlayerWanted(int id, int wantedId, UUID issuer, LocalDateTime issued, String variationsJson) {
        this.id = id;
        this.wantedId = wantedId;
        this.issuer = issuer;
        this.issued = issued;

        try {
            this.variations = GSON.fromJson(variationsJson, new TypeToken<List<WantedVariation>>() {}.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            this.variations = new ArrayList<>();
        }
    }

    public PlayerWanted(int wantedId, UUID issuer, LocalDateTime issued, List<WantedVariation> variations) {
        this.wantedId = wantedId;
        this.issuer = issuer;
        this.issued = issued;
        this.variations = variations;
    }

    public PlayerWanted(int id, int wantedId, UUID issuer, LocalDateTime issued, List<WantedVariation> variations) {
        this.id = id;
        this.wantedId = wantedId;
        this.issuer = issuer;
        this.issued = issued;
        this.variations = variations;
    }

    public String getVariationsAsJson() {
        return GSON.toJson(this.variations);
    }

    public void addVariationAsync(WantedVariation wantedVariation) {
        this.variations.add(wantedVariation);
        String updateQuery = "UPDATE player_wanteds SET variations = ? WHERE id = ?";

        Main.getInstance().getMySQL()
                .queryThreaded(updateQuery, getVariationsAsJson(), this.id);
    }

    public boolean hasVariation(WantedVariation wantedVariation) {
        return variations.contains(wantedVariation);
    }

    public Collection<WantedVariation> getVariations() {
        return variations;
    }
}
