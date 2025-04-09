package de.polo.api.player;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PlayerCharacter {
    <T> void setVariable(String key, T value);
    <T> T getVariable(String key);
    void addMoney(int amount, String reason);
}
