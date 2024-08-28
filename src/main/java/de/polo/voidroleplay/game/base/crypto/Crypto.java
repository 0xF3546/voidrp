package de.polo.voidroleplay.game.base.crypto;

import de.polo.voidroleplay.Main;
import lombok.Getter;

public class Crypto {

    @Getter
    private float price = Main.random(1, 200);

    public Crypto() {
        updatePrice();
    }

    private void updatePrice() {
        float percentageChange = (price - 0.5f) * 0.1f;
        price += percentageChange;
        if (price < 1) price = 0;
    }
}
