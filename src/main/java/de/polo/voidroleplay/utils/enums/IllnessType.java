package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IllnessType {
    HUSTEN("Husten"),
    CHOLERA("Cholera"),
    HERPES("Herpes");

    private final String name;

}
