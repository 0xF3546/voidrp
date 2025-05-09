package de.polo.core.player.entities;

import de.polo.api.Utils.GUI;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.jobs.Job;
import de.polo.api.player.PlayerCharacter;
import de.polo.api.player.PlayerRuntimeStatistic;
import de.polo.api.player.PlayerSetting;
import de.polo.api.player.VoidPlayer;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.api.player.enums.Setting;
import de.polo.core.player.services.PlayerService;
import de.polo.core.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.labymod.serverapi.core.model.feature.DiscordRPC;
import net.labymod.serverapi.server.bukkit.LabyModPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreVoidPlayer implements VoidPlayer {
    @Getter
    private final Player player;

    @Getter
    @Setter
    private GUI lastGUI;

    @Getter
    private MiniJob miniJob;
    @Getter
    @Setter
    private Job activeJob;

    @Getter
    private final List<PlayerSetting> settings = new ObjectArrayList<>();

    private boolean notificationsEnabled;

    @Getter
    private boolean aduty;

    @Getter
    private final PlayerRuntimeStatistic runtimeStatistic;

    @Getter
    private final UUID uuid;

    public CoreVoidPlayer(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.runtimeStatistic = new PlayerRuntimeStatistic(Utils.getTime());

        List<String> addonsToRequest = new ArrayList<>();
        addonsToRequest.add("damageindicator");
        addonsToRequest.add("clearwater");
        addonsToRequest.add("voicechat");

        loadSettings();
        LabyModPlayer labyModPlayer = VoidAPI.getLabyModPlayer(player);
        if (labyModPlayer == null) return;
        labyModPlayer.disableAddons("damageindicator", "clearwater");

        labyModPlayer.requestInstalledAddons(addonsToRequest, response -> {
            if (response.isEnabled("voicechat")) {
                player.sendMessage(Component.text("§cDu hast VoiceChat aktiviert. Bitte beachte, das diese modifikation nicht genutzt werden soll."));
            }
        });
        DiscordRPC discordRPC = DiscordRPC.createWithStart("Reallife & Roleplay", System.currentTimeMillis());
        labyModPlayer.sendDiscordRPC(discordRPC);
    }

    private void loadSettings() {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        playerService.getPlayerSettings(player)
                .thenAccept(playerSettings -> {
                    if (playerSettings == null) return;
                    this.settings.addAll(playerSettings);
                });
    }

    @Override
    public PlayerCharacter getData() {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        return playerService.getPlayerData(player);
    }

    @Override
    public void addSetting(Setting setting, String value) {
        PlayerSetting playerSetting = new CorePlayerSetting(setting, value);
        if (settings.stream().anyMatch(playerSetting1 -> playerSetting1.getSetting() == setting)) {
            return;
        }
        settings.add(playerSetting);
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        playerService.addPlayerSetting(this, playerSetting);
    }

    @Override
    public void removeSetting(Setting setting) {
        PlayerSetting playerSetting = settings.stream()
                .filter(playerSetting1 -> playerSetting1.getSetting() == setting)
                .findFirst()
                .orElse(null);
        if (playerSetting == null) {
            return;
        }
        settings.remove(playerSetting);
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        playerService.removePlayerSetting(this, playerSetting);
    }

    @Override
    public PlayerSetting getSetting(Setting setting) {
        return settings.stream()
                .filter(playerSetting -> playerSetting.getSetting() == setting)
                .findFirst()
                .orElse(null);
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
