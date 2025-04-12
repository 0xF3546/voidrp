package de.polo.core.player.entities;

import de.polo.api.VoidAPI;
import de.polo.api.jobs.Job;
import de.polo.api.player.PlayerCharacter;
import de.polo.api.player.VoidPlayer;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.core.player.services.PlayerService;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    @Getter
    @Setter
    private Job activeJob;

    private boolean notificationsEnabled;

    @Getter
    private boolean aduty;

    public CoreVoidPlayer(Player player) {
        this.player = player;
    }

    @Override
    public PlayerCharacter getData() {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
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

    @Override
    public boolean notificationsEnabled() {
        return notificationsEnabled;
    }

    @Override
    public void setNotificationsEnabled(boolean enabled) {
        this.notificationsEnabled = enabled;
    }

    public void setMiniJob(MiniJob miniJob) {
        this.miniJob = miniJob;
        if (miniJob == null) {
            getData().setVariable("job", null);
            return;
        }
        getData().setVariable("job", miniJob.getName());
    }

    public void setAduty(boolean aduty) {
        this.aduty = aduty;
        if (aduty) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));

        } else {
            player.removePotionEffect(PotionEffectType.GLOWING);

        }
    }
}
