package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.faction.entity.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import lombok.SneakyThrows;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class RegisterFactionBanner implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public RegisterFactionBanner(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;

        Main.registerCommand("registerfactionbanner", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 90) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (strings.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /registerfactionbanner [Fraktion]");
            return false;
        }
        Block block = player.getTargetBlock(null, 10);
        BlockState state = block.getState();
        if (!(state instanceof Banner)) {
            player.sendMessage(Prefix.ERROR + "Der Angeschaute Block ist kein Banner.");
            return false;
        }

        Banner banner = (Banner) state;
        List<Pattern> patterns = banner.getPatterns();
        JSONArray jsonArray = new JSONArray();

        for (Pattern pattern : patterns) {
            JSONObject patternObject = new JSONObject();
            patternObject.put("color", pattern.getColor().name());
            patternObject.put("type", pattern.getPattern().name());
            jsonArray.put(patternObject);
        }

        JSONObject bannerObject = new JSONObject();
        bannerObject.put("baseColor", banner.getType().name());
        bannerObject.put("patterns", jsonArray);

        FactionData factionData = factionManager.getFactionData(strings[0]);
        if (factionData == null) {
            player.sendMessage(Prefix.ERROR + "Fraktion nicht gefunden.");
            return false;
        }
        factionData.setBannerPattern(banner.getPatterns());
        factionData.setBannerColor(banner.getType());
        Main.getInstance().getMySQL().updateAsync("UPDATE factions SET banner = ? WHERE id = ?",
                bannerObject.toString(),
                factionData.getId());
        return false;
    }
}
