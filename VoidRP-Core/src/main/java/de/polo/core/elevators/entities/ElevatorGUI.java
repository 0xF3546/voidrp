package de.polo.core.elevators.entities;

import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.elevators.Elevator;
import de.polo.api.elevators.Floor;
import de.polo.api.player.VoidPlayer;
import net.kyori.adventure.text.Component;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class ElevatorGUI {
    private final VoidPlayer player;
    private final Elevator elevator;
    private final Floor floor;
    public ElevatorGUI(VoidPlayer player, Elevator elevator, Floor floor) {
        this.player = player;
        this.elevator = elevator;
        this.floor = floor;
    }

    public void open() {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 9, Component.text("§7Fahrstuhl " + elevator.name()));
    }
}
