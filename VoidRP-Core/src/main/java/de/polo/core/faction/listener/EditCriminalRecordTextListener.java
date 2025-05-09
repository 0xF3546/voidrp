package de.polo.core.faction.listener;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.game.events.SubmitChatEvent;
import de.polo.core.utils.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Event
public class EditCriminalRecordTextListener implements Listener {
    @EventHandler
    public void onTextMessage(SubmitChatEvent event) {
        if (!event.getSubmitTo().equalsIgnoreCase("criminalrecord::edit")) return;
        if (event.isCancel()) {
            event.end();
            event.sendCancelMessage();
            return;
        }
        VoidPlayer player = VoidAPI.getPlayer(event.getPlayer());
        player.setVariable("criminalrecord::edit::infoText", event.getMessage());
        player.getLastGUI().open();
        event.end();
    }
}
