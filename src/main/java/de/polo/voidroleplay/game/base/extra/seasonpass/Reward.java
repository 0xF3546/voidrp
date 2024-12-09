package de.polo.voidroleplay.game.base.extra.seasonpass;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Reward {
    private int id;
    private String type;
    private int amount;
    private String info;
    private String name;

    public Reward(int id, String type, int amount) {
        this.id = id;
        this.type = type;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
