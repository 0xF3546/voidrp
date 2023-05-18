package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.commands.farmerCommand;
import de.polo.void_roleplay.commands.lumberjackCommand;
import de.polo.void_roleplay.commands.mineCommand;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class blockbreakListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            if (playerData.getVariable("job") != null) {
                switch (playerData.getVariable("job")) {
                    case "lumberjack":
                        lumberjackCommand.blockBroken(player, event.getBlock(), event);
                        break;
                    case "mine":
                        mineCommand.blockBroken(player, event.getBlock(), event);
                        break;
                    case "farmer":
                        farmerCommand.blockBroken(player, event.getBlock(), event);
                        break;
                }
            } else {
                event.setCancelled(true);
            }
        }
    }
}
