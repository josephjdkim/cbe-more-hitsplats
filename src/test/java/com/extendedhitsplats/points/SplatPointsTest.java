package com.extendedhitsplats.points;

import net.runelite.api.Point;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SplatPointsTest
{
	// The overlay's cell size: the regular hitsplat sprite with a -2 gap
	private static final int CELL_WIDTH = 22;
	private static final int CELL_HEIGHT = 21;

	@Test
	public void extraSlotsNeverOverlap()
	{
		for (int perSide = 1; perSide <= 5; perSide++)
		{
			int slots = 4 + 2 * perSide * 4;
			for (int a = 4; a < slots; a++)
			{
				for (int b = a + 1; b < slots; b++)
				{
					Point pa = SplatPoints.offset(a, perSide, CELL_WIDTH, CELL_HEIGHT);
					Point pb = SplatPoints.offset(b, perSide, CELL_WIDTH, CELL_HEIGHT);
					boolean overlap = Math.abs(pa.getX() - pb.getX()) < CELL_WIDTH && Math.abs(pa.getY() - pb.getY()) < CELL_HEIGHT;
					assertFalse("slots " + a + " and " + b + " overlap with " + perSide + " per side", overlap);
				}
			}
		}
	}

	@Test
	public void sidesMirrorAndFillOutwardThenUp()
	{
		Point left0 = SplatPoints.offset(4, 2, CELL_WIDTH, CELL_HEIGHT);
		Point right0 = SplatPoints.offset(5, 2, CELL_WIDTH, CELL_HEIGHT);
		Point left1 = SplatPoints.offset(6, 2, CELL_WIDTH, CELL_HEIGHT);
		Point left2 = SplatPoints.offset(8, 2, CELL_WIDTH, CELL_HEIGHT);

		// inner columns line up with the game's left and right hitsplats
		assertEquals(SplatPoints.offset(2, 2, CELL_WIDTH, CELL_HEIGHT).getX(), left0.getX());
		assertEquals(SplatPoints.offset(3, 2, CELL_WIDTH, CELL_HEIGHT).getX(), right0.getX());
		assertTrue(left0.getX() < 0);
		assertEquals(-left0.getX(), right0.getX());
		assertEquals(left0.getY(), right0.getY());
		// second left hitsplat sits further out on the same row
		assertEquals(left0.getX() - CELL_WIDTH, left1.getX());
		assertEquals(left0.getY(), left1.getY());
		// third starts a new row above, back next to the middle
		assertEquals(left0.getX(), left2.getX());
		assertEquals(left0.getY() - CELL_HEIGHT, left2.getY());
		// first row sits half a row plus 4px above the game's highest hitsplat (centered 20 above the anchor)
		assertEquals(-20 - CELL_HEIGHT / 2 - 4, left0.getY());
	}
}
