package de.polo.void_roleplay.DataStorage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class HouseData {
    private int id;
    private String owner;
    private int number;
    private int price;
    private List<String> renter = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public List<String> getRenter() {
        return renter;
    }

    public void setRenter(List<String> renter) {
        this.renter = renter;
    }

    public void addRenter(String uuid) {
        this.renter.add(uuid);
    }
}
