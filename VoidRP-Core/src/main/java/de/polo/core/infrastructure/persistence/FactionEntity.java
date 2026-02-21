package de.polo.core.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Hibernate entity that maps the {@code factions} table.
 *
 * <p>Only columns that are mutated in-game (primarily {@code bank},
 * {@code motd}, {@code chatColor}, {@code equipPoints}, {@code alliance},
 * {@code subGroup}) are mapped here.  Static columns set at server setup
 * (name, type, etc.) are included for completeness but are rarely updated
 * by Hibernate.
 *
 * <p>hbm2ddl.auto = validate – schema is never altered.
 */
@Entity
@Table(name = "factions")
@Getter
@Setter
public class FactionEntity {

    @Id
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "fullname", length = 128)
    private String fullname;

    @Column(name = "type", length = 32)
    private String type;

    @Column(name = "bank")
    private int bank;

    @Column(name = "payday")
    private int payDay;

    @Column(name = "primaryColor", length = 16)
    private String primaryColor;

    @Column(name = "secondaryColor", length = 16)
    private String secondaryColor;

    @Column(name = "maxMember")
    private int maxMember;

    @Column(name = "hasBlacklist")
    private boolean hasBlacklist;

    @Column(name = "doGangwar")
    private boolean doGangwar;

    @Column(name = "hasLaboratory")
    private boolean hasLaboratory;

    @Column(name = "isBadFrak")
    private boolean badFrak;

    @Column(name = "isActive")
    private boolean active;

    @Column(name = "motd", length = 512)
    private String motd;

    @Column(name = "chatColor", length = 32)
    private String chatColor;

    @Column(name = "equippoints")
    private int equipPoints;

    @Column(name = "alliance")
    private int allianceFaction;

    @Column(name = "subGroup")
    private int subGroupId;

    /**
     * Factory – creates a {@link FactionEntity} pre-populated from
     * the in-memory {@link de.polo.api.faction.Faction} domain model.
     * Called once per startup when seeding the Caffeine cache.
     */
    public static FactionEntity fromDomain(de.polo.api.faction.Faction f) {
        FactionEntity e = new FactionEntity();
        e.setId(f.getId());
        e.setName(f.getName());
        e.setFullname(f.getFullname());
        e.setType(f.getFactionType() != null ? f.getFactionType().name() : null);
        e.setBank(f.getBank());
        e.setPayDay(f.getPayDay());
        e.setPrimaryColor(f.getPrimaryColor());
        e.setSecondaryColor(f.getSecondaryColor());
        e.setMaxMember(f.getMaxMember());
        e.setHasBlacklist(f.isHasBlacklist());
        e.setDoGangwar(f.isDoGangwar());
        e.setHasLaboratory(f.isHasLaboratory());
        e.setBadFrak(f.isBadFrak());
        e.setActive(f.isActive());
        e.setMotd(f.getMotd());
        e.setChatColor(f.getChatColor());
        e.setEquipPoints(f.getEquipPoints());
        e.setAllianceFaction(f.getAllianceFaction());
        e.setSubGroupId(f.getSubGroupId());
        return e;
    }
}
