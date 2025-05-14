package de.polo.core.beerpong.listeners;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.beerpong.services.BeerPongService;
import de.polo.core.beerpong.handler.BeerPongHandler;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.utils.Event;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import static de.polo.core.Main.blockManager;

@Event
public class BeerPongThrowListener implements Listener {
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;

        VoidPlayer voidPlayer = VoidAPI.getPlayer(shooter);
        BeerPongService service = VoidAPI.getService(BeerPongService.class);
        BeerPongHandler handler = service.getHandlerByPlayer(voidPlayer);

        if (handler == null || !handler.isStarted()) return;

        if (!handler.getCurrentTurn().getPlayer().equals(voidPlayer)) {
            voidPlayer.sendMessage("Du bist nicht dran!", Prefix.BEERPONG);
            return;
        }

        if (event.getHitBlock() != null) {
            Block hitBlock = event.getHitBlock();
            RegisteredBlock registeredBlock = blockManager.getBlockAtLocation(hitBlock.getLocation());
            if (registeredBlock == null) {
                voidPlayer.sendMessage("Du hast nichts getroffen!", Prefix.BEERPONG);
            }
            if (hitBlock.getType() == Material.FLOWER_POT) {
                hitBlock.setType(Material.AIR);
                handler.increaseScore(voidPlayer);
                voidPlayer.sendMessage("Treffer! Punkt für dein Team!", Prefix.BEERPONG);
            } else {
                voidPlayer.sendMessage("Kein Treffer!", Prefix.BEERPONG);
            }
        } else {
            voidPlayer.sendMessage("Du hast nichts getroffen!", Prefix.BEERPONG);
        }

        handler.increaseScore(voidPlayer);
        handler.switchTurn();
    }

}
