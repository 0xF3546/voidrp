package de.polo.core.crew.entities;

import de.polo.api.crew.CrewRank;
import de.polo.api.crew.enums.CrewPermission;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreCrewRank implements CrewRank {
    @Getter
    private final int id;
    @Getter
    private final int crewId;
    @Getter
    private final TextColor color;
    @Getter
    private final boolean isDefault;
    @Getter
    private final boolean isBoss;
    @Getter
    private final List<CrewPermission> permissions;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private int rank;

    public CoreCrewRank(int id, String name, int crewId, TextColor color, int rank, boolean isDefault, boolean isBoss, List<CrewPermission> permissions) {
        this.id = id;
        this.name = name;
        this.crewId = crewId;
        this.color = color;
        this.rank = rank;
        this.isDefault = isDefault;
        this.isBoss = isBoss;
        this.permissions = permissions;
    }

    @Override
    public void addPermission(CrewPermission permission) {
        permissions.add(permission);
    }

    @Override
    public void removePermission(CrewPermission permission) {
        permissions.remove(permission);
    }

    @Override
    public boolean hasPermission(CrewPermission permission) {
        return permissions.contains(permission) || permissions.contains(CrewPermission.BOSS);
    }
}
