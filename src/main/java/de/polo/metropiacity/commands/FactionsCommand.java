package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerShearEntityEvent;

public class FactionsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() < 80) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        player.sendMessage("§7   ===§8[§bFraktionen§8]§7===");
        for (FactionData factionData : FactionManager.factionDataMap.values()) {
            TextComponent faction = new TextComponent("§8 ➥ §" + factionData.getPrimaryColor() + factionData.getName());
            faction.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§" + factionData.getSecondaryColor() + Utils.toDecimalFormat(factionData.getBank()) + "$ §8| §" + factionData.getSecondaryColor() + FactionManager.getMemberCount(factionData.getName()) + "§7/§" + factionData.getSecondaryColor() + factionData.getMaxMember())));
            player.spigot().sendMessage(faction);
        }
        return false;
    }
}
