package de.polo.core.crew.entities;

import de.polo.api.crew.Crew;
import de.polo.api.crew.CrewRank;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreCrew implements Crew {

    @Getter
    private final int id;

    @Getter
    private final String name;

    @Getter
    private final UUID owner;

    @Getter
    private final List<CrewRank> ranks = new ObjectArrayList<>();

    @Setter
    private int defaultGroup;
    @Setter
    private int bossGroup;

    public CoreCrew(final int id, final String name, final UUID owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }

    @Override
    public CrewRank getRank(int grade) {
        return ranks.stream()
                .filter(rank -> rank.getRank() == grade)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addRank(CrewRank rank) {
        ranks.add(rank);
    }

    @Override
    public CrewRank getDefaultRank() {
        return ranks.stream()
                .filter(rank -> rank.getId() == defaultGroup)
                .findFirst()
                .orElse(null);
    }

    @Override
    public CrewRank getBossRank() {
        return ranks.stream()
                .filter(rank -> rank.getId() == bossGroup)
                .findFirst()
                .orElse(null);
    }
}
