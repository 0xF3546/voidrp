package de.polo.core.crew.entities;

import de.polo.api.crew.Crew;
import de.polo.api.crew.CrewRank;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

import static de.polo.core.Main.database;

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

    @Getter
    private int level = 1;

    @Getter
    private int exp = 0;

    public CoreCrew(final int id, final String name, final UUID owner, final int level, final int exp) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.level = level;
        this.exp = exp;
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

    @Override
    public void addExp(int exp) {
        this.exp += exp;
        if (this.exp >= level * 3500) {
            this.level++;
            this.exp = 0;
        }
        database.updateAsync(
                "UPDATE crews SET level = ?, exp = ? WHERE id = ?",
                this.level,
                this.exp,
                this.id
        );
    }
}
