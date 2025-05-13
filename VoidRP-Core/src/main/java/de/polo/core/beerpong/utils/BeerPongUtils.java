package de.polo.core.beerpong.utils;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.beerpong.services.BeerPongService;
import de.polo.core.beerpong.handler.BeerPongHandler;
import de.polo.core.beerpong.entity.BeerPongPlayer;
import org.bukkit.inventory.ItemStack;

public class BeerPongUtils {

    public static void handlePlayerLeave(VoidPlayer player) {
        if (player.getVariable("beerpongInventory") != null) {
            player.getPlayer().getInventory().setContents((ItemStack[]) player.getVariable("beerpongInventory"));
            player.setVariable("beerpongInventory", null);
        }

        BeerPongService service = VoidAPI.getService(BeerPongService.class);
        BeerPongHandler handler = service.getHandlerByPlayer(player);
        if (handler == null) return;

        BeerPongPlayer pongPlayer = handler.getBeerPongPlayer(player);
        if (pongPlayer == null) return;

        handler.getPlayers().remove(pongPlayer);
        pongPlayer.unequip();

        if (handler.getPlayers().size() < 2) {
            handler.endGame();
        } else {
            if (handler.getCurrentTurn() != null && handler.getCurrentTurn().equals(pongPlayer)) {
                handler.switchTurn();
            }
        }
    }
}
