package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FactionsCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public FactionsCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("factions", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 80) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        player.sendMessage("§7   ===§8[§bFraktionen§8]§7===");
        for (FactionData factionData : factionManager.getFactions()) {
            TextComponent faction = new TextComponent("§8 ➥ §" + factionData.getPrimaryColor() + factionData.getName());
            faction.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§" + factionData.getSecondaryColor() + Utils.toDecimalFormat(factionData.getBank()) + "$ §8| §" + factionData.getSecondaryColor() + factionManager.getMemberCount(factionData.getName()) + "§7/§" + factionData.getSecondaryColor() + factionData.getMaxMember())));
            player.spigot().sendMessage(faction);
        }
        return false;
    }
}
