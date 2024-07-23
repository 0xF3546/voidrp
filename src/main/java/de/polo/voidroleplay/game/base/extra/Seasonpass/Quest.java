package de.polo.voidroleplay.game.base.extra.Seasonpass;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Quest {
    private int id;
    private int points;
    private String name;
    private String description;
    private int rewardId;

    public Quest(int id, int points, String name, String description) {
        this.id = id;
        this.points = points;
        this.name = name;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
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
}
