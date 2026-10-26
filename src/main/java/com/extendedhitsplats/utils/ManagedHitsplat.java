package com.extendedhitsplats.utils;

import net.runelite.api.Hitsplat;

public class ManagedHitsplat {
    public final Hitsplat hitsplat;
    public final int position;

    public ManagedHitsplat(Hitsplat hitsplat, int position)
    {
        this.hitsplat = hitsplat;
        this.position = position;
    }
}
