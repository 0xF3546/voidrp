package de.polo.core.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@AllArgsConstructor
@Getter
public enum FFALobbyType {
    BASIC("§eJeder gegen Jeden", false),
    RANKED_1v1("§8[§cRanked§8]§e1vs1", true),
    RANKED_2v2("§8[§cRanked§8]§e2vs2", true),
    RANKED_3v3("§8[§cRanked§8]§e3vs3", true);

    private final String displayName;
    private final boolean isRanked;
}
