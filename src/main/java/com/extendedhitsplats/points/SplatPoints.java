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

/**
 * Hitsplat positions, continuing the game's own staggered pattern. The game's four sit on a honeycomb:
 * columns {@link #COLUMN_WIDTH} apart, each column stepping up {@link #ROW_HEIGHT}, with odd columns
 * shifted down half a step. Extras fill that same honeycomb above the game's four.
 */
public class SplatPoints {
    // Distance between neighbouring columns; also how far the game's left and right hitsplats sit from the middle
    private static final int COLUMN_WIDTH = 16;
    // Distance between hitsplats in one column
    private static final int ROW_HEIGHT = 20;
    // The game's own four hitsplat positions, relative to the first one
    private static final Point[] VANILLA_POINTS = {
        new Point(0, 0),
        new Point(0, -ROW_HEIGHT),
        new Point(-COLUMN_WIDTH, -ROW_HEIGHT / 2),
        new Point(COLUMN_WIDTH, -ROW_HEIGHT / 2),
    };
    // Height of the lowest level of extras: just above the odd columns' game hitsplat
    private static final int FIRST_EXTRA_Y = -ROW_HEIGHT - ROW_HEIGHT / 2;

    /**
     * Offset of a hitsplat slot from the actor's hitsplat anchor, to the hitsplat's center.
     * Slots 0-3 match the game's layout. Extra slots (4 + 2 * index + side) go in the left or right half
     * of the honeycomb above the game's four, {@code perSide} columns wide, filling the lowest free level
     * first and the middle outward. The middle column's cells alternate between the halves, level by level.
     */
    public static Point offset(int slot, int perSide) {
        if (slot < VANILLA_POINTS.length) {
            return VANILLA_POINTS[slot];
        }
        int extra = slot - VANILLA_POINTS.length;
        int side = extra % 2;
        int index = extra / 2;
        int sign = side == ActorHitsplats.LEFT ? -1 : 1;

        // Each level is half a row above the last and alternates between odd and even columns
        for (int level = 0; ; level++) {
            int y = FIRST_EXTRA_Y - level * ROW_HEIGHT / 2;
            boolean oddColumns = level % 2 == 0;
            if (!oddColumns && ownsMiddle(side, y)) {
                if (index == 0) {
                    return new Point(0, y);
                }
                index--;
            }
            for (int column = oddColumns ? 1 : 2; column <= perSide; column += 2) {
                if (index == 0) {
                    return new Point(sign * column * COLUMN_WIDTH, y);
                }
                index--;
            }
        }
    }

    // The middle column's cells alternate between halves, starting with the left
    private static boolean ownsMiddle(int side, int y) {
        int cell = -y / ROW_HEIGHT;
        return cell % 2 == 0 ? side == ActorHitsplats.LEFT : side == ActorHitsplats.RIGHT;
    }
}
