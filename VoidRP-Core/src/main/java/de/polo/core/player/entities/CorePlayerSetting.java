package de.polo.core.player.entities;

import de.polo.api.player.PlayerSetting;
import de.polo.api.player.enums.Setting;
import lombok.Getter;

public class CorePlayerSetting implements PlayerSetting {
    @Getter
    private final Setting setting;

    @Getter
    private final String value;

    public CorePlayerSetting(Setting setting, String value) {
        this.setting = setting;
        this.value = value;
    }
}
