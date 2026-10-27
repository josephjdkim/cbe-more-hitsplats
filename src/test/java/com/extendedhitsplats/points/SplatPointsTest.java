package com.extendedhitsplats.points;

import com.extendedhitsplats.utils.ActorHitsplats;
import net.runelite.api.Point;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SplatPointsTest
{
	private static Point left(int index, int perSide)
	{
		return SplatPoints.offset(4 + 2 * index, perSide);
	}

	private static Point right(int index, int perSide)
	{
		return SplatPoints.offset(5 + 2 * index, perSide);
	}

	@Test
	public void estimatedOwnHitSitsUnderTheGamesLeftHitsplat()
	{
		Point ownHit = SplatPoints.offset(ActorHitsplats.ESTIMATED_OWN_HIT_SLOT, 2);
		Point gameLeft = SplatPoints.offset(2, 2);
		assertEquals(gameLeft.getX(), ownHit.getX());
		assertEquals(gameLeft.getY() + 20, ownHit.getY());

		for (int perSide = 1; perSide <= 8; perSide++)
		{
			for (int slot = 0; slot < 4 + 2 * 40; slot++)
			{
				assertNotEquals(ownHit, SplatPoints.offset(slot, perSide));
			}
		}
	}

	@Test
	public void totalSitsBelowTheGamesFirstHitsplatAndYourEstimatedHit()
	{
		int hitsplatHeight = 23;
		int totalHeight = 30;
		Point total = SplatPoints.totalOffset(hitsplatHeight, totalHeight);
		int totalTop = total.getY() - totalHeight / 2;

		assertEquals(SplatPoints.offset(0, 2).getX(), total.getX());
		assertTrue(totalTop >= SplatPoints.offset(0, 2).getY() + hitsplatHeight / 2);
		assertTrue(totalTop >= SplatPoints.offset(ActorHitsplats.ESTIMATED_OWN_HIT_SLOT, 2).getY() + hitsplatHeight / 2);
	}

	@Test
	public void everySlotIsADistinctCellOnTheGamesHoneycomb()
	{
		for (int perSide = 1; perSide <= 8; perSide++)
		{
			Set<Point> seen = new HashSet<>();
			for (int slot = 0; slot < 4 + 2 * 40; slot++)
			{
				Point p = SplatPoints.offset(slot, perSide);
				assertTrue("slot " + slot + " reused " + p + " with " + perSide + " per side", seen.add(p));

				// columns 16 apart, rows 20 apart, odd columns shifted down half a row
				assertEquals(0, p.getX() % 16);
				int column = Math.abs(p.getX() / 16);
				assertTrue(column <= perSide);
				assertEquals(column % 2 == 0 ? 0 : 10, Math.floorMod(p.getY(), 20));
			}
		}
	}

	@Test
	public void extrasSitAboveTheGamesFourOnTheirOwnSide()
	{
		for (int index = 0; index < 30; index++)
		{
			Point l = left(index, 3);
			Point r = right(index, 3);
			assertTrue(l.getY() <= -30 && r.getY() <= -30);
			assertTrue(l.getX() <= 0 && r.getX() >= 0);
		}
	}

	@Test
	public void middleColumnAlternatesBetweenHalves()
	{
		Set<Integer> leftMiddle = new HashSet<>();
		Set<Integer> rightMiddle = new HashSet<>();
		for (int index = 0; index < 30; index++)
		{
			if (left(index, 2).getX() == 0)
			{
				leftMiddle.add(left(index, 2).getY());
			}
			if (right(index, 2).getX() == 0)
			{
				rightMiddle.add(right(index, 2).getY());
			}
		}
		assertTrue(leftMiddle.contains(-40) && leftMiddle.contains(-80));
		assertTrue(rightMiddle.contains(-60) && rightMiddle.contains(-100));
		assertTrue(leftMiddle.stream().noneMatch(rightMiddle::contains));
	}

	@Test
	public void fillsLowestLevelFirstThenMiddleOutward()
	{
		// two columns per side: left gets the middle at -40, right does not
		assertEquals(new Point(-16, -30), left(0, 2));
		assertEquals(new Point(0, -40), left(1, 2));
		assertEquals(new Point(-32, -40), left(2, 2));
		assertEquals(new Point(-16, -50), left(3, 2));

		assertEquals(new Point(16, -30), right(0, 2));
		assertEquals(new Point(32, -40), right(1, 2));
		assertEquals(new Point(16, -50), right(2, 2));
		assertEquals(new Point(0, -60), right(3, 2));
	}

	@Test
	public void gameSlotsMatchTheGame()
	{
		assertEquals(new Point(0, 0), SplatPoints.offset(0, 2));
		assertEquals(new Point(0, -20), SplatPoints.offset(1, 2));
		assertEquals(new Point(-16, -10), SplatPoints.offset(2, 2));
		assertEquals(new Point(16, -10), SplatPoints.offset(3, 2));
	}
}
