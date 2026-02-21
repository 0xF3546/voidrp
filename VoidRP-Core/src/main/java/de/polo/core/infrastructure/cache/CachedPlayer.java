package de.polo.core.infrastructure.cache;

import de.polo.core.infrastructure.persistence.PlayerEntity;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wraps a {@link PlayerEntity} with an atomic dirty flag.
 *
 * <p>All money mutations update the in-memory entity and set the dirty flag.
 * The {@link de.polo.core.infrastructure.persistence.FlushService} picks up
 * dirty instances on a schedule and persists them in a single batch
 * transaction – avoiding one DB write per operation.
 *
 * <p>Thread safety: {@code dirty} is an {@link AtomicBoolean} so reads and
 * writes from the Bukkit main thread and from the async flush thread are safe.
 * The {@link PlayerEntity} fields themselves are only mutated from the main
 * thread (Bukkit event handlers), so no additional locking is required.
 */
public final class CachedPlayer {

    private final PlayerEntity entity;
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    public CachedPlayer(PlayerEntity entity) {
        this.entity = entity;
    }

    public PlayerEntity getEntity() {
        return entity;
    }

    /** Returns {@code true} if this entry has unsaved changes. */
    public boolean isDirty() {
        return dirty.get();
    }

    /**
     * Marks this entry as needing to be flushed to the database.
     * Called after any in-memory mutation.
     */
    public void markDirty() {
        dirty.set(true);
    }

    /**
     * Clears the dirty flag after a successful flush.
     * Returns the previous dirty state so callers can skip a flush that
     * was already cleared by a concurrent flush.
     */
    public boolean clearDirty() {
        return dirty.getAndSet(false);
    }

    // ── Convenience mutators that automatically mark dirty ─────────────────

    public int getBargeld() {
        return entity.getBargeld();
    }

    public void setBargeld(int bargeld) {
        entity.setBargeld(bargeld);
        markDirty();
    }

    public int getBank() {
        return entity.getBank();
    }

    public void setBank(int bank) {
        entity.setBank(bank);
        markDirty();
    }

    public int getCoins() {
        return entity.getCoins();
    }

    public void setCoins(int coins) {
        entity.setCoins(coins);
        markDirty();
    }

    public float getCrypto() {
        return entity.getCrypto();
    }

    public void setCrypto(float crypto) {
        entity.setCrypto(crypto);
        markDirty();
    }

    public int getLoyaltyBonus() {
        return entity.getLoyaltyBonus();
    }

    public void setLoyaltyBonus(int loyaltyBonus) {
        entity.setLoyaltyBonus(loyaltyBonus);
        markDirty();
    }
}
