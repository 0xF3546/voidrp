package de.polo.core.shop.listener;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.shop.services.ShopService;
import de.polo.core.utils.Event;
import de.polo.core.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Event
public class ShopRobTickListener implements Listener {
    @EventHandler
    public void onMinuteTick(MinuteTickEvent event) {
        ShopService shopService = VoidAPI.getService(ShopService.class);
        shopService.getActiveRobberies().forEach(shopRob -> {
            if (shopRob.getShop().getLocation().distance(shopRob.getRobber().getLocation()) > 5) {
                shopRob.getRobber().sendMessage("§cDu bist zu weit vom Shop entfernt, um den Raub erfolgreich abzuschließen.", Prefix.MAIN);
                shopService.removeRobbery(shopRob);
                return;
            }
            if (shopRob.getStartTime().plusMinutes(5).isAfter(Utils.getTime())) {
                VoidPlayer robber = shopRob.getRobber();

                robber.sendMessage("Du hast den Shop erfolgreich ausgeraubt.", Prefix.MAIN);
                shopService.removeRobbery(shopRob);
            } else {
                shopRob.getRobber().sendMessage("§cDie Raubüberfallzeit beträgt noch §l" + (5 - Utils.getTime().getMinute() + shopRob.getStartTime().getMinute()) + " Minuten§c.");
            }
        });
    }

}
