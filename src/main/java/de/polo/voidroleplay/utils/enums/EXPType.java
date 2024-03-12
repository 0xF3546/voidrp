package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@Getter
@AllArgsConstructor
public enum EXPType {
    LEVEL("Level", SkillType.NULL, ChatColor.GREEN, -1),
    SKILL_FISHING("Fishing", SkillType.FISHING, ChatColor.BLUE, 3120);

    private final String displayName;
    private final SkillType skillType;
    private final ChatColor color;
    private final int LevelUpXp;
}