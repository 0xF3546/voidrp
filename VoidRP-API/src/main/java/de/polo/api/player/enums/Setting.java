package de.polo.api.player.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Setting {
    TOGGLE_ADMIN_MESSAGES("Admin-Nachrichten");
    private final String name;
}
