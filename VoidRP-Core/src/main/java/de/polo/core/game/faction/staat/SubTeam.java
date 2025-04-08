package de.polo.core.game.faction.staat;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import lombok.Getter;
import lombok.Setter;

public class SubTeam {
    @Getter
    private final int factionId;
    @Getter
    private final String name;
    @Getter
    @Setter
    private int id;

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
