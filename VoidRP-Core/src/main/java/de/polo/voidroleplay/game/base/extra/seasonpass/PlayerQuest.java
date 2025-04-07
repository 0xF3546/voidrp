package de.polo.voidroleplay.game.base.extra.seasonpass;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerQuest {
    private int id;
    private int questId;
    private int state;

    public PlayerQuest(int questId, int state) {
        this.questId = questId;
        this.state = state;
    }

    public PlayerQuest(int id, int questId, int state) {
        this.id = id;
        this.questId = questId;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestId() {
        return questId;
    }

    public void setQuestId(int questId) {
        this.questId = questId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
