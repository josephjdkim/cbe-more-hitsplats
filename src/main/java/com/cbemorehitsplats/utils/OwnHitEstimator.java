package com.cbemorehitsplats.utils;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Works out your own hits from your combat XP, for when the server leaves your hitsplat out. It sends only so
 * many hitsplats per actor per tick, and in a crowd yours can miss the cut.
 * <p>
 * The XP for a hit arrives on the tick you attack, and the hitsplat once the hit lands: a tick later for melee
 * on an NPC, longer for projectiles. Each XP gain waits that long for your hitsplat, and is shown as an estimate
 * if none comes. The wait is learned from the hitsplats that do come; one that turns up after its estimate was
 * shown replaces it.
 * <p>
 * Misses give no XP, so they can't be estimated. XP from several hits in one tick (multi-hit weapons, area
 * attacks) makes one estimate, put on the actor you're attacking.
 *
 * @param <T> the actors you hit
 */
public class OwnHitEstimator<T>
{
	// Hitpoints XP per point of damage
	private static final double HITPOINTS_XP_PER_DAMAGE = 4.0 / 3;
	// Attack, Strength, Defence and Ranged XP together per point of damage, in every attack style
	private static final double STYLE_XP_PER_DAMAGE = 4;
	// NPCs that give more or less combat XP than usual, by name
	private static final Map<String, Double> XP_MODIFIERS = ImmutableMap.of(
		"Gemstone Crab", 0.875
	);
	// Ticks from attacking to the hit landing, until a hitsplat has shown otherwise: melee on an NPC
	static final int DEFAULT_DELAY = 1;
	// How long an XP gain waits for a late hitsplat
	static final int MAX_DELAY = 6;

	public static class Estimate<T>
	{
		public final T target;
		public final int damage;

		Estimate(T target, int damage)
		{
			this.target = target;
			this.damage = damage;
		}
	}

	private static class Attack<T>
	{
		final T target;
		final int damage;
		final int tick;
		boolean estimateShown;

		Attack(T target, int damage, int tick)
		{
			this.target = target;
			this.damage = damage;
			this.tick = tick;
		}
	}

	// Attacks whose hitsplat hasn't come, oldest first
	private final List<Attack<T>> attacks = new ArrayList<>();
	// Your hitsplats that came before their XP, for hits that land on the tick you attack
	private final List<T> earlyHits = new ArrayList<>();
	private int earlyHitsTick = -1;
	private int delay = DEFAULT_DELAY;

	/**
	 * Records the combat XP you gained this tick.
	 *
	 * @param styleXp Attack, Strength, Defence and Ranged XP gained, or 0 if a spell gave some of it
	 */
	public void xpGained(T target, String targetName, int tick, int hitpointsXp, int styleXp)
	{
		if (earlyHitsTick == tick && earlyHits.remove(target))
		{
			delay = 0;
			return;
		}
		attacks.add(new Attack<>(target, damage(targetName, hitpointsXp, styleXp), tick));
	}

	/**
	 * Records your hitsplat arriving on an actor.
	 *
	 * @return true if it replaces an estimate that's already showing
	 */
	public boolean hitsplatApplied(T target, int tick)
	{
		Attack<T> attack = oldestAttackOn(target, false);
		if (attack == null)
		{
			// Arrived after its estimate was shown
			attack = oldestAttackOn(target, true);
		}
		if (attack == null)
		{
			if (earlyHitsTick != tick)
			{
				earlyHits.clear();
				earlyHitsTick = tick;
			}
			earlyHits.add(target);
			return false;
		}

		attacks.remove(attack);
		delay = Math.min(tick - attack.tick, MAX_DELAY);
		return attack.estimateShown;
	}

	// An estimate shown earlier means that attack's hitsplat was left out, so a hitsplat that's on time is for
	// a later attack
	private Attack<T> oldestAttackOn(T target, boolean estimateShown)
	{
		for (Attack<T> attack : attacks)
		{
			if (attack.target == target && attack.estimateShown == estimateShown)
			{
				return attack;
			}
		}
		return null;
	}

	/**
	 * Call once per tick, after the tick's hitsplats.
	 *
	 * @return estimates to show now, for attacks whose hitsplat is due and hasn't come
	 */
	public List<Estimate<T>> tick(int tick)
	{
		List<Estimate<T>> due = new ArrayList<>();
		for (Iterator<Attack<T>> it = attacks.iterator(); it.hasNext(); )
		{
			Attack<T> attack = it.next();
			if (tick - attack.tick > MAX_DELAY)
			{
				it.remove();
			}
			else if (!attack.estimateShown && tick - attack.tick >= delay)
			{
				attack.estimateShown = true;
				due.add(new Estimate<>(attack.target, attack.damage));
			}
		}
		return due;
	}

	public void forget(T target)
	{
		attacks.removeIf(attack -> attack.target == target);
		earlyHits.remove(target);
	}

	public void clear()
	{
		attacks.clear();
		earlyHits.clear();
	}

	static int damage(String targetName, int hitpointsXp, int styleXp)
	{
		double modifier = XP_MODIFIERS.getOrDefault(targetName, 1.0);
		int fromHitpoints = Math.max(1, (int) Math.round(hitpointsXp / (HITPOINTS_XP_PER_DAMAGE * modifier)));
		if (styleXp <= 0)
		{
			return fromHitpoints;
		}
		// The game rounds XP down to whole points, and style XP is three times finer per point of damage. Use it
		// when it agrees, as it should unless something else gave XP this tick.
		int fromStyle = (int) Math.round(styleXp / (STYLE_XP_PER_DAMAGE * modifier));
		return Math.abs(fromStyle - fromHitpoints) <= 1 ? Math.max(1, fromStyle) : fromHitpoints;
	}
}
