package de.polo.core.infrastructure.cache;

import de.polo.core.infrastructure.persistence.PlayerEntity;

/**
 * Wraps a {@link PlayerEntity} and provides dirty-tracking via
 * {@link AbstractDirtyEntry}.
 *
 * <p>All money/coin/crypto mutations update the in-memory entity and
 * automatically mark this entry dirty.  The {@link de.polo.core.infrastructure.persistence.FlushService}
 * picks up dirty instances on a schedule and persists them in a single batch
 * transaction – avoiding one DB write per operation.
 */
public final class CachedPlayer extends AbstractDirtyEntry<PlayerEntity> {

    private final PlayerEntity entity;

    public CachedPlayer(PlayerEntity entity) {
        this.entity = entity;
    }

    @Override
    public PlayerEntity getEntity() {
        return entity;
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
