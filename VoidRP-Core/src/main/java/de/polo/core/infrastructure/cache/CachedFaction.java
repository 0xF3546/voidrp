package de.polo.core.infrastructure.cache;

import de.polo.api.faction.Faction;
import de.polo.core.infrastructure.persistence.FactionEntity;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wraps both the domain {@link Faction} and the Hibernate {@link FactionEntity}
 * with an atomic dirty flag.
 *
 * <p>All bank balance mutations update the domain object and set the dirty flag.
 * The {@link de.polo.core.infrastructure.persistence.FactionFlushService} picks
 * up dirty instances on a schedule and persists them in a single batch
 * transaction – avoiding one DB write per bank operation.
 *
 * <p>Thread safety: {@code dirty} is an {@link AtomicBoolean} so reads and
 * writes from different threads are safe.  Domain fields are only mutated
 * from Bukkit event handlers (main thread), so no extra locking is required
 * on those.
 */
public final class CachedFaction {

    private final Faction domain;
    private final FactionEntity entity;
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    public CachedFaction(Faction domain, FactionEntity entity) {
        this.domain = domain;
        this.entity = entity;
    }

    public Faction getDomain() {
        return domain;
    }

    public FactionEntity getEntity() {
        return entity;
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public void markDirty() {
        dirty.set(true);
    }

    /**
     * Atomically clears the dirty flag.
     *
     * @return the previous value – {@code true} if this entry was dirty and
     *         should be included in the current flush batch
     */
    public boolean clearDirty() {
        return dirty.getAndSet(false);
    }

    // ── Convenience mutators that keep domain + entity in sync ──────────────

    public int getBank() {
        return domain.getBank();
    }

    /**
     * Updates both the in-memory domain and the Hibernate entity, then marks
     * this entry dirty so it will be persisted in the next flush cycle.
     */
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
