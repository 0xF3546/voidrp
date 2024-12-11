package de.polo.voidroleplay.utils.player;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
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
        Main.getInstance().getMySQL().executeQueryAsync("SELECT * FROM player_inventory_items WHERE uuid = ?", playerData.getPlayer().getUniqueId().toString())
                .thenAccept(result -> {
                    if (result != null && !result.isEmpty()) {
                        for (Map<String, Object> itemValue : result) {
                            PlayerInventoryItem item = new PlayerInventoryItem((Integer) itemValue.get("id"), RoleplayItem.valueOf((String) itemValue.get("item")), (Integer) itemValue.get("amount"));
                            items.add(item);
                        }
                    }
                });
    }

    public Collection<PlayerInventoryItem> getItems() {
        return items;
    }

    private void updateItem(PlayerInventoryItem item) {
        Main.getInstance().getMySQL().updateAsync("UPDATE player_inventory_items SET amount = ? WHERE id = ?", item.getAmount(), item.getId());
    }

    public void setSizeToDatabase(int size) {
        setSize(size);
        Main.getInstance().getMySQL().updateAsync("UPDATE players SET inventorySize = ? WHERE uuid = ?", size, playerData.getUuid().toString());
    }

    private int getWeight() {
        int weight = 0;
        for (PlayerInventoryItem item : items) {
            weight += item.getAmount();
        }
        return weight;
    }

    public boolean addItem(RoleplayItem item, int amount) {
        PlayerInventoryItem inventoryItem = getByType(item);
        if (inventoryItem == null) {
            inventoryItem = new PlayerInventoryItem(item, amount);
        } else {
            inventoryItem.setAmount(inventoryItem.getAmount() + amount);
        }
        return addItem(inventoryItem);
    }

    public boolean addItem(PlayerInventoryItem item) {
        if (getWeight() + item.getAmount() > getSize()) return false;
        PlayerInventoryItem cachedItem = getByType(item.getItem());
        if (cachedItem != null) {
            cachedItem.setAmount(cachedItem.getAmount() + 1);
            updateItem(cachedItem);
            return true;
        }
        Main.getInstance().getMySQL().insertAndGetKeyAsync("INSERT INTO player_inventory_items (uuid, item, amount) VALUES (?, ?, ?)", playerData.getUuid().toString(), item.getItem().name(), item.getAmount())
                .thenApply(key -> {
                    if (key.isPresent()) {
                        item.setId(key.get());
                        items.add(item);
                    }
                    return null;
                });
        return true;
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

    private void removeItem(PlayerInventoryItem item) {
        PlayerInventoryItem cachedItem = getByType(item.getItem());
        Main.getInstance().getMySQL().deleteAsync("DELETE FROM player_inventory_items WHERE id = ?", item.getId());
        items.remove(item);
    }

    public PlayerInventoryItem getByTypeOrEmpty(RoleplayItem type) {
        return items.stream().filter(x -> x.getItem() == type).findFirst().orElse(new PlayerInventoryItem(type, 0));
    }

    public PlayerInventoryItem getByType(RoleplayItem type) {
        return items.stream().filter(x -> x.getItem() == type).findFirst().orElse(null);
    }
}
