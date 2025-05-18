package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * Capability interface to store and manage a player's pass tier.
 * Tier values correspond to PassType tiers: 0=FREE, 1=SUPER, 2=ULTRA, 3=MASTER.
 */
public interface IPassCapability {
    /**
     * Gets the current pass tier for the player.
     *
     * @return the tier value (0–3)
     */
    int getTier();

    /**
     * Sets the pass tier for the player.
     *
     * @param tier the new tier value (0–3)
     */
    void setTier(int tier);

    /**
     * Synchronizes this capability's data to the client.
     * Should be called on the server side when the tier changes.
     *
     * @param player the server player to sync to
     */
    void syncToClient(ServerPlayerEntity player);
}
