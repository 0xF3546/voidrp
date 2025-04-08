package de.polo.api.player.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gender {
    MALE("Männlich"),
    FEMALE("Weiblich");

    private final String translation;
}
