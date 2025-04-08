package de.polo.core.game.base.extra.beginnerpass;

import org.bukkit.Material;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Quest {
    private int id;
    private String name;
    private String description;
    private int rewardId;
    private Material item;
    private int reachedAt;

    public Quest(int id, String name, String description, int reachedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.reachedAt = reachedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRewardId() {
        return rewardId;
    }

    public void setRewardId(int rewardId) {
        this.rewardId = rewardId;
    }

    public Material getItem() {
        return item;
    }

    public void setItem(Material item) {
        this.item = item;
    }

    public int getReachedAt() {
        return reachedAt;
    }

    public void setReachedAt(int reachedAt) {
        this.reachedAt = reachedAt;
    }
}

