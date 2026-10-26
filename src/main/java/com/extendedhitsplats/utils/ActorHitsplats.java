package com.extendedhitsplats.utils;

import net.runelite.api.Hitsplat;

import java.util.ArrayList;
import java.util.List;

/**
 * The hitsplats currently on one actor. Each hitsplat keeps the slot it was given until it expires.
 * <p>
 * Slots 0-3 are the game's own four hitsplat positions. The game keeps one display object per position for
 * the actor's whole life, adding them in position order and reusing them for later hits, and the hitsplat
 * object in a {@code HitsplatApplied} event is that display object whenever the game shows the hit. So an
 * object seen before is shown at its known position, and a new one either opens the next position or, once
 * all four exist, is a hit the game didn't show.
 * <p>
 * Hits the game doesn't show go to an extra slot (4+) on the left or right side. Each new tick's extras go to
 * the opposite side from the previous tick's, so the two ticks a hitsplat stays on screen never share a side.
 * Extra slots interleave: 4 + 2 * index + side.
 */
public class ActorHitsplats
{
	public static final int VANILLA_SLOTS = 4;
	public static final int LEFT = 0;
	public static final int RIGHT = 1;

	private final List<ManagedHitsplat> hitsplats = new ArrayList<>();
	// The game's display objects for this actor, by position
	private final List<Hitsplat> gameObjects = new ArrayList<>();
	private int extraSide = RIGHT;
	private int extraTick = -1;

	public ManagedHitsplat add(Hitsplat hitsplat, int gameCycle, int tick)
	{
		prune(gameCycle);

		int position = gamePosition(hitsplat);
		if (position < 0)
		{
			position = lowestFreeExtraSlot(sideForTick(tick));
		}
		else if (isTaken(position))
		{
			// The game shows this hit in place of one that's still up (e.g. your own hit taking priority);
			// keep showing the pushed-out hit with the previous tick's extras
			moveToExtras(position, 1 - sideForTick(tick));
		}

		ManagedHitsplat managedHitsplat = new ManagedHitsplat(hitsplat, position);
		hitsplats.add(managedHitsplat);
		return managedHitsplat;
	}

	// Where the game shows this hit, or -1 if it doesn't
	private int gamePosition(Hitsplat hitsplat)
	{
		for (int i = 0; i < gameObjects.size(); i++)
		{
			if (gameObjects.get(i) == hitsplat)
			{
				return i;
			}
		}
		if (gameObjects.size() < VANILLA_SLOTS)
		{
			gameObjects.add(hitsplat);
			return gameObjects.size() - 1;
		}
		return -1;
	}

	private void moveToExtras(int position, int side)
	{
		for (int i = 0; i < hitsplats.size(); i++)
		{
			ManagedHitsplat managedHitsplat = hitsplats.get(i);
			if (managedHitsplat.position == position)
			{
				hitsplats.set(i, managedHitsplat.withPosition(lowestFreeExtraSlot(side)));
			}
		}
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
		while (isTaken(slot))
		{
			slot += 2;
		}
		return slot;
	}

	private boolean isTaken(int position)
	{
		return hitsplats.stream().anyMatch(h -> h.position == position);
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
