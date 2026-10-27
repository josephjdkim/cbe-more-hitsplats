package com.cbemorehitsplats.utils;

import com.google.common.collect.MapMaker;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
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
 * <p>
 * Also kept here: your own hit when it's estimated from XP, which has a slot of its own, and the damage the
 * actor took in its latest tick, for the large total.
 */
@Slf4j
public class ActorHitsplats
{
	public static final int VANILLA_SLOTS = 4;
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int ESTIMATED_OWN_HIT_SLOT = -1;
	// The most hitsplats the server sends for one actor in one tick; past it, it leaves hits out. Seen at the
	// Gemstone Crab, and the top of the hitsplat limit the game's protocol has carried since revision 237.
	public static final int SERVER_HITSPLAT_LIMIT = 20;
	// Fewer hits than this in a tick need no total
	private static final int MIN_HITS_FOR_TOTAL = 2;

	private final List<ManagedHitsplat> hitsplats = new ArrayList<>();
	private ManagedHitsplat estimatedOwnHit;
	private int totalTick = -1;
	private int totalDamage;
	private int totalHits;
	// Every hitsplat the server sent in the total's tick, of any type
	private int tickHitsplats;
	private boolean totalIncludesEstimate;
	private int totalDisappearsOnGameCycle;
	// From the boss HP bar, when the actor has one
	private boolean totalExactDamageKnown;
	private int totalExactDamage;
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
		startTick(tick);
		tickHitsplats++;
		addToTotal(managedHitsplat);
		return managedHitsplat;
	}

	// Your hit, worked out from your XP because the server didn't send it
	public void addEstimatedOwnHit(int amount, int gameCycle, int tick)
	{
		estimatedOwnHit = ManagedHitsplat.estimatedOwnHit(amount, gameCycle);
		startTick(tick);
		totalIncludesEstimate = true;
		addToTotal(estimatedOwnHit);
	}

	// The server sent your hit after all
	public void removeEstimatedOwnHit()
	{
		estimatedOwnHit = null;
	}

	public ManagedHitsplat getEstimatedOwnHit()
	{
		return estimatedOwnHit;
	}

	// The total and the hitsplat count are for the latest tick that brought hits, of any type
	private void startTick(int tick)
	{
		if (tick != totalTick)
		{
			totalTick = tick;
			totalDamage = 0;
			totalHits = 0;
			tickHitsplats = 0;
			totalIncludesEstimate = false;
			totalDisappearsOnGameCycle = 0;
			totalExactDamageKnown = false;
			totalExactDamage = 0;
		}
	}

	private void addToTotal(ManagedHitsplat hit)
	{
		if (!isDamage(hit.hitsplatType))
		{
			return;
		}
		totalDamage += hit.amount;
		totalHits++;
		// Shown for as long as the tick's hitsplats
		totalDisappearsOnGameCycle = Math.max(totalDisappearsOnGameCycle, hit.disappearsOnGameCycle);
	}

	// Heals and drains of things other than hitpoints don't add to the total
	private static boolean isDamage(int hitsplatType)
	{
		switch (hitsplatType)
		{
			case HitsplatID.HEAL:
			case HitsplatID.PRAYER_DRAIN:
			case HitsplatID.SANITY_DRAIN:
			case HitsplatID.SANITY_RESTORE:
			case HitsplatID.CYAN_UP:
			case HitsplatID.CYAN_DOWN:
				return false;
			default:
				return true;
		}
	}

	/**
	 * The exact damage taken this tick, from HP lost, which counts the hits the server left out. Ignored for a
	 * tick without hitsplats, as HP can drop for other reasons.
	 */
	public void setExactDamage(int damage, int tick)
	{
		if (tick == totalTick)
		{
			totalExactDamageKnown = true;
			// HP can go up, when the actor heals
			totalExactDamage = Math.max(damage, 0);
		}
	}

	/**
	 * True if the latest tick's total can't be missing hits: the HP bar showed the HP the actor lost, or the
	 * server sent fewer than {@value #SERVER_HITSPLAT_LIMIT} hitsplats, so it left none out. A total with your
	 * estimated hit is only confirmed by the HP bar, as the estimate is worked out from XP.
	 */
	public boolean isTotalConfirmed()
	{
		if (totalExactDamageKnown && totalExactDamage >= totalDamage)
		{
			return true;
		}
		return tickHitsplats < SERVER_HITSPLAT_LIMIT && !totalIncludesEstimate;
	}

	// Hitsplats of any type the server sent this actor in the tick
	public int hitsplatsOnTick(int tick)
	{
		return tick == totalTick ? tickHitsplats : 0;
	}

	/**
	 * The HP the HP bar showed lost in the tick beyond what the hitsplats add up to, when the server sent fewer
	 * than {@value #SERVER_HITSPLAT_LIMIT} hitsplats. Under the limit no hits should be missing, so it should be 0.
	 */
	public int missingDamageUnderLimit(int tick)
	{
		if (tick != totalTick || !totalExactDamageKnown || tickHitsplats >= SERVER_HITSPLAT_LIMIT)
		{
			return 0;
		}
		return Math.max(totalExactDamage - totalDamage, 0);
	}

	/**
	 * The damage taken in the actor's latest tick, while that tick's hitsplats are up and if it took
	 * {@value #MIN_HITS_FOR_TOTAL} or more hits. Uses the exact damage when it's more than the hitsplats add up
	 * to; it can be less when the actor also healed.
	 */
	public OptionalInt visibleTotal(int gameCycle)
	{
		int damage = Math.max(totalDamage, totalExactDamage);
		// Exact damage over the hitsplats' means some hits were left out, so there were several
		boolean severalHits = totalHits >= MIN_HITS_FOR_TOTAL || damage > totalDamage;
		return severalHits && totalDisappearsOnGameCycle > gameCycle
			? OptionalInt.of(damage)
			: OptionalInt.empty();
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
		if (estimatedOwnHit != null && estimatedOwnHit.disappearsOnGameCycle <= gameCycle)
		{
			estimatedOwnHit = null;
		}
		return hitsplats.isEmpty() && estimatedOwnHit == null;
	}

	public List<ManagedHitsplat> getHitsplats()
	{
		return hitsplats;
	}
}
