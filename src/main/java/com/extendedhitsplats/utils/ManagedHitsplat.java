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
    public final int position;

    public ManagedHitsplat(Hitsplat hitsplat, int position)
    {
        this(hitsplat.getHitsplatType(), hitsplat.getAmount(), hitsplat.getDisappearsOnGameCycle(), position);
    }

    private ManagedHitsplat(int hitsplatType, int amount, int disappearsOnGameCycle, int position)
    {
        this.hitsplatType = hitsplatType;
        this.amount = amount;
        this.disappearsOnGameCycle = disappearsOnGameCycle;
        this.position = position;
    }

    public ManagedHitsplat withPosition(int position)
    {
        return new ManagedHitsplat(hitsplatType, amount, disappearsOnGameCycle, position);
    }
}
