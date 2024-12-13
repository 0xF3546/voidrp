package de.polo.voidroleplay.storage;

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
    private final AgreementCallback callback;

    @Getter
    private final AgreementCallback denyCallback;

    public Agreement(Player contractor, Player contracted, String type, AgreementCallback callback, AgreementCallback denyCallback) {
        this.contractor = contractor;
        this.contracted = contracted;
        this.type = type;
        this.callback = callback;
        this.denyCallback = denyCallback;
    }

    // Method to accept the agreement and trigger the callback
    public void accept() {
        if (callback != null) {
            callback.execute();
        }
    }

    public void deny() {
        if (denyCallback != null) {
            denyCallback.execute();
        }
    }
}
