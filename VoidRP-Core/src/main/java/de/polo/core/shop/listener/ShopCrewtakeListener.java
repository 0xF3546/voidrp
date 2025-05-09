package de.polo.core.shop.listener;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.crew.services.CrewService;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.shop.entities.CrewTakeShop;
import de.polo.core.shop.services.ShopService;
import de.polo.core.utils.Event;
import de.polo.core.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

@Event
public class ShopCrewtakeListener implements Listener {

    @EventHandler
    public void onMinuteTick(MinuteTickEvent event) {
        ShopService service = VoidAPI.getService(ShopService.class);
        CrewService crewService = VoidAPI.getService(CrewService.class);
        service.getActiveCrewTakes().forEach(crewTake -> {
            List<VoidPlayer> crewPlayers = crewService.getOnlineMembers(crewTake.crew());
            if (crewPlayers.isEmpty()) {
                crewService.sendMessageToMembers(crewTake.crew(), "Eure Crew hat keine Mitglieder mehr online, die Übernahme wird abgebrochen.");
                service.removeCrewTake(crewTake);
                return;
            }
            boolean isAnyPlayerNear = crewPlayers.stream().anyMatch(player -> player.getLocation().distance(crewTake.shop().getLocation()) < 5);
            if (!isAnyPlayerNear) {
                crewService.sendMessageToMembers(crewTake.crew(), "Ihr seid zu weit vom Shop entfernt, die Übernahme wird abgebrochen.");
                service.removeCrewTake(crewTake);
                return;
            }
            if (crewTake.startTime().plusMinutes(5).isAfter(Utils.getTime())) {
                crewService.sendMessageToMembers(crewTake.crew(), "Ihr habt den Shop " + crewTake.shop().getName() + " erfolgreich eingenommen.");
                crewTake.crew().addExp(Utils.random(12, 20), "Shop übernahme");
                service.removeCrewTake(crewTake);
            } else {
                crewService.sendMessageToMembers(crewTake.crew(), "Noch " + (5 - Utils.getTime().getMinute() + crewTake.startTime().getMinute()) + " Minuten bis zur erfolgreichen Übernahme des Shops " + crewTake.shop().getName() + ".");
            }
        });
    }
}
