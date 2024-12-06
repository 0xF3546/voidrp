package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class SperrzonenCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public SperrzonenCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("sperrzonen", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        player.sendMessage("§7   ===§8[§9Sperrzonen§8]§7===");
        for (String point : SperrzoneCommand.sperrzonen) {
            if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
                TextComponent message = new TextComponent("§8 ➥ §b" + point.replace("&", "§").replace("_", " "));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sperrzone " + point.replace(" ", "_").replace("§", "&")));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§b§oSperrzone löschen")));
                player.spigot().sendMessage(message);
            } else {
                player.sendMessage("§8 ➥ §b" + point.replace("&", "§").replace("_", " "));
            }
        }
        return false;
    }
}
