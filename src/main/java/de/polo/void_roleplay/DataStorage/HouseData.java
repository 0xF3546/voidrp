package de.polo.void_roleplay.DataStorage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HouseData {
    private int id;
    private String owner;
    private int number;
    private int price;
    private HashMap<String, Integer> renter = new HashMap<>();
    private int money;
    private int totalMoney;

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

    public HashMap<String, Integer> getRenter() {
        return renter;
    }

    public void setRenter(HashMap<String, Integer> map) {
        this.renter = map;
    }

    public void addRenter(String uuid, int miete) {
        this.renter.put(uuid, miete);
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(int totalMoney) {
        this.totalMoney = totalMoney;
    }
}
