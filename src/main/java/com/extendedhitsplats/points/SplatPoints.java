/*
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

package com.extendedhitsplats.points;

import com.extendedhitsplats.utils.ActorHitsplats;
import net.runelite.api.Point;

public class SplatPoints {
    // How far the game's left and right hitsplats sit from the middle
    private static final int VANILLA_SIDE_X = 16;
    // The game's own four hitsplat positions, relative to the first one
    private static final Point[] VANILLA_POINTS = {
        new Point(0, 0),
        new Point(0, -20),
        new Point(-VANILLA_SIDE_X, -10),
        new Point(VANILLA_SIDE_X, -10),
    };
    // Slot 1, the highest of the game's four, is centered this far above the anchor
    private static final int VANILLA_TOP = 20;
    // Extra space between the game's hitsplats and the first row of extras
    private static final int EXTRA_LIFT = 4;

    /**
     * Offset of a hitsplat slot from the actor's hitsplat anchor, to the hitsplat's center.
     * Slots 0-3 match the game's layout. Extra slots (4 + 2 * index + side) go in a left or right half
     * above the game's cluster: rows of {@code perSide} filling outward from the middle, stacking upward.
     * Each half's inner column lines up with the game's hitsplat on that side.
     */
    public static Point offset(int slot, int perSide, int cellWidth, int cellHeight) {
        if (slot < VANILLA_POINTS.length) {
            return VANILLA_POINTS[slot];
        }
        int extra = slot - VANILLA_POINTS.length;
        int side = extra % 2;
        int index = extra / 2;
        int row = index / perSide;
        int column = index % perSide;
        int x = VANILLA_SIDE_X + column * cellWidth;
        // First row starts half a row (plus a little breathing room) above the game's highest hitsplat
        int y = -VANILLA_TOP - cellHeight / 2 - EXTRA_LIFT - row * cellHeight;
        return new Point(side == ActorHitsplats.LEFT ? -x : x, y);
    }
}
