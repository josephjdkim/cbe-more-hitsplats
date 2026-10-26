package com.extendedhitsplats.utils;

import net.runelite.api.Hitsplat;

import java.util.ArrayList;
import java.util.List;

/**
 * The hitsplats currently on one actor. Each hitsplat keeps the slot it was given until it expires.
 * <p>
 * Slots 0-3 mirror the game's own four hitsplat slots, so the plugin knows which hitsplats the game is
 * already drawing (and where). Anything the game has no room for goes to an extra slot (4+) on the left or
 * right side. Each new tick's extras go to the opposite side from the previous tick's, so the two ticks a
 * hitsplat stays on screen never share a side. Extra slots interleave: 4 + 2 * index + side.
 */
public class ActorHitsplats
{
	public static final int VANILLA_SLOTS = 4;
	public static final int LEFT = 0;
	public static final int RIGHT = 1;

	private final List<ManagedHitsplat> hitsplats = new ArrayList<>();
	private final int[] vanillaSlotExpiry = new int[VANILLA_SLOTS];
	private int vanillaCursor;
	private int extraSide = RIGHT;
	private int extraTick = -1;

	public ManagedHitsplat add(Hitsplat hitsplat, int gameCycle, int tick)
	{
		prune(gameCycle);

		int position = takeVanillaSlot(hitsplat, gameCycle);
		if (position < 0)
		{
			position = lowestFreeExtraSlot(sideForTick(tick));
		}

		ManagedHitsplat managedHitsplat = new ManagedHitsplat(hitsplat, position);
		hitsplats.add(managedHitsplat);
		return managedHitsplat;
	}

	// Same slot choice as the game: round-robin over the free slots, restarting at 0 once all four are free.
	// When all four are busy the game drops the new hitsplat (some types replace one instead, which isn't mirrored).
	private int takeVanillaSlot(Hitsplat hitsplat, int gameCycle)
	{
		boolean allFree = true;
		boolean allBusy = true;
		for (int expiry : vanillaSlotExpiry)
		{
			if (expiry > gameCycle)
			{
				allFree = false;
			}
			else
			{
				allBusy = false;
			}
		}

		if (allBusy)
		{
			return -1;
		}
		if (allFree)
		{
			vanillaCursor = 0;
		}

		for (int i = 0; i < VANILLA_SLOTS; i++)
		{
			int slot = vanillaCursor;
			vanillaCursor = (vanillaCursor + 1) % VANILLA_SLOTS;
			if (vanillaSlotExpiry[slot] <= gameCycle)
			{
				vanillaSlotExpiry[slot] = hitsplat.getDisappearsOnGameCycle();
				return slot;
			}
		}
		return -1;
	}

	// Flips sides on each new tick that brings extras; starts on the left when no extras are showing
	private int sideForTick(int tick)
	{
		if (tick != extraTick)
		{
			boolean extrasShowing = hitsplats.stream().anyMatch(h -> h.position >= VANILLA_SLOTS);
			extraSide = extrasShowing ? 1 - extraSide : LEFT;
			extraTick = tick;
		}
		return extraSide;
	}

	private int lowestFreeExtraSlot(int side)
	{
		int slot = VANILLA_SLOTS + side;
		boolean taken = true;
		while (taken)
		{
			taken = false;
			for (ManagedHitsplat managedHitsplat : hitsplats)
			{
				if (managedHitsplat.position == slot)
				{
					slot += 2;
					taken = true;
					break;
				}
			}
		}
		return slot;
	}

	/**
	 * Removes expired hitsplats.
	 *
	 * @return true if no hitsplats are left
	 */
	public boolean prune(int gameCycle)
	{
		hitsplats.removeIf(h -> h.disappearsOnGameCycle <= gameCycle);
		return hitsplats.isEmpty();
	}

	public List<ManagedHitsplat> getHitsplats()
	{
		return hitsplats;
	}
}
