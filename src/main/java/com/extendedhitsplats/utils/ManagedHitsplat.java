package com.extendedhitsplats.utils;

import net.runelite.api.Hitsplat;

/**
 * A copy of a hitsplat's values, taken when it was applied. The game reuses its hitsplat objects for later
 * hits, so holding on to one would let an expired hitsplat take on a newer hit's values.
 */
public class ManagedHitsplat {
    public final int hitsplatType;
    public final int amount;
    public final int disappearsOnGameCycle;
    // True if the game draws this hit itself
    public final boolean shownByGame;
    // The game's position for this hit if it shows it, otherwise the extra slot the overlay draws it in
    public final int position;

    public ManagedHitsplat(Hitsplat hitsplat, boolean shownByGame, int position)
    {
        this(hitsplat.getHitsplatType(), hitsplat.getAmount(), hitsplat.getDisappearsOnGameCycle(), shownByGame, position);
    }

    private ManagedHitsplat(int hitsplatType, int amount, int disappearsOnGameCycle, boolean shownByGame, int position)
    {
        this.hitsplatType = hitsplatType;
        this.amount = amount;
        this.disappearsOnGameCycle = disappearsOnGameCycle;
        this.shownByGame = shownByGame;
        this.position = position;
    }

    // The same hit in an extra slot, once the game stops showing it
    public ManagedHitsplat asExtra(int slot)
    {
        return new ManagedHitsplat(hitsplatType, amount, disappearsOnGameCycle, false, slot);
    }
}
