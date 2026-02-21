package de.polo.core.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
/**
 * Hibernate entity that maps the {@code players} table.
 *
 * <p>Only the columns that are frequently mutated by game logic
 * (money, bank, rank, level, exp, playtime, etc.) are mapped here.
 * The remaining columns are left to the existing JDBC layer until a
 * full migration is performed.
 *
 * <p>hbm2ddl.auto = validate – Hibernate validates the schema but
 * never changes it.
 */
@Entity
@Table(name = "players")
@Getter
@Setter
public class PlayerEntity {

    @Id
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "player_name", length = 64)
    private String playerName;

    @Column(name = "bargeld")
    private int bargeld;

    @Column(name = "bank")
    private int bank;

    @Column(name = "player_rank", length = 64)
    private String rang;

    @Column(name = "player_permlevel")
    private int permlevel;

    @Column(name = "level")
    private int level;

    @Column(name = "exp")
    private int exp;

    @Column(name = "needed_exp")
    private int neededExp;

    @Column(name = "playtime_hours")
    private int playtimeHours;

    @Column(name = "playtime_minutes")
    private int playtimeMinutes;

    @Column(name = "current_hours")
    private int currentHours;

    @Column(name = "needed_hours")
    private int neededHours;

    @Column(name = "visum")
    private int visum;

    @Column(name = "coins")
    private int coins;

    @Column(name = "crypto")
    private float crypto;

    @Column(name = "deathTime")
    private int deathTime;

    @Column(name = "isDead")
    private boolean dead;

    @Column(name = "loyaltyBonus")
    private int loyaltyBonus;

    /**
     * Creates a {@link PlayerEntity} pre-populated from the in-memory
     * {@code PlayerData} fields that are persisted by the Hibernate layer.
     * Called once per player login to seed the Caffeine cache.
     */
    public static PlayerEntity fromPlayerData(
            java.util.UUID uuid,
            String playerName,
            int bargeld,
            int bank,
            String rang,
            int permlevel,
            int level,
            int exp,
            int coins,
            float crypto,
            boolean dead,
            int deathTime,
            int loyaltyBonus) {
        PlayerEntity e = new PlayerEntity();
        e.setUuid(uuid.toString());
        e.setPlayerName(playerName);
        e.setBargeld(bargeld);
        e.setBank(bank);
        e.setRang(rang);
        e.setPermlevel(permlevel);
        e.setLevel(level);
        e.setExp(exp);
        e.setCoins(coins);
        e.setCrypto(crypto);
        e.setDead(dead);
        e.setDeathTime(deathTime);
        e.setLoyaltyBonus(loyaltyBonus);
        return e;
    }
}
