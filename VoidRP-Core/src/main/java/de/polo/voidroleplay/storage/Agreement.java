package de.polo.voidroleplay.storage;

import de.polo.voidroleplay.player.entities.VoidPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;

public class Agreement {
    @Getter
    private final VoidPlayer contractor;

    @Getter
    private final VoidPlayer contracted;

    @Getter
    private final String type;

    @Getter
    private final AgreementCallback callback;

    @Getter
    private final AgreementCallback denyCallback;

    public Agreement(VoidPlayer contractor, VoidPlayer contracted, String type, AgreementCallback callback, AgreementCallback denyCallback) {
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
