package com.extendedhitsplats.overlays;
/*
 * Copyright (c) 2018, Morgan Lewis <https://github.com/MESLewis>
 * Copyright (c) 2022, Ferrariic <ferrariictweet@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.extendedhitsplats.ExtendedHitsplatsConfig;
import com.extendedhitsplats.points.SplatPoints;
import com.extendedhitsplats.utils.ActorHitsplats;
import com.extendedhitsplats.utils.HitsplatManager;
import com.extendedhitsplats.utils.Icons;
import com.extendedhitsplats.utils.ManagedHitsplat;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.Point;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

public class ExtendedHitsplatsOverlay extends Overlay
{
    // Used to find each actor's hitsplat anchor and to size the grid cells
    private static final BufferedImage REFERENCE_ICON = Icons.OSRS_SELF_DAMAGE_HITSPLAT;
    // Negative: the sprites' spiky edges are mostly transparent, so neighbours can overlap them a little
    private static final int CELL_GAP = -2;

    private final ExtendedHitsplatsConfig config;
    private final Client client;
    private final HitsplatManager hitsplatManager;

    @Inject
    private ExtendedHitsplatsOverlay(ExtendedHitsplatsConfig config, Client client, HitsplatManager hitsplatManager)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        this.config = config;
        this.client = client;
        this.hitsplatManager = hitsplatManager;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Map<Actor, ActorHitsplats> actors = hitsplatManager.getActors();
        if (actors.isEmpty()){
            return null;
        }

        int gameCycle = client.getGameCycle();
        for (Map.Entry<Actor, ActorHitsplats> entry : actors.entrySet()){
            Actor actor = entry.getKey();
            Point anchor = actor.getCanvasImageLocation(REFERENCE_ICON, actor.getLogicalHeight()/2);
            if (anchor == null){
                continue;
            }
            int centerX = anchor.getX() + REFERENCE_ICON.getWidth()/2 + 1;
            int centerY = anchor.getY() + REFERENCE_ICON.getHeight()/2 - 1;
            drawHitsplats(graphics, entry.getValue(), gameCycle, centerX, centerY);
        }
        return null;
    }

    // The game's hitsplat backgrounds are blanked, but it still draws their numbers, so the first four
    // slots sit exactly over the game's to cover them
    private void drawHitsplats(Graphics2D graphics, ActorHitsplats hitsplats, int gameCycle, int centerX, int centerY){
        int perSide = config.extrasPerSide();
        int cellWidth = REFERENCE_ICON.getWidth() + CELL_GAP;
        int cellHeight = REFERENCE_ICON.getHeight() + CELL_GAP;
        Font font = FontManager.getRunescapeSmallFont();

        for (ManagedHitsplat managedHitsplat : hitsplats.getHitsplats()) {
            Hitsplat hitsplat = managedHitsplat.hitsplat;
            if (hitsplat.getDisappearsOnGameCycle() <= gameCycle) {
                continue;
            }

            BufferedImage icon = iconFor(hitsplat.getHitsplatType());
            if (icon == null) {
                continue;
            }

            Point offset = SplatPoints.offset(managedHitsplat.position, perSide, cellWidth, cellHeight);
            drawHitsplat(graphics, icon, String.valueOf(hitsplat.getAmount()), font, centerX + offset.getX(), centerY + offset.getY());
        }
    }

    private static void drawHitsplat(Graphics2D graphics, BufferedImage icon, String text, Font font, int centerX, int centerY){
        int x = centerX - icon.getWidth()/2;
        int y = centerY - icon.getHeight()/2;
        graphics.drawImage(icon, x, y, null);

        graphics.setFont(font);
        FontMetrics metrics = graphics.getFontMetrics();
        int textX = x + (icon.getWidth() - metrics.stringWidth(text)) / 2;
        int textY = y + (icon.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        // draw shadow
        graphics.setColor(Color.black);
        graphics.drawString(text, textX+1, textY+1);
        // draw normal text
        graphics.setColor(Color.white);
        graphics.drawString(text, textX, textY);
    }

    private static BufferedImage iconFor(int hitsplat_type){
        BufferedImage hitIcon;
        switch (hitsplat_type){
            case HitsplatID.BLEED:
                hitIcon = Icons.OSRS_BLEED_HITSPLAT;
                break;
            case HitsplatID.BURN:
                hitIcon = Icons.OSRS_BURN_HITSPLAT;
                break;
            case HitsplatID.BLOCK_ME:
                hitIcon = Icons.OSRS_SELF_MISS_HITSPLAT;
                break;
            case HitsplatID.BLOCK_OTHER:
                hitIcon = Icons.OSRS_OTHER_MISS_HITSPLAT;
                break;
            case HitsplatID.CORRUPTION:
                hitIcon = Icons.OSRS_CORRUPTION_HITSPLAT;
                break;
            case HitsplatID.CYAN_DOWN:
                hitIcon = Icons.OSRS_ALT_UNCHARGE_HITSPLAT;
                break;
            case HitsplatID.CYAN_UP:
                hitIcon = Icons.OSRS_ALT_CHARGE_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_MAX_ME:
                hitIcon = Icons.OSRS_MAX_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_MAX_ME_CYAN:
                hitIcon = Icons.OSRS_MAX_SHIELD_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_MAX_ME_ORANGE:
                hitIcon = Icons.OSRS_MAX_ARMOUR_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_MAX_ME_POISE:
                hitIcon = Icons.OSRS_MAX_POISE_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_MAX_ME_WHITE:
                hitIcon = Icons.OSRS_MAX_UNCHARGE_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_MAX_ME_YELLOW:
                hitIcon = Icons.OSRS_MAX_CHARGE_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_ME:
                hitIcon = Icons.OSRS_SELF_DAMAGE_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_ME_CYAN:
                hitIcon = Icons.OSRS_SELF_SHIELD_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_ME_ORANGE:
                hitIcon = Icons.OSRS_SELF_ARMOUR_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_ME_POISE:
                hitIcon = Icons.OSRS_SELF_POISE_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_ME_WHITE:
                hitIcon = Icons.OSRS_SELF_UNCHARGE_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_ME_YELLOW:
                hitIcon = Icons.OSRS_SELF_CHARGE_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_OTHER:
                hitIcon = Icons.OSRS_OTHER_DAMAGE_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_OTHER_CYAN:
                hitIcon = Icons.OSRS_OTHER_SHIELD_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_OTHER_ORANGE:
                hitIcon = Icons.OSRS_OTHER_ARMOUR_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_OTHER_POISE:
                hitIcon = Icons.OSRS_OTHER_POISE_HITSPLAT;
                break;
            // Does not exist, defaulting to self for future support
            case HitsplatID.DAMAGE_OTHER_WHITE:
                hitIcon = Icons.OSRS_SELF_UNCHARGE_HITSPLAT;
                break;
            case HitsplatID.DAMAGE_OTHER_YELLOW:
                hitIcon = Icons.OSRS_OTHER_CHARGE_HITSPLAT;
                break;
            case HitsplatID.DISEASE:
                hitIcon = Icons.OSRS_DISEASE_HITSPLAT;
                break;
            case HitsplatID.DOOM:
                hitIcon = Icons.OSRS_DOOM_HITSPLAT;
                break;
            case HitsplatID.HEAL:
                hitIcon = Icons.OSRS_HEAL_HITSPLAT;
                break;
            case HitsplatID.POISON:
                hitIcon = Icons.OSRS_POISON_HITSPLAT;
                break;
            case HitsplatID.PRAYER_DRAIN:
                hitIcon = Icons.OSRS_PRAYER_DRAIN_HITSPLAT;
                break;
            case HitsplatID.SANITY_DRAIN:
                hitIcon = Icons.OSRS_SANITY_DRAIN_HITSPLAT;
                break;
            case HitsplatID.SANITY_RESTORE:
                hitIcon = Icons.OSRS_SANITY_RESTORE_HITSPLAT;
                break;
            case HitsplatID.VENOM:
                hitIcon = Icons.OSRS_VENOM_HITSPLAT;
                break;
            default:
                // unknown hitsplat ids are sometimes sent, skip them rather than guess
                return null;
        }
        return hitIcon;
    }
}
