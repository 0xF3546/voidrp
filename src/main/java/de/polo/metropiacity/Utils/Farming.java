package de.polo.metropiacity.Utils;

import de.polo.metropiacity.Utils.Events.BreakPersistentBlockEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

public class Farming implements Listener {
    @EventHandler
    public void onPersistentBlockBreak(BreakPersistentBlockEvent event) {
        String type = event.getPersistentData(PersistentDataType.STRING);
        if (type.contains("farming_")) {
            String farmingType = type.replace("farming_", "");

        }
    }
}
