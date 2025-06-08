package de.polo.api.Utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum Prefix {
    INFO("§b    Info§f: "),
    ADMIN("§8[§cAdmin§8] §7"),
    ERROR_CANT_INTERACT("§8[§cFehler§8] §7Du kannst gerade nicht interagieren."),
    PAYDAY("§8[§aPayDay§8] §7"),
    FACTION("§8[§9Fraktion§8] §7"),
    SUPPORT("§8[§3Support§8] §7"),
    GAMEDESIGN("§8[§9Gamedesign§8] §7"),
    ERROR_NO_PERMISSION("§cFehler: Für den ausgeführten Befehl hast du keine Rechte."),
    ERROR("§cFehler: "),
    ADMIN_ERROR("§8[§c§lADMIN§8] §cFehler§8 » §7"),
    BUSINESS("§8[§6Business§8]§7 "),
    MAIN("§8 » §7"),
    BEERPONG("§9BeerPong §8┃ §7➜ "),
    POLICE_COMPUTER("§9Polizeicomputer §8┃ §7➜ "),;

    private final String prefix;

    @Override
    public String toString() {
        return prefix;
    }
}