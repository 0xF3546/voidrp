package de.polo.core.utils.player;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.enums.RoleplayItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PlayerInventoryManager {
    private final List<PlayerInventoryItem> items = new ObjectArrayList<>();

    private final PlayerData playerData;

    @Getter
    @Setter
    private int size;

    public PlayerInventoryManager(PlayerData playerData) {
        this.playerData = playerData;
        load();
    }

    private void load() {
        // ISSUE VRP-10000: SQL race condition
        try {
            Main.getInstance().getCoreDatabase().executeQueryAsync("SELECT * FROM player_inventory_items WHERE uuid = ?", playerData.getPlayer().getUniqueId().toString())
                    .thenAccept(result -> {
                        if (result != null && !result.isEmpty()) {
                            for (Map<String, Object> itemValue : result) {
                                PlayerInventoryItem item = new PlayerInventoryItem((Integer) itemValue.get("id"), RoleplayItem.valueOf((String) itemValue.get("item")), (Integer) itemValue.get("amount"));
                                items.add(item);
                            }
                        }
                    });
        } catch (Exception e) {
            // Stacktrace:
            // [13:26:28] [pool-24-thread-1/INFO]: [VoidRoleplay] [STDOUT] Executing query: SELECT * FROM player_inventory_items WHERE uuid = ? with args: [2aa52162-8631-4d2c-9989-3132c719e5ac]
            //[13:26:28] [pool-24-thread-1/INFO]: [VoidRoleplay] [STDOUT] Query successful, results: []
            //[13:26:28] [Server thread/WARN]: java.sql.SQLException: Column Index out of range, 0 < 1.
            System.out.printf("Error while loading inventory for %s%n", playerData.getPlayer().getName());
        }
    }

    public Collection<PlayerInventoryItem> getItems() {
        return items;
    }

    private void updateItem(PlayerInventoryItem item) {
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE player_inventory_items SET amount = ? WHERE id = ?", item.getAmount(), item.getId());
    }

    public void setSizeToDatabase(int size) {
        setSize(size);
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET inventorySize = ? WHERE uuid = ?", size, playerData.getUuid().toString());
    }

    public int getWeight() {
        int weight = 0;
        for (PlayerInventoryItem item : items) {
            weight += item.getAmount();
        }
        return weight;
    }

    public int getDiff() {
        return getSize() - getWeight();
    }

    public boolean addItem(RoleplayItem item, int amount) {
        PlayerInventoryItem inventoryItem = getByType(item);

        if (inventoryItem == null) {
            inventoryItem = new PlayerInventoryItem(item, amount);
            if (!canAddItem(inventoryItem)) return false;
            return addItem(inventoryItem);
        } else {
            int newAmount = inventoryItem.getAmount() + amount;
            if (!canAddWeight(amount)) return false;
            inventoryItem.setAmount(newAmount);
            updateItem(inventoryItem);
            return true;
        }
    }

    public boolean addItem(PlayerInventoryItem item) {
        if (!canAddItem(item)) return false;

        PlayerInventoryItem cachedItem = getByType(item.getItem());
        if (cachedItem != null) {
            cachedItem.setAmount(cachedItem.getAmount() + item.getAmount());
            updateItem(cachedItem);
        } else {
            Main.getInstance().getCoreDatabase().insertAndGetKeyAsync(
                    "INSERT INTO player_inventory_items (uuid, item, amount) VALUES (?, ?, ?)",
                    playerData.getUuid().toString(),
                    item.getItem().name(),
                    item.getAmount()
            ).thenApply(key -> {
                if (key.isPresent()) {
                    item.setId(key.get());
                    items.add(item);
                }
                return null;
            });
        }
        return true;
    }

    private boolean canAddItem(PlayerInventoryItem item) {
        return getWeight() + item.getAmount() <= getSize();
    }

    private boolean canAddWeight(int amount) {
        return getWeight() + amount <= getSize();
    }


    public boolean removeItem(RoleplayItem item, int amount) {
        PlayerInventoryItem inventoryItem = getByType(item);
        if (inventoryItem == null) return false;
        if (inventoryItem.getAmount() < amount) return false;
        inventoryItem.setAmount(inventoryItem.getAmount() - amount);
        if (inventoryItem.getAmount() <= 0) {
            removeItem(inventoryItem);
            return true;
        }
        updateItem(inventoryItem);
        return true;
    }

    public void removeItem(PlayerInventoryItem item) {
        PlayerInventoryItem cachedItem = getByType(item.getItem());
        Main.getInstance().getCoreDatabase().deleteAsync("DELETE FROM player_inventory_items WHERE id = ?", item.getId());
        items.remove(item);
    }

    public PlayerInventoryItem getByTypeOrEmpty(RoleplayItem type) {
        return items.stream().filter(x -> x.getItem() == type).findFirst().orElse(new PlayerInventoryItem(type, 0));
    }

    public PlayerInventoryItem getByType(RoleplayItem type) {
        return items.stream().filter(x -> x.getItem() == type).findFirst().orElse(null);
    }
}
