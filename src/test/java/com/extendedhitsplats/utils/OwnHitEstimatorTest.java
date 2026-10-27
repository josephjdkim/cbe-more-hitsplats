package com.extendedhitsplats.utils;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class OwnHitEstimatorTest
{
	private final Object target = new Object();
	private final OwnHitEstimator<Object> estimator = new OwnHitEstimator<>();

	// XP for a hit of 3: 4 Hitpoints XP and 12 style XP
	private void attack(Object target, int tick)
	{
		estimator.xpGained(target, "Goblin", tick, 4, 12);
	}

	@Test
	public void hitThatNeverComesIsEstimatedWhenDue()
	{
		attack(target, 10);
		assertTrue(estimator.tick(10).isEmpty());

		List<OwnHitEstimator.Estimate<Object>> due = estimator.tick(11);
		assertEquals(1, due.size());
		assertSame(target, due.get(0).target);
		assertEquals(3, due.get(0).damage);
		// shown once
		assertTrue(estimator.tick(12).isEmpty());
	}

	@Test
	public void hitThatComesInTimeNeedsNoEstimate()
	{
		attack(target, 10);
		assertFalse(estimator.hitsplatApplied(target, 11));
		for (int tick = 11; tick < 20; tick++)
		{
			assertTrue(estimator.tick(tick).isEmpty());
		}
	}

	@Test
	public void lateHitReplacesItsEstimateAndTheWaitIsLearned()
	{
		attack(target, 10);
		assertEquals(1, estimator.tick(11).size());
		assertTrue(estimator.hitsplatApplied(target, 12));

		// projectiles take two ticks from here on
		attack(target, 20);
		assertTrue(estimator.tick(21).isEmpty());
		assertFalse(estimator.hitsplatApplied(target, 22));
		assertTrue(estimator.tick(22).isEmpty());
	}

	@Test
	public void hitThatLandsOnTheAttackTickIsClaimedByItsXp()
	{
		assertFalse(estimator.hitsplatApplied(target, 10));
		attack(target, 10);
		assertTrue(estimator.tick(10).isEmpty());
		assertTrue(estimator.tick(11).isEmpty());
	}

	@Test
	public void onTimeHitIsForTheLatestAttackWhenAnEarlierOneWasLeftOut()
	{
		attack(target, 10);
		assertEquals(1, estimator.tick(11).size());
		attack(target, 12);

		// this hit is attack 12's; attack 10's estimate stays up
		assertFalse(estimator.hitsplatApplied(target, 13));
		assertTrue(estimator.tick(13).isEmpty());
	}

	@Test
	public void eachTargetWaitsForItsOwnHit()
	{
		Object other = new Object();
		attack(target, 10);
		attack(other, 10);
		assertFalse(estimator.hitsplatApplied(other, 11));

		List<OwnHitEstimator.Estimate<Object>> due = estimator.tick(11);
		assertEquals(1, due.size());
		assertSame(target, due.get(0).target);
	}

	@Test
	public void forgottenTargetGetsNoEstimate()
	{
		attack(target, 10);
		estimator.forget(target);
		assertTrue(estimator.tick(11).isEmpty());
	}

	@Test
	public void hitTooLateToMatchIsNotTakenForTheAttack()
	{
		attack(target, 10);
		estimator.tick(11);
		estimator.tick(10 + OwnHitEstimator.MAX_DELAY + 1);
		assertFalse(estimator.hitsplatApplied(target, 10 + OwnHitEstimator.MAX_DELAY + 2));
	}

	@Test
	public void damageComesFromStyleXpWhenItAgreesWithHitpointsXp()
	{
		// a hit of 22 gives 29.33 Hitpoints XP and 88 style XP; the game rounds XP down
		assertEquals(22, OwnHitEstimator.damage("Goblin", 29, 88));
		assertEquals(22, OwnHitEstimator.damage("Goblin", 30, 88));
		// a spell's damage only shows in Hitpoints XP
		assertEquals(22, OwnHitEstimator.damage("Goblin", 29, 0));
		// style XP from something else as well
		assertEquals(3, OwnHitEstimator.damage("Goblin", 4, 400));
		// any Hitpoints XP is at least 1 damage
		assertEquals(1, OwnHitEstimator.damage("Goblin", 1, 0));
	}

	@Test
	public void gemstoneCrabGivesLessXp()
	{
		// a hit of 25 there gives 87.5 style XP and 29.17 Hitpoints XP
		assertEquals(25, OwnHitEstimator.damage("Gemstone Crab", 29, 87));
		assertEquals(25, OwnHitEstimator.damage("Gemstone Crab", 29, 0));
	}
}
