package com.extendedhitsplats.utils;

import com.google.common.collect.MapMaker;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Hitsplat;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The hitsplats currently on one actor. Each hitsplat keeps its place until it expires.
 * <p>
 * The game keeps one display object per hitsplat position for the actor's whole life, adding them in position
 * order and reusing them for later hits. The hitsplat object in a {@code HitsplatApplied} event is that display
 * object whenever the game shows the hit, and a new object that's never used again when it doesn't. So an object
 * seen before is shown at its known position, and a new one either opens the next position or, once four exist,
 * is a hit the game didn't show.
 * <p>
 * That guess can be wrong: the server sends a position limit with each hit, so the game can leave a hit unshown
 * before it has four positions, or open a fifth; and tracking can start after the game already has positions.
 * Wrong guesses heal, because only the game's own objects come back: an object first taken for an extra that is
 * seen again is one of the game's positions from then on.
 * <p>
 * Hits the game doesn't show go to an extra slot (4+) on the left or right side. Each new tick's extras go to
 * the opposite side from the previous tick's, so the two ticks a hitsplat stays on screen never share a side.
 * Extra slots interleave: 4 + 2 * index + side.
 */
@Slf4j
public class ActorHitsplats
{
	public static final int VANILLA_SLOTS = 4;
	public static final int LEFT = 0;
	public static final int RIGHT = 1;

	private final List<ManagedHitsplat> hitsplats = new ArrayList<>();
	// The game's display objects for this actor, by position. Can include a hit the game didn't show, taken for a
	// new position; it's never seen again, so it only fills a place.
	private final List<Hitsplat> gameObjects = new ArrayList<>();
	// Positions whose object has come back, proving it's the game's
	private final BitSet reusedPositions = new BitSet();
	// Objects of hits taken for extras, in case one comes back. Weak and by identity: the game keeps its own
	// objects alive and drops the ones for hits it didn't show.
	private final Set<Hitsplat> extraObjects = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());
	private int extraSide = RIGHT;
	private int extraTick = -1;

	public ManagedHitsplat add(Hitsplat hitsplat, int gameCycle, int tick)
	{
		prune(gameCycle);

		int position = gamePosition(hitsplat);
		boolean shownByGame = position >= 0;
		if (!shownByGame)
		{
			position = lowestFreeExtraSlot(sideForTick(tick));
		}
		else if (isTaken(true, position))
		{
			// The game shows this hit in place of one that's still up (e.g. your own hit taking priority);
			// keep showing the pushed-out hit with the previous tick's extras
			moveToExtras(position, 1 - sideForTick(tick));
		}

		ManagedHitsplat managedHitsplat = new ManagedHitsplat(hitsplat, shownByGame, position);
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
				markReused(i);
				return i;
			}
		}
		if (extraObjects.remove(hitsplat))
		{
			gameObjects.add(hitsplat);
			log.debug("A hitsplat taken for an extra was the game's; tracking {} game positions", gameObjects.size());
			markReused(gameObjects.size() - 1);
			return gameObjects.size() - 1;
		}
		if (gameObjects.size() < VANILLA_SLOTS)
		{
			gameObjects.add(hitsplat);
			return gameObjects.size() - 1;
		}
		extraObjects.add(hitsplat);
		return -1;
	}

	private void markReused(int position)
	{
		if (!reusedPositions.get(position))
		{
			reusedPositions.set(position);
			if (reusedPositions.cardinality() == VANILLA_SLOTS + 1)
			{
				log.debug("The game has shown hitsplats at more than {} positions on one actor", VANILLA_SLOTS);
			}
		}
	}

	private void moveToExtras(int position, int side)
	{
		for (int i = 0; i < hitsplats.size(); i++)
		{
			ManagedHitsplat managedHitsplat = hitsplats.get(i);
			if (managedHitsplat.shownByGame && managedHitsplat.position == position)
			{
				hitsplats.set(i, managedHitsplat.asExtra(lowestFreeExtraSlot(side)));
			}
		}
	}

	// Flips sides on each new tick that brings extras; starts on the left when no extras are showing
	private int sideForTick(int tick)
	{
		if (tick != extraTick)
		{
			boolean extrasShowing = hitsplats.stream().anyMatch(h -> !h.shownByGame);
			extraSide = extrasShowing ? 1 - extraSide : LEFT;
			extraTick = tick;
		}
		return extraSide;
	}

	private int lowestFreeExtraSlot(int side)
	{
		int slot = VANILLA_SLOTS + side;
		while (isTaken(false, slot))
		{
			slot += 2;
		}
		return slot;
	}

	private boolean isTaken(boolean shownByGame, int position)
	{
		return hitsplats.stream().anyMatch(h -> h.shownByGame == shownByGame && h.position == position);
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
