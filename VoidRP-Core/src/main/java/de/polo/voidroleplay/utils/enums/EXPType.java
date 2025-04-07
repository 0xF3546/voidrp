package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@Getter
@AllArgsConstructor
public enum EXPType {
    LEVEL("Level", SkillType.NULL, ChatColor.GREEN, -1),
    SKILL_FISHING("Fishing", SkillType.FISHING, ChatColor.BLUE, 3120),
    SKILL_LUMBERJACK("Holzfäller", SkillType.LUMBERJACK, ChatColor.GRAY, 3120),
    POPULARITY("Popularität", SkillType.POPULARITY, ChatColor.RED, 5000),
    SKILL_MINER("Miner", SkillType.MINER, ChatColor.GRAY, 5000);

    private final String displayName;
    private final SkillType skillType;
    private final ChatColor color;
    private final int LevelUpXp;
}