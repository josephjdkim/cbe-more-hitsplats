package com.extendedhitsplats.utils;

import com.extendedhitsplats.ExtendedHitsplatsPlugin;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

public class Icons {
    // osrs hitsplats
    private static final Class<?> PLUGIN_CLASS = ExtendedHitsplatsPlugin.class;
    public static final BufferedImage OSRS_ALT_CHARGE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/alt_charge_hitsplat.png");
    public static final BufferedImage OSRS_ALT_UNCHARGE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/alt_uncharge_hitsplat.png");
    // In the other players' hitsplat reds; scaled down from 48 to sit with the game's hitsplats
    private static final int BIG_HITSPLAT_SIZE = 30;
    public static final BufferedImage OSRS_BIG_HITSPLAT = ImageUtil.resizeImage(ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/big_hitsplat.png"), BIG_HITSPLAT_SIZE, BIG_HITSPLAT_SIZE);
    public static final BufferedImage OSRS_BLEED_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/bleed_hitsplat.png");
    public static final BufferedImage OSRS_BURN_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/burn_hitsplat.png");
    public static final BufferedImage OSRS_CORRUPTION_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/corruption_hitsplat.png");
    public static final BufferedImage OSRS_DISEASE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/disease_hitsplat.png");
    public static final BufferedImage OSRS_DOOM_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/doom_hitsplat.png");
    public static final BufferedImage OSRS_HEAL_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/heal_hitsplat.png");
    public static final BufferedImage OSRS_MAX_ARMOUR_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/max_armour_hitsplat.png");
    public static final BufferedImage OSRS_MAX_CHARGE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/max_charge_hitsplat.png");
    public static final BufferedImage OSRS_MAX_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/max_hitsplat.png");
    public static final BufferedImage OSRS_MAX_POISE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/max_poise_hitsplat.png");
    public static final BufferedImage OSRS_MAX_SHIELD_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/max_shield_hitsplat.png");
    public static final BufferedImage OSRS_MAX_UNCHARGE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/max_uncharge_hitsplat.png");
    public static final BufferedImage OSRS_OTHER_ARMOUR_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/other_armour_hitsplat.png");
    public static final BufferedImage OSRS_OTHER_CHARGE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/other_charge_hitsplat.png");
    public static final BufferedImage OSRS_OTHER_DAMAGE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/other_damage_hitsplat.png");
    public static final BufferedImage OSRS_OTHER_MISS_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/other_miss_hitsplat.png");
    public static final BufferedImage OSRS_OTHER_POISE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/other_poise_hitsplat.png");
    public static final BufferedImage OSRS_OTHER_SHIELD_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/other_shield_hitsplat.png");
    public static final BufferedImage OSRS_PRAYER_DRAIN_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/prayer_drain_hitsplat.png");
    public static final BufferedImage OSRS_POISON_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/poison_hitsplat.png");
    public static final BufferedImage OSRS_SANITY_DRAIN_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/sanity_drain_hitsplat.png");
    public static final BufferedImage OSRS_SANITY_RESTORE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/sanity_restore_hitsplat.png");
    public static final BufferedImage OSRS_SELF_ARMOUR_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/self_armour_hitsplat.png");
    public static final BufferedImage OSRS_SELF_CHARGE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/self_charge_hitsplat.png");
    public static final BufferedImage OSRS_SELF_DAMAGE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/self_damage_hitsplat.png");
    public static final BufferedImage OSRS_SELF_MISS_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/self_miss_hitsplat.png");
    public static final BufferedImage OSRS_SELF_POISE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/self_poise_hitsplat.png");
    public static final BufferedImage OSRS_SELF_SHIELD_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/self_shield_hitsplat.png");
    public static final BufferedImage OSRS_SELF_UNCHARGE_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/self_uncharge_hitsplat.png");
    public static final BufferedImage OSRS_VENOM_HITSPLAT = ImageUtil.loadImageResource(PLUGIN_CLASS, "/com/extendedhitsplats/hitsplats/osrs/venom_hitsplat.png");

}
