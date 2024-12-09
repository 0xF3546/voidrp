package de.polo.voidroleplay.utils.player;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PlayerInventoryManager {
    private final List<PlayerInventoryItem> items = new ObjectArrayList<>();

    private final PlayerData playerData;

    public PlayerInventoryManager(PlayerData playerData) {
        this.playerData = playerData;
        load();
    }

    private void load() {
        Main.getInstance().getMySQL().executeQueryAsync("SELECT * FROM player_inventory_items WHERE uuid = ?", playerData.getUuid().toString())
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

    public void addItem(PlayerInventoryItem item) {
        PlayerInventoryItem cachedItem = getByType(item.getItem());
        if (cachedItem != null) {
            cachedItem.setAmount(cachedItem.getAmount() + 1);
            updateItem(cachedItem);
            return;
        }
        Main.getInstance().getMySQL().insertAndGetKeyAsync("INSERT INTO player_inventory_items (uuid, item, amount) VALUES (?, ?, ?)", playerData.getUuid().toString(), item.getItem().name(), item.getAmount())
                .thenApply(key -> {
                    if (key.isPresent()) {
                        item.setId(key.get());
                        items.add(item);
                    }
                    return null;
                });
    }

    public void removeItem(PlayerInventoryItem item) {
        PlayerInventoryItem cachedItem = getByType(item.getItem());
        if (cachedItem != null && cachedItem.getAmount() > 1) {
            cachedItem.setAmount(cachedItem.getAmount() - 1);
            updateItem(cachedItem);
            return;
        }
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
