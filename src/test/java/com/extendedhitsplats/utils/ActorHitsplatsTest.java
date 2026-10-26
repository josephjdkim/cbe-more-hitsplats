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

	// Like the game's hitsplat objects, which it reuses for later hits
	private static class RecycledHitsplat implements Hitsplat
	{
		int type = HitsplatID.DAMAGE_ME;
		int amount;
		int disappearsOn;

		@Override
		public int getHitsplatType()
		{
			return type;
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
	}

	@Test
	public void recycledHitsplatObjectsDoNotChangeOrReviveTrackedHitsplats()
	{
		ActorHitsplats hitsplats = new ActorHitsplats();
		RecycledHitsplat object = new RecycledHitsplat();
		object.amount = 7;
		object.disappearsOn = 50;
		ManagedHitsplat first = hitsplats.add(object, 0, 0);

		// the game reuses the object for another player's hit after the first one expires
		object.type = HitsplatID.DAMAGE_OTHER;
		object.amount = 3;
		object.disappearsOn = 110;
		assertEquals(HitsplatID.DAMAGE_ME, first.hitsplatType);
		assertEquals(7, first.amount);
		assertEquals(50, first.disappearsOnGameCycle);

		ManagedHitsplat second = hitsplats.add(object, 60, 2);
		// the first hit expired and stays gone, rather than piling up alongside the new one
		assertEquals(1, hitsplats.getHitsplats().size());
		assertEquals(second, hitsplats.getHitsplats().get(0));
		assertEquals(3, second.amount);
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

	private static RecycledHitsplat gameObject(int type, int amount, int disappearsOn)
	{
		RecycledHitsplat object = new RecycledHitsplat();
		object.type = type;
		object.amount = amount;
		object.disappearsOn = disappearsOn;
		return object;
	}

	// The game putting a new hit into one of its existing display objects
	private static RecycledHitsplat reuse(RecycledHitsplat object, int type, int amount, int disappearsOn)
	{
		object.type = type;
		object.amount = amount;
		object.disappearsOn = disappearsOn;
		return object;
	}

	@Test
	public void vanillaOnlyTicksDoNotFlipSides()
	{
		ActorHitsplats hitsplats = new ActorHitsplats();
		hitsplats.add(splat(1, 1000), 0, 0);
		hitsplats.add(splat(2, 1000), 0, 0);
		hitsplats.add(splat(3, 1000), 0, 0);
		RecycledHitsplat fourth = gameObject(HitsplatID.DAMAGE_OTHER, 4, 25);
		hitsplats.add(fourth, 0, 0);
		assertEquals(4, hitsplats.add(splat(5, 1000), 0, 0).position); // left
		// tick 1 only reuses a game position
		assertEquals(3, hitsplats.add(reuse(fourth, HitsplatID.DAMAGE_OTHER, 6, 1000), 30, 1).position);
		// tick 2 is the next tick with extras, so it flips to the right
		assertEquals(5, hitsplats.add(splat(7, 1000), 60, 2).position);
	}

	@Test
	public void reusedGameObjectKeepsItsPosition()
	{
		ActorHitsplats hitsplats = new ActorHitsplats();
		RecycledHitsplat first = gameObject(HitsplatID.DAMAGE_OTHER, 1, 10);
		RecycledHitsplat second = gameObject(HitsplatID.DAMAGE_OTHER, 2, 100);
		assertEquals(0, hitsplats.add(first, 0, 0).position);
		assertEquals(1, hitsplats.add(second, 0, 0).position);
		// position 0 is free, but the game put this hit in its position-1 object
		assertEquals(1, hitsplats.add(reuse(second, HitsplatID.DAMAGE_OTHER, 3, 150), 120, 4).position);
	}

	@Test
	public void newObjectsOnceAllFourExistAreExtras()
	{
		ActorHitsplats hitsplats = new ActorHitsplats();
		fillVanillaSlots(hitsplats, 10);
		// all four game hits have expired, but a new object still means the game didn't show this hit
		assertEquals(4, hitsplats.add(splat(9, 100), 20, 1).position);
	}

	@Test
	public void hitTheGamePushedOutMovesToTheExtras()
	{
		ActorHitsplats hitsplats = new ActorHitsplats();
		RecycledHitsplat[] game = new RecycledHitsplat[4];
		for (int i = 0; i < 4; i++)
		{
			game[i] = gameObject(HitsplatID.DAMAGE_OTHER, 10 + i, 100);
			hitsplats.add(game[i], 0, 0);
		}

		// your hit takes over position 2 while the other player's hit there is still up
		ManagedHitsplat yours = hitsplats.add(reuse(game[2], HitsplatID.DAMAGE_ME, 25, 130), 30, 1);
		assertEquals(2, yours.position);
		assertEquals(HitsplatID.DAMAGE_ME, yours.hitsplatType);

		ManagedHitsplat pushedOut = hitsplats.getHitsplats().stream().filter(h -> h.amount == 12).findFirst().get();
		assertTrue(pushedOut.position >= ActorHitsplats.VANILLA_SLOTS);
		assertEquals(HitsplatID.DAMAGE_OTHER, pushedOut.hitsplatType);
		assertEquals(100, pushedOut.disappearsOnGameCycle);
		// only one hit at position 2
		assertEquals(1, hitsplats.getHitsplats().stream().filter(h -> h.position == 2).count());
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
