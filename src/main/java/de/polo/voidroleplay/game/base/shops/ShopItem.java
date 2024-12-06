package de.polo.voidroleplay.game.base.shops;

import org.bukkit.Material;

public class ShopItem {
    private int id;
    private int shop;
    private Material material;
    private String displayName;
    private int price;
    private String type;
    private String secondType;

    public ShopItem() {

    }

    public ShopItem(int id, int shop, Material material, String displayName, int price, String type, String secondType) {
        this.id = id;
        this.shop = shop;
        this.material = material;
        this.displayName = displayName;
        this.price = price;
        this.type = type;
        this.secondType = secondType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getShop() {
        return shop;
    }

    public void setShop(int shop) {
        this.shop = shop;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSecondType() {
        return secondType;
    }

    public void setSecondType(String secondType) {
        this.secondType = secondType;
    }
}
