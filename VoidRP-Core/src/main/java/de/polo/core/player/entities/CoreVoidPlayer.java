package de.polo.core.player.entities;

import de.polo.api.player.PlayerCharacter;
import de.polo.api.player.VoidPlayer;
import de.polo.api.jobs.enums.MiniJob;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import static de.polo.core.Main.playerService;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreVoidPlayer implements VoidPlayer {
    @Getter
    private final Player player;

    @Getter
    private MiniJob miniJob;

    public CoreVoidPlayer(Player player) {
        this.player = player;
    }

    @Override
    public PlayerCharacter getData() {
        return (PlayerCharacter) playerService.getPlayerData(player);
    }

    @Override
    public void sendMessage(Component component) {
        player.sendMessage(component);
    }

    @Override
    public void setVariable(String key, Object value) {
        getData().setVariable(key, value);
    }

    @Override
    public Object getVariable(String key) {
        return getData().getVariable(key);
    }

    public void setMiniJob(MiniJob miniJob) {
        this.miniJob = miniJob;
        if (miniJob == null) {
            getData().setVariable("job", null);
            return;
        }
        getData().setVariable("job", miniJob.getName());
    }
}
