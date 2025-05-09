package de.polo.voidroleplay.player.entities;

import de.polo.voidroleplay.jobs.enums.MiniJob;
import de.polo.voidroleplay.storage.PlayerData;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import static de.polo.voidroleplay.Main.playerService;

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
    public PlayerData getData() {
        return playerService.getPlayerData(player);
    }

    @Override
    public void sendMessage(Component component) {
        player.sendMessage(component);
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
