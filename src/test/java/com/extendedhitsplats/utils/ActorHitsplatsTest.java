package com.extendedhitsplats.utils;

import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ActorHitsplatsTest
{
	private static Hitsplat splat(int amount, int disappearsOn)
	{
		return new Hitsplat()
		{
			@Override
			public int getHitsplatType()
			{
				return HitsplatID.DAMAGE_ME;
			}

			@Override
			public int getAmount()
			{
				return amount;
			}

			@Override
			public int getDisappearsOnGameCycle()
			{
				return disappearsOn;
			}
		};
	}


	private static void fillVanillaSlots(ActorHitsplats hitsplats, int expiry)
	{
		for (int i = 0; i < ActorHitsplats.VANILLA_SLOTS; i++)
		{
			hitsplats.add(splat(i, expiry), 0, 0);
		}
	}

	@Test
	public void fillsVanillaSlotsThenLeftExtras()
	{
		ActorHitsplats hitsplats = new ActorHitsplats();
		for (int i = 0; i < 4; i++)
		{
			assertEquals(i, hitsplats.add(splat(i, 100), 0, 0).position);
		}
		// same tick: all extras on the left, 4 + 2 * index
		assertEquals(4, hitsplats.add(splat(4, 100), 0, 0).position);
		assertEquals(6, hitsplats.add(splat(5, 100), 0, 0).position);
		assertEquals(8, hitsplats.add(splat(6, 100), 0, 0).position);
	}

	@Test
	public void eachTickUsesTheOtherSide()
	{
		ActorHitsplats hitsplats = new ActorHitsplats();
		fillVanillaSlots(hitsplats, 1000);
		assertEquals(4, hitsplats.add(splat(1, 60), 0, 0).position);  // left
		assertEquals(5, hitsplats.add(splat(2, 90), 30, 1).position); // right
		assertEquals(7, hitsplats.add(splat(3, 90), 30, 1).position);
		// tick 2: the tick-0 extra has expired, its left slot is free again
		assertEquals(4, hitsplats.add(splat(4, 120), 60, 2).position);
		// tick 3: back to the right, which still holds tick 1's extras
		assertEquals(9, hitsplats.add(splat(5, 150), 89, 3).position);
	}

	@Test
	public void startsOnTheLeftWhenNoExtrasShowing()
	{
		ActorHitsplats hitsplats = new ActorHitsplats();
		fillVanillaSlots(hitsplats, 1000);
		assertEquals(4, hitsplats.add(splat(1, 20), 0, 0).position);
		// two ticks later the left extra is gone, so the next extras start left again
		assertEquals(4, hitsplats.add(splat(2, 100), 60, 2).position);
	}

	@Test
	public void vanillaOnlyTicksDoNotFlipSides()
	{
		ActorHitsplats hitsplats = new ActorHitsplats();
		hitsplats.add(splat(1, 1000), 0, 0);
		hitsplats.add(splat(2, 1000), 0, 0);
		hitsplats.add(splat(3, 1000), 0, 0);
		hitsplats.add(splat(4, 25), 0, 0); // vanilla slot 3, frees up before tick 1
		assertEquals(4, hitsplats.add(splat(5, 1000), 0, 0).position); // left
		// tick 1 only uses a game slot
		assertEquals(3, hitsplats.add(splat(6, 1000), 30, 1).position);
		// tick 2 is the next tick with extras, so it flips to the right
		assertEquals(5, hitsplats.add(splat(7, 1000), 60, 2).position);
	}

	@Test
	public void vanillaSlotsAreRoundRobin()
	{
		ActorHitsplats hitsplats = new ActorHitsplats();
		hitsplats.add(splat(1, 10), 0, 0);  // slot 0, expires first
		hitsplats.add(splat(2, 100), 0, 0); // slot 1
		// slot 0 is free again, but the game continues from slot 2
		assertEquals(2, hitsplats.add(splat(3, 100), 20, 0).position);
		assertEquals(3, hitsplats.add(splat(4, 100), 20, 0).position);
		assertEquals(0, hitsplats.add(splat(5, 100), 20, 0).position);
	}

	@Test
	public void restartsAtSlotZeroOnceAllFree()
	{
		ActorHitsplats hitsplats = new ActorHitsplats();
		hitsplats.add(splat(1, 10), 0, 0);
		hitsplats.add(splat(2, 10), 0, 0);
		assertEquals(0, hitsplats.add(splat(3, 100), 20, 0).position);
	}

	@Test
	public void hitsplatsKeepTheirSlotWhenOthersExpire()
	{
		ActorHitsplats hitsplats = new ActorHitsplats();
		fillVanillaSlots(hitsplats, 100);
		ManagedHitsplat early = hitsplats.add(splat(4, 10), 0, 0);
		ManagedHitsplat late = hitsplats.add(splat(5, 100), 0, 0);
		assertEquals(4, early.position);
		assertEquals(6, late.position);

		hitsplats.prune(20);
		assertEquals(6, late.position);
		// same tick, same side: the freed slot is reused
		assertEquals(4, hitsplats.add(splat(6, 100), 20, 0).position);
	}

	@Test
	public void pruneReportsEmpty()
	{
		ActorHitsplats hitsplats = new ActorHitsplats();
		hitsplats.add(splat(1, 10), 0, 0);
		assertTrue(hitsplats.prune(10));
	}
}
