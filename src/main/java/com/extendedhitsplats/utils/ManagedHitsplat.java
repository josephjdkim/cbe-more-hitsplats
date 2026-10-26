package com.extendedhitsplats.utils;

import net.runelite.api.Hitsplat;

/**
 * A copy of a hitsplat's values, taken when it was applied. The game recycles its hitsplat objects, so
 * holding on to one would let an expired hitsplat take on a newer hit's values.
 */
public class ManagedHitsplat {
    public final int hitsplatType;
    public final int amount;
    public final int disappearsOnGameCycle;
    public final int position;

    public ManagedHitsplat(Hitsplat hitsplat, int position)
    {
        this.hitsplatType = hitsplat.getHitsplatType();
        this.amount = hitsplat.getAmount();
        this.disappearsOnGameCycle = hitsplat.getDisappearsOnGameCycle();
        this.position = position;
    }
}
