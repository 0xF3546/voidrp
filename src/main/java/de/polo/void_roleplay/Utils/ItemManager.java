package de.polo.void_roleplay.Utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

public class ItemManager {

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

    public static ItemStack createCustomHead(String texture, int anzahl, int subid, String displayname, String lore)
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
        if (lore != null) {
            m.setLore(Collections.singletonList(lore));
        }
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
    public static void removeItem(Player player, Material item, Integer count) {
        for (int i = 0; i < count; i++) {
            player.getInventory().removeItem(new ItemStack(item));
        }
    }

}