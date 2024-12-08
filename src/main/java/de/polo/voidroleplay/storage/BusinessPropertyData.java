package de.polo.voidroleplay.storage;

import de.polo.voidroleplay.utils.enums.BusinessType;

import java.util.UUID;

public class BusinessPropertyData {
    private int id;
    private BusinessType type;
    private int price;
    private UUID owner;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BusinessType getType() {
        return type;
    }

    public void setType(BusinessType type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}
