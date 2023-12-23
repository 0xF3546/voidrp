package de.polo.metropiacity.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Color;

@Getter
@AllArgsConstructor
public enum EXPType {
    LEVEL("Level", SkillType.NULL, Color.GREEN, -1),
    SKILL_FISHING("Fishing", SkillType.FISHING, Color.BLUE, 3120);

    private final String displayName;
    private final SkillType skillType;
    private final Color color;
    private final int LevelUpXp;
}