package de.polo.core.infrastructure.cache;

import de.polo.api.faction.Faction;
import de.polo.core.infrastructure.persistence.FactionEntity;

/**
 * Wraps both the domain {@link Faction} and the Hibernate {@link FactionEntity}
 * and provides dirty-tracking via {@link AbstractDirtyEntry}.
 *
 * <p>All bank/equip/motd/chatColor mutations update both the domain object and
 * the Hibernate entity, then mark this entry dirty so it will be persisted in
 * the next {@link de.polo.core.infrastructure.persistence.FlushService} batch.
 */
public final class CachedFaction extends AbstractDirtyEntry<FactionEntity> {

    private final Faction domain;
    private final FactionEntity entity;

    public CachedFaction(Faction domain, FactionEntity entity) {
        this.domain = domain;
        this.entity = entity;
    }

    public Faction getDomain() {
        return domain;
    }

    @Override
    public FactionEntity getEntity() {
        return entity;
    }

    // ── Convenience mutators that keep domain + entity in sync ──────────────

    public int getBank() {
        return domain.getBank();
    }

    public void setBank(int bank) {
        domain.setBank(bank);
        entity.setBank(bank);
        markDirty();
    }

    public int getEquipPoints() {
        return domain.getEquipPoints();
    }

    public void setEquipPoints(int equipPoints) {
        domain.setEquipPoints(equipPoints);
        entity.setEquipPoints(equipPoints);
        markDirty();
    }

    public String getMotd() {
        return domain.getMotd();
    }

    public void setMotd(String motd) {
        domain.setMotd(motd);
        entity.setMotd(motd);
        markDirty();
    }

    public int getAllianceFaction() {
        return domain.getAllianceFaction();
    }

    public void setAllianceFaction(int allianceFaction) {
        domain.setAllianceFaction(allianceFaction);
        entity.setAllianceFaction(allianceFaction);
        markDirty();
    }

    public int getSubGroupId() {
        return domain.getSubGroupId();
    }

    public void setSubGroupId(int subGroupId) {
        domain.setSubGroupId(subGroupId);
        entity.setSubGroupId(subGroupId);
        markDirty();
    }

    public void setChatColor(String chatColor) {
        domain.setChatColor(chatColor);
        entity.setChatColor(chatColor);
        markDirty();
    }
}
