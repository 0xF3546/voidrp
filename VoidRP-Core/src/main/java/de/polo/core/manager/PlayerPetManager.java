package de.polo.core.manager;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.enums.Pet;
import de.polo.core.utils.enums.PlayerPed;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerPetManager {
    private final PlayerData playerData;
    private final Player player;
    private final List<PlayerPed> pets = new ObjectArrayList<>();

    public PlayerPetManager(PlayerData playerData, Player player) {
        this.playerData = playerData;
        this.player = player;
        load();
        if (getActivePed() != null) spawnPet(getActivePed());
    }

    @SneakyThrows
    private void load() {
        pets.clear();
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_pets WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            PlayerPed ped = new PlayerPed(Pet.valueOf(result.getString("pet")), result.getBoolean("active"));
            pets.add(ped);
        }
    }

    @SneakyThrows
    public void addPet(PlayerPed pet, boolean save) {
        pets.add(pet);
        if (save) {
            Connection connection = Main.getInstance().coreDatabase.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO player_pets (uuid, pet) VALUES (?, ?)");
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, pet.getPet().name());
            statement.execute();
            statement.close();
            connection.close();
        }
    }

    @SneakyThrows
    public void removePet(PlayerPed pet, boolean save) {
        pets.remove(pet);
        if (save) {
            Connection connection = Main.getInstance().coreDatabase.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM player_pets WHERE uuid = ? AND pet = ?");
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, pet.getPet().name());
            statement.execute();
            statement.close();
            connection.close();
        }
    }

    @SneakyThrows
    public void changeState(PlayerPed pet, boolean state) {
        pet.setActive(state);

        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE player_pets SET active = ? WHERE pet = ? AND uuid = ?");
        statement.setBoolean(1, state);
        statement.setString(3, player.getUniqueId().toString());
        statement.setString(2, pet.getPet().name());
        statement.execute();
        statement.close();
        connection.close();

        if (state) {
            spawnPet(pet);
        } else {
            despawnPet(pet);
        }
    }

    public Collection<PlayerPed> getPlayerPets() {
        return pets;
    }

    public PlayerPed getActivePed() {
        for (PlayerPed ped : getPlayerPets()) {
            if (ped.isActive()) return ped;
        }
        return null;
    }

    public PlayerPed getPed(Pet pet) {
        for (PlayerPed ped : getPlayerPets()) {
            if (ped.getPet().equals(pet)) return ped;
        }
        return null;
    }

    public void spawnPet(PlayerPed ped) {
        Animals entity = (Animals) player.getWorld().spawnEntity(player.getLocation(), ped.getPet().getAnimal());
        entity.setCustomName("§7" + player.getName() + "'s Haustier");
        entity.setCustomNameVisible(true);
        entity.teleport(player.getLocation());
        ped.setEntity(entity);
        entity.setInvulnerable(true);
        entity.setSilent(true);
        entity.setRemoveWhenFarAway(false);
        // entity.setAI(false); // hinteher laufe
        entity.setCollidable(false);
        entity.setCanPickupItems(false);
        entity.setBreed(false);
        entity.setTicksLived(Integer.MAX_VALUE);

        if (ped.getPet().isSmall()) {
            entity.setBaby();
        } else {
            entity.setAdult();
        }
        entity.setTarget(player);
    }

    public void despawnPet(PlayerPed ped) {
        if (ped.getEntity() != null) {
            ped.getEntity().remove();
            ped.setEntity(null);
        }
    }


    public void everySecond() {

    }
}
