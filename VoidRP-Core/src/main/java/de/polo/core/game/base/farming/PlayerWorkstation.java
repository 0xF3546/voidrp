package de.polo.core.game.base.farming;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.manager.ItemManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.enums.Workstation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerWorkstation {
    public final Workstation Workstation;
    private final UUID uuid;
    private int id = -1;
    private int input;
    private int output;

    public PlayerWorkstation(UUID uuid, Workstation workstation) {
        this.uuid = uuid;
        this.Workstation = workstation;
    }

    @SneakyThrows
    public static Collection<PlayerWorkstation> getPlayerWorkstationsFromDatabase(UUID uuid) {
        List<PlayerWorkstation> workstations = new ObjectArrayList<>();
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM workstations WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            System.out.println("LOADED WORKSTATION #" + result.getInt("id"));
            PlayerWorkstation workstation = new PlayerWorkstation(uuid, de.polo.core.utils.enums.Workstation.valueOf(result.getString("workstation")));
            workstation.setId(result.getInt("id"));
            workstation.setInput(result.getInt("input"));
            workstation.setOutput(result.getInt("output"));
            workstations.add(workstation);
        }

        return workstations;
    }

    public static boolean hasWorkstation(Player player, Workstation workstation) {
        for (PlayerData playerData : Main.playerManager.getPlayers()) {
            if (playerData.getPlayer() == player) {
                for (PlayerWorkstation playerWorkstation : playerData.getWorkstations()) {
                    if (playerWorkstation.Workstation.equals(workstation)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getInput() {
        return input;
    }

    public void setInput(int input) {
        this.input = input;
    }

    public int getOutput() {
        return output;
    }

    public void setOutput(int output) {
        this.output = output;
    }

    public void doTick() {
        if (input < Workstation.getTickInput()) return;
        setOutput(output + Workstation.getTickOutput());
        setInput(input - Workstation.getTickInput());
    }

    @SneakyThrows
    public void create() {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO workstations (uuid, workstation) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, uuid.toString());
        statement.setString(2, Workstation.name());
        statement.execute();
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            int key = generatedKeys.getInt(1);
            setId(key);
        }
    }

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE workstations SET input = ?, output = ? WHERE id = ?");
        statement.setInt(1, input);
        statement.setInt(2, output);
        statement.setInt(3, id);
        statement.execute();
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void open() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §7Workstation (" + this.Workstation.getName() + ")"));

        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cAusmieten", "§8 ➥ §4§lAchtung!§4 Du verlierst deine Workstation-Items.")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                player.sendMessage(Prefix.MAIN + "Du hast deine Workstation geschlossen.");
                deleteWorkstation();
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Workstation.getOutputItem().getMaterial(), 1, 0, Workstation.getOutputItem().getDisplayName(), "§8 ➥ §7" + output + " Stück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (output <= 0) return;
                player.closeInventory();
                ItemManager.addCustomItem(player, Workstation.getOutputItem(), output);
                player.sendMessage("§8[§7Workstation§8]§7 Du hast " + output + " " + Workstation.getOutputItem().getDisplayName() + "§7 entnommen.");
                setOutput(0);
                save();
            }
        });
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Workstation.getInputItem().getMaterial(), 1, 0, Workstation.getInputItem().getDisplayName(), "§8 ➥ §7" + input + " Stück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openInputChange();
            }
        });

    }

    private void openInputChange() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        int newInputCount = ItemManager.getCustomItemCount(player, Workstation.getInputItem());

        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §7Workstation (" + this.Workstation.getName() + ")"));

        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Workstation.getInputItem().getMaterial(), 1, 0, Workstation.getInputItem().getDisplayName(), "§8 » §eKlicke um " + input + " Stück auszulagern.")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (input <= 0) return;
                player.closeInventory();
                ItemManager.addCustomItem(player, Workstation.getInputItem(), input);
                player.sendMessage("§8[§7Workstation§8]§7 Du hast " + input + " " + Workstation.getInputItem().getDisplayName() + "§7 ausgelagert.");
                setInput(0);
                save();
            }
        });

        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Workstation.getInputItem().getMaterial(), 1, 0, Workstation.getInputItem().getDisplayName(), "§8 » §eKlicke um " + newInputCount + " Stück einzulagern.")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (newInputCount <= 0) return;
                player.closeInventory();
                ItemManager.removeCustomItem(player, Workstation.getInputItem(), newInputCount);
                player.sendMessage("§8[§7Workstation§8]§7 Du hast " + newInputCount + " " + Workstation.getInputItem().getDisplayName() + "§7 eingelagert.");
                setInput(input + newInputCount);
                save();
            }
        });

        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                open();
            }
        });
    }

    @SneakyThrows
    public void deleteWorkstation() {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("DELETE FROM workstations WHERE id = ?");
        statement.setInt(1, id);
        statement.execute();
        Main.playerManager.getPlayerData(uuid).removeWorkstation(this);
    }
}
