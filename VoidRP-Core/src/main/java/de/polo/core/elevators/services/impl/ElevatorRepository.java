package de.polo.core.elevators.services.impl;

import de.polo.api.VoidAPI;
import de.polo.api.elevators.Elevator;
import de.polo.api.elevators.Floor;
import de.polo.api.pinwheels.Pinwheel;
import de.polo.core.elevators.dto.CreateElevatorDto;
import de.polo.core.elevators.dto.CreateFloorDto;
import de.polo.core.elevators.entities.CoreElevator;
import de.polo.core.elevators.entities.CoreFloor;
import de.polo.core.location.services.LocationService;
import de.polo.core.pinwheels.dto.CreatePinwheelDto;
import de.polo.core.pinwheels.entities.CorePinwheel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.Location;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static de.polo.core.Main.database;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class ElevatorRepository {
    private final List<Elevator> elevators = new ObjectArrayList<>();

    @SneakyThrows
    public List<Elevator> getElevators() {
        if (elevators.isEmpty()) {
            LocationService locationService = VoidAPI.getService(LocationService.class);
            try (PreparedStatement statement = database.getConnection().prepareStatement("SELECT * FROM elevators")) {
                var resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    var id = resultSet.getInt("id");
                    var name = resultSet.getString("name");
                    Elevator elevator = new CoreElevator(id, new ObjectArrayList<>(), name);
                    elevators.add(elevator);
                    try (PreparedStatement floorStatement = database.getConnection().prepareStatement("SELECT * FROM elevator_stages WHERE elevatorId = ?")) {
                        floorStatement.setInt(1, id);
                        var floorResultSet = floorStatement.executeQuery();
                        while (floorResultSet.next()) {
                            int stage = floorResultSet.getInt("stage");
                            String floorName = floorResultSet.getString("name");
                            Location floorLocation = locationService.getLocation(floorResultSet.getInt("locationId"));
                            elevator.floors().add(new CoreFloor(elevator, stage, floorLocation));
                        }
                } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return elevators;
    }

    public int addElevator(CreateElevatorDto createElevatorDto) {
        database.insertAndGetKeyAsync(
                "INSERT INTO elevators (name) VALUES (?)",
                createElevatorDto.name()
        ).thenApply(key -> {
            int id = 0;
            if (key.isPresent()) {
                id = key.get();
                Elevator elevator = new CoreElevator(id, new ObjectArrayList<>(), createElevatorDto.name());
                elevators.add(elevator);
                addFloor(new CreateFloorDto(elevator, createElevatorDto.firstFloorLocation(),0));
            }
            return id;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
        return 0;
    }

    public int addFloor(CreateFloorDto createFloorDto) {
        var locationService = VoidAPI.getService(LocationService.class);
        int locationDbId = locationService.createLocation("elevator_" + createFloorDto.elevator().name() + "_stage_" + createFloorDto.floor(), createFloorDto.location());
        database.insertAndGetKeyAsync(
                "INSERT INTO elevator_stages (elevatorId, stage, locationId) VALUES (?, ?, ?)",
                createFloorDto.elevator().id(),
                createFloorDto.floor(),
                locationDbId
        ).thenApply(key -> {
            int id = 0;
            if (key.isPresent()) {
                id = key.get();
                Floor floor = new CoreFloor(createFloorDto.elevator(), createFloorDto.floor(), createFloorDto.location());
                createFloorDto.elevator().floors().add(floor);
            }
            return id;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
        return 0;
    }
}
