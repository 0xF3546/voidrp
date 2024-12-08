package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.BlacklistData;
import de.polo.voidroleplay.storage.FactionData;
import de.polo.voidroleplay.manager.FactionManager;
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
public class BlacklistsCommand implements CommandExecutor {
    private final FactionManager factionManager;

    public BlacklistsCommand(FactionManager factionManager) {
        this.factionManager = factionManager;
        Main.registerCommand("blacklists", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        player.sendMessage("§7   ===§8[§cBlacklists§8]§7===");
        for (BlacklistData blacklistData : factionManager.getBlacklists()) {
            if (blacklistData.getUuid().equalsIgnoreCase(player.getUniqueId().toString())) {
                FactionData factionData = factionManager.getFactionData(blacklistData.getFaction());
                TextComponent message = new TextComponent("§8 - §" + factionData.getPrimaryColor() + factionData.getFullname() + "§8: §a" + blacklistData.getPrice() + "$ §7| §c" + blacklistData.getKills() + " Kills §8| §7" + blacklistData.getReason());
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/blacklist pay " + blacklistData.getFaction()));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oBezahlen")));
                player.spigot().sendMessage(message);
            }
        }
        return false;
    }
}
