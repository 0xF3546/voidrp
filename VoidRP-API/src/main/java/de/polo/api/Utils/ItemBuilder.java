package de.polo.api.Utils;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.annotations.Beta;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ItemBuilder - ItemBuilder for Bukkit 1.13+
 * <p>
 * This class is used to create ItemStacks with ease.
 * This class is licensed under the MIT License.
 * </p>
 * Usage:
 * <code>
 * ItemStack item = new ItemBuilder(Material.DIRT).build();
 * </code>
 *
 * @author Erik Pförtner
 * @since 1.0.0
 */
public class ItemBuilder {
    private final ItemStack item;

    /**
     * Create an ItemBuilder instance
     *
     * @param mat Material of the item
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    public ItemBuilder(@NotNull final Material mat) {
        this.item = new ItemStack(mat);
    }

    /**
     * Create an ItemBuilder instance using an existing item stack
     */
    public ItemBuilder(@NotNull final ItemStack item) {
        this.item = item;
    }

    /**
     * Use ItemStack.setAmount() on item
     *
     * @param amount Item amount
     * @return ItemBuilder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder setAmount(final int amount) {
        this.item.setAmount(amount);
        return this;
    }

    /**
     * Use ItemStack.setDurability() on item
     *
     * @param data Item durability
     * @return ItemBuilder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     * @deprecated In 1.13+ durability is replaced by Materialname (Material.GLASS_PANE -> Material.LIGHT_GRAY_STAINED_GLASS_PANE)
     */
    @Deprecated
    @NotNull
    public ItemBuilder setDurability(final byte data) {
        this.item.setDurability(data);
        return this;
    }

    /**
     * Use ItemMeta.setDisplayName() on item
     *
     * @param name Item name
     * @return ItemBuilder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder setName(@NotNull final String name) {
        ItemMeta meta = this.item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(name);
        this.item.setItemMeta(meta);
        return this;
    }

    /**
     * Use ItemMeta.addEnchant() on item
     *
     * @param enchantment Enchantment
     * @param level       Enchantment level
     * @return ItemBuilder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder addEnchantment(@NotNull final Enchantment enchantment, @NotNull final Integer level) {
        ItemMeta meta = this.item.getItemMeta();
        assert meta != null;
        meta.addEnchant(enchantment, level, true);
        this.item.setItemMeta(meta);
        return this;
    }

    /**
     * Use ItemMeta.addEnchant() on item
     * <p>
     * <b>NOTE:</b> This method is used to add multiple enchantments to an item.
     * If you want to add only one enchantment, use {@link ItemBuilder#addEnchantment(Enchantment, Integer)}
     * </p>
     *
     * @param enchantments Map of enchantments
     *                     <p>
     *                     <b>NOTE:</b> The key of the map is the enchantment, the value is the level of the enchantment
     *                     </p>
     * @return ItemBuilder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder addEnchantments(@NotNull final Map<Enchantment, Integer> enchantments) {
        ItemMeta meta = this.item.getItemMeta();
        assert meta != null;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }
        this.item.setItemMeta(meta);
        return this;
    }

    /**
     * Use ItemMeta.addUnsafeEnchantment() on item
     *
     * @param enchantment Enchantment
     * @param level       Enchantment level
     * @return ItemBuilder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder addUnsafeEnchantment(@NotNull final Enchantment enchantment, int level) {
        this.item.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    /**
     * Use ItemMeta.addUnsafeEnchantments() on item
     * <p>
     * <b>NOTE:</b> This method is used to add multiple enchantments to an item.
     * If you want to add only one enchantment, use {@link ItemBuilder#addUnsafeEnchantment(Enchantment, int)}
     * </p>
     *
     * @param enchantments Map of enchantments
     *                     <p>
     *                     <b>NOTE:</b> The key of the map is the enchantment, the value is the level of the enchantment
     *                     </p>
     * @return ItemBuilder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder addUnsafeEnchantments(@NotNull final Map<Enchantment, Integer> enchantments) {
        this.item.addUnsafeEnchantments(enchantments);
        return this;
    }

    /**
     * Use ItemMeta.setUnbreakable() on item
     *
     * @param unbreakable Unbreakable state
     * @return ItemBuilder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder setUnbreakable(final boolean unbreakable) {
        ItemMeta meta = this.item.getItemMeta();
        assert meta != null;
        meta.setUnbreakable(unbreakable);
        this.item.setItemMeta(meta);
        return this;
    }

    /**
     * Use ItemMeta.addItemFlags() on item
     *
     * @param flags Array of item flags
     * @return ItemBuilder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder setItemFlags(@NotNull final ItemFlag... flags) {
        ItemMeta meta = this.item.getItemMeta();
        assert meta != null;
        meta.addItemFlags(flags);
        this.item.setItemMeta(meta);
        return this;
    }

    /**
     * Use ItemMeta.setLore() on item
     *
     * @param lorestr Item lore
     * @return Item Builder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder setLore(@NotNull final String lorestr) {
        ItemMeta meta = this.item.getItemMeta();
        List<String> lore = new ArrayList<String>();
        lore.add(lorestr);
        assert meta != null;
        meta.setLore(lore);
        this.item.setItemMeta(meta);
        return this;
    }

    /**
     * Use ItemMeta.setLore() on item
     *
     * @param lore Item lore
     * @return Item Builder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder setLore(@NotNull final List<String> lore) {
        ItemMeta meta = this.item.getItemMeta();
        assert meta != null;
        meta.setLore(lore);
        this.item.setItemMeta(meta);
        return this;
    }

    /**
     * Use ItemMeta.setCustomModelData() on item
     *
     * @param data Custom model data
     * @return Item Builder object
     * @author Erik Pförtner
     * @see ItemBuilder
     */
    @NotNull
    public ItemBuilder setCustomModelData(final int data) {
        ItemMeta meta = this.item.getItemMeta();
        assert meta != null;
        meta.setCustomModelData(data);
        this.item.setItemMeta(meta);
        return this;
    }


    /**
     * Use ItemMeta.setLore() on item with color codes in lore
     *
     * @param lorestr Item lore with color codes to be translated
     * @return Item Builder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder setLoreFormatted(@NotNull final String lorestr) {
        List<String> loreFormatted = new ArrayList<String>();
        loreFormatted.add(ChatColor.translateAlternateColorCodes('&', lorestr));
        ItemMeta meta = this.item.getItemMeta();
        assert meta != null;
        meta.setLore(loreFormatted);
        this.item.setItemMeta(meta);
        return this;
    }


    /**
     * Use ItemMeta.setLore() on item with color codes in lore
     *
     * @param lore Item lore with color codes to be translated
     * @return Item Builder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder setLoreFormatted(@NotNull final List<String> lore) {
        List<String> loreFormatted = new ArrayList<String>();
        for (String loreLine : lore) {
            loreFormatted.add(ChatColor.translateAlternateColorCodes('&', loreLine));
        }
        ItemMeta meta = this.item.getItemMeta();
        assert meta != null;
        meta.setLore(loreFormatted);
        this.item.setItemMeta(meta);
        return this;
    }

    /**
     * Use SkullMeta.setOwner() on a skull
     *
     * @param owner The owner of that skull
     * @return Builder object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder setOwner(@NotNull final String owner) {
        if (this.item.getType() != Material.PLAYER_HEAD) {
            return this;
        }
        SkullMeta meta = (SkullMeta) this.item.getItemMeta();
        assert meta != null;
        meta.setOwner(owner);
        this.item.setItemMeta(meta);
        return this;
    }

    /**
     * Use SkullMeta.setOwner() on a skull
     *
     * @param texture The texture of that skull
     * @return Builder object
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder setTexture(@NotNull final String texture) {
        if (this.item.getType() != Material.PLAYER_HEAD) {
            return this;
        }

        SkullMeta meta = (SkullMeta) this.item.getItemMeta();
        if (meta == null) {
            return this;
        }

        PlayerProfile profile = meta.getPlayerProfile();
        if (profile == null) {
            profile = Bukkit.createProfile(UUID.randomUUID(), "profile");
        }

        profile.setProperty(new ProfileProperty("textures", texture));
        meta.setPlayerProfile(profile);
        this.item.setItemMeta(meta);

        return this;
    }

    /**
     * Use ((ArmorMeta) armorMeta).setTrim() on an armor item
     * <p>
     * <b>NOTE:</b> Spigot 1.20+ api experimental feature
     * </p>
     *
     * @param material Trim material
     * @param pattern  Trim pattern
     * @return Builder object
     * @see ItemBuilder
     * @since 1.0.0
     */
    @Beta
    @NotNull
    public ItemBuilder addArmorTrim(@NotNull final TrimMaterial material, @NotNull final TrimPattern... pattern) {
        if (!isArmor(this.item)) {
            return this;
        }
        ItemMeta meta = this.item.getItemMeta();
        assert meta != null;

        if (meta instanceof ArmorMeta armorMeta) {
            for (TrimPattern trimPattern : pattern) {
                armorMeta.setTrim(new ArmorTrim(material, trimPattern));
            }
        }
        this.item.setItemMeta(meta);

        return this;
    }

    /**
     * Use ItemStack.setItemMeta() on item
     * <p>
     * <b>NOTE:</b> This method overrides all previous item meta data!
     * </p>
     *
     * @param itemMeta ItemMeta object
     * @return ItemBuilder object
     * @see ItemBuilder
     * @since 1.0.0
     */
    @NotNull
    public ItemBuilder setItemMeta(@NotNull final ItemMeta itemMeta) {
        this.item.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Get the finished item
     *
     * @return ItemStack object
     * @author Erik Pförtner
     * @see ItemBuilder
     * @see ItemStack
     * @since 1.0.0
     */
    @NotNull
    public ItemStack build() {
        return this.item;
    }

    /**
     * Check if an item is armor
     * <p>
     * This method checks if an item is armor.
     * It is used to check if an item is a helmet, chestplate, leggings or boots.
     * </p>
     *
     * @param item ItemStack to check
     * @return True if the item is armor, otherwise false
     * @since 1.0.0
     */
    public boolean isArmor(@NotNull final ItemStack item) {
        Material itemType = item.getType();

        return itemType == Material.LEATHER_BOOTS ||
                itemType == Material.LEATHER_LEGGINGS ||
                itemType == Material.LEATHER_CHESTPLATE ||
                itemType == Material.LEATHER_HELMET ||
                itemType == Material.CHAINMAIL_BOOTS ||
                itemType == Material.CHAINMAIL_LEGGINGS ||
                itemType == Material.CHAINMAIL_CHESTPLATE ||
                itemType == Material.CHAINMAIL_HELMET ||
                itemType == Material.IRON_BOOTS ||
                itemType == Material.IRON_LEGGINGS ||
                itemType == Material.IRON_CHESTPLATE ||
                itemType == Material.IRON_HELMET ||
                itemType == Material.GOLDEN_BOOTS ||
                itemType == Material.GOLDEN_LEGGINGS ||
                itemType == Material.GOLDEN_CHESTPLATE ||
                itemType == Material.GOLDEN_HELMET ||
                itemType == Material.DIAMOND_BOOTS ||
                itemType == Material.DIAMOND_LEGGINGS ||
                itemType == Material.DIAMOND_CHESTPLATE ||
                itemType == Material.DIAMOND_HELMET ||
                itemType == Material.NETHERITE_BOOTS ||
                itemType == Material.NETHERITE_LEGGINGS ||
                itemType == Material.NETHERITE_CHESTPLATE ||
                itemType == Material.NETHERITE_HELMET;
    }
}
