package de.polo.voidroleplay.game.faction.staat;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import lombok.Getter;
import lombok.Setter;

public class SubTeam {
    @Getter
    @Setter
    private int id;

    @Getter
    private final int factionId;

    @Getter
    private final String name;

    public SubTeam(int factionId, String name) {
        this.factionId = factionId;
        this.name = name;
    }

    public void sendMessage(String message) {
        for (PlayerData playerData : Main.getInstance().playerManager.getPlayers()) {
            if (playerData.getSubTeam() == null) continue;
            if (playerData.getSubTeam() != this) continue;
            playerData.getPlayer().sendMessage(message);
        }
    }
}
