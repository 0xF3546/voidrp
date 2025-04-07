package de.polo.voidroleplay.utils.player;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerTutorial {
    private final Player player;
    private final PlayerData playerData;
    private int stage = 1;

    public PlayerTutorial(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public PlayerTutorial(Player player, PlayerData playerData, int stage) {
        this.player = player;
        this.playerData = playerData;
        this.stage = stage;
    }

    public static PlayerTutorial getPlayerTutorial(PlayerData playerData) {
        if (playerData.getVariable("tutorial") == null) return null;
        return playerData.getVariable("tutorial");
    }

    public int getStage() {
        return stage;
    }

    @SneakyThrows
    public void setStage(int stage) {
        this.stage = stage;
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET tutorial = ? WHERE uuid = ?",
                stage,
                player.getUniqueId().toString());
    }

    @SneakyThrows
    public void end() {
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET tutorial = ? WHERE uuid = ?",
                0,
                player.getUniqueId().toString());
    }
}
