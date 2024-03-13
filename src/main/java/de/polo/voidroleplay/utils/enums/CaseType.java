package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CaseType {
    BASIC("§6§lCase"),
    DAILY("§b§lDaily-Case");

    private final String DisplayName;
}
