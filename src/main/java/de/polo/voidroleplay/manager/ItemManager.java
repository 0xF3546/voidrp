package de.polo.voidroleplay.manager;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;

public class ItemManager {

    public static ItemStack createItem(Material material, int anzahl, int subid, String displayname)
    {
        short neuesubid = (short)subid;
        ItemStack i = new ItemStack(material, anzahl, neuesubid);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(displayname);
        i.setItemMeta(m);

        return i;
    }
    public static ItemStack createItem(Material material, int anzahl, int subid, String displayname, String lore)
    {
        short neuesubid = (short)subid;
        ItemStack i = new ItemStack(material, anzahl, neuesubid);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(displayname);
        if (lore != null) {
            m.setLore(Collections.singletonList(lore));
        }
        i.setItemMeta(m);

        return i;
    }

    public static ItemStack createItem(Material material, int anzahl, int subid, String displayname, List list)
    {
        short neuesubid = (short)subid;
        ItemStack i = new ItemStack(material, anzahl, neuesubid);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(displayname);
        if (list != null) {
            m.setLore(list);
        }
        i.setItemMeta(m);

        return i;
    }

    public static ItemStack createItemHead(String owner, int anzahl, int subid, String displayname, String lore)
    {
        UUID uuid = UUID.fromString(owner);
        short neuesubid = (short)subid;
        ItemStack i = new ItemStack(Material.PLAYER_HEAD, anzahl, neuesubid);
        SkullMeta skullMeta = (SkullMeta) i.getItemMeta();
        assert skullMeta != null;
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        i.setItemMeta(skullMeta);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(displayname);
        if (lore != null) {
            m.setLore(Collections.singletonList(lore));
        }
        i.setItemMeta(m);

        return i;
    }

    public static ItemStack createItemHead(String owner, int anzahl, int subid, String displayname)
    {
        UUID uuid = UUID.fromString(owner);
        short neuesubid = (short)subid;
        ItemStack i = new ItemStack(Material.PLAYER_HEAD, anzahl, neuesubid);
        SkullMeta skullMeta = (SkullMeta) i.getItemMeta();
        assert skullMeta != null;
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        i.setItemMeta(skullMeta);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(displayname);
        i.setItemMeta(m);

        return i;
    }

    public static ItemStack createItemHead(String owner, int anzahl, int subid, String displayname, List list)
    {
        UUID uuid = UUID.fromString(owner);
        short neuesubid = (short)subid;
        ItemStack i = new ItemStack(Material.PLAYER_HEAD, anzahl, neuesubid);
        SkullMeta skullMeta = (SkullMeta) i.getItemMeta();
        assert skullMeta != null;
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        i.setItemMeta(skullMeta);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(displayname);
        m.setLore(list);

        i.setItemMeta(m);

        return i;
    }

    public static ItemStack createCustomHead(String texture, int anzahl, int subid, String displayname, List list)
    {
        short neuesubid = (short)subid;
        ItemStack i = new ItemStack(Material.PLAYER_HEAD, anzahl, neuesubid);
        SkullMeta meta = (SkullMeta) i.getItemMeta();

        UUID id = UUID.randomUUID();
        GameProfile profile = new GameProfile(id, "");
        profile.getProperties().put("textures", new Property("textures", texture));

        Field profileField = null;

        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
        i.setItemMeta(meta);

        ItemMeta m = i.getItemMeta();
        m.setDisplayName(displayname);
        if (list != null) {
            m.setLore(list);
        }
        i.setItemMeta(m);

        return i;
    }

    public static ItemStack createCustomHead(String texture, int anzahl, int subid, String displayname)
    {
        short neuesubid = (short)subid;
        ItemStack i = new ItemStack(Material.PLAYER_HEAD, anzahl, neuesubid);
        SkullMeta meta = (SkullMeta) i.getItemMeta();

        UUID id = UUID.randomUUID();
        GameProfile profile = new GameProfile(id, "");
        profile.getProperties().put("textures", new Property("textures", texture));

        Field profileField = null;

        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
        i.setItemMeta(meta);

        ItemMeta m = i.getItemMeta();
        m.setDisplayName(displayname);
        i.setItemMeta(m);

        return i;
    }

    private String getURLFromBase64(String base64) {
        return new String(Base64.getDecoder().decode(base64.getBytes())).replace("{\"textures\":{\"SKIN\":{\"url\":\"", "").replace("\"}}}", "");
    }

    public static int getItem(Player player, Material material) {
        ItemStack[] contents = player.getInventory().getContents();
        int count = 0;
        for (ItemStack itemStack : contents) {
            if (itemStack != null && itemStack.getType() == material) {
                count += itemStack.getAmount();
            }
        }
        return count;
    }
    public static int getCustomItemCount(Player player, RoleplayItem item) {
        ItemStack[] contents = player.getInventory().getContents();
        int count = 0;
        for (ItemStack itemStack : contents) {
            if (itemStack != null) {
                if (itemStack.getType() == item.getMaterial() && itemStack.getItemMeta().getDisplayName().equals(item.getDisplayName())) {
                    count += itemStack.getAmount();
                }
            }
        }
        return count;
    }
    public static void removeItem(Player player, Material item, int count) {
        int remainingCount = count;

        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack itemStack : contents) {
            if (itemStack != null && itemStack.getType() == item) {
                int stackAmount = itemStack.getAmount();
                if (stackAmount <= remainingCount) {
                    remainingCount -= stackAmount;
                    player.getInventory().removeItem(itemStack);
                } else {
                    itemStack.setAmount(stackAmount - remainingCount);
                    break;
                }
                if (remainingCount <= 0) {
                    break;
                }
            }
        }

        ItemStack offhandItem = player.getInventory().getItemInOffHand();
        if (offhandItem.getType() == item && remainingCount > 0) {
            int offhandAmount = offhandItem.getAmount();
            if (offhandAmount <= remainingCount) {
                remainingCount -= offhandAmount;
                player.getInventory().setItemInOffHand(null);
            } else {
                offhandItem.setAmount(offhandAmount - remainingCount);
            }
        }
    }



    public static void removeCustomItem(Player player, RoleplayItem item, int amount) {
        ItemStack[] contents = player.getInventory().getContents();

        for (ItemStack itemStack : contents) {
            if (itemStack != null && itemStack.getType() == item.getMaterial()) {
                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() &&
                        itemStack.getItemMeta().getDisplayName().equals(item.getDisplayName())) {
                    if (itemStack.getAmount() >= amount) {
                        itemStack.setAmount(itemStack.getAmount() - amount);
                        player.updateInventory();
                        return;
                    } else {
                        amount -= itemStack.getAmount();
                        player.getInventory().remove(itemStack);
                    }
                }
            }
        }

        ItemStack offhandItem = player.getInventory().getItemInOffHand();
        if (offhandItem.getType() == item.getMaterial() && amount > 0) {
            if (offhandItem.hasItemMeta() && offhandItem.getItemMeta().hasDisplayName() &&
                    offhandItem.getItemMeta().getDisplayName().equals(item.getDisplayName())) {
                if (offhandItem.getAmount() >= amount) {
                    offhandItem.setAmount(offhandItem.getAmount() - amount);
                    player.updateInventory();
                } else {
                    amount -= offhandItem.getAmount();
                    player.getInventory().setItemInOffHand(null);
                }
            }
        }
    }


    public static void addCustomItem(Player player, RoleplayItem item, int amount) {
        for (int i = 0; i < amount; i++) {
            player.getInventory().addItem(ItemManager.createItem(item.getMaterial(), 1, 0, item.getDisplayName()));
        }
    }

    public static void addItem(Player player, Material item, String displayName, int amount) {
        for (int i = 0; i < amount; i++) {
            player.getInventory().addItem(ItemManager.createItem(item, 1, 0, displayName));
        }
    }

    public static boolean equals(ItemStack itemStack, RoleplayItem roleplayItem) {
        if (itemStack == null) return false;
        if (itemStack.getItemMeta() == null) return false;
        return (itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(roleplayItem.getDisplayName()) && itemStack.getType().equals(roleplayItem.getMaterial()));
    }
}