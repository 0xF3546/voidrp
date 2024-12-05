package de.polo.voidroleplay.dataStorage;

import lombok.Getter;
import org.bukkit.entity.Player;

public class Agreement {
    @Getter
    private final Player contractor;

    @Getter
    private final Player contracted;

    @Getter
    private final String type;

    @Getter
    private final Object agreement;

    public Agreement(Player contractor, Player contracted, String type, Object agreement) {
        this.contractor = contractor;
        this.contracted = contracted;
        this.type = type;
        this.agreement = agreement;
    }
}
