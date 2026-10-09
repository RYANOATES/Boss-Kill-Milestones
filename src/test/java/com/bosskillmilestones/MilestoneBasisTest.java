package com.bosskillmilestones;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.bosskillmilestones.BossKillMilestonesConfig.MilestoneBasis.*;

public class MilestoneBasisTest
{
	@Test public void personalGoalsUseIndependentBasis()
	{
		BossKillMilestonesPlugin.Progress sessionGoal = new BossKillMilestonesPlugin.Progress("Sarachnis", 10, 125, "~1000", 25, ALL_TIME, SESSION);
		assertEquals(Integer.valueOf(1000), sessionGoal.milestoneCount());
		assertEquals(Integer.valueOf(10), sessionGoal.goalCount());
		assertEquals("10", sessionGoal.goalCountText());
		BossKillMilestonesPlugin.Progress savedGoal = new BossKillMilestonesPlugin.Progress("Sarachnis", 10, 125, "Unknown", 150, SESSION, SINCE_ACTIVATION);
		assertEquals(Integer.valueOf(125), savedGoal.goalCount());
		assertEquals("125", savedGoal.goalCountText());
		BossKillMilestonesPlugin.Progress legacy = new BossKillMilestonesPlugin.Progress("Sarachnis", 10, 125, "~1000", 1500);
		assertEquals(Integer.valueOf(1000), legacy.goalCount());
		assertEquals("~1000", legacy.goalCountText());
	}

	@Test public void sameKillUsesSelectedCounter()
	{
		assertEquals(Celebration.TEN, Celebration.select(SESSION.count(26, 10, 1250), 10, 0, false));
		assertEquals(Celebration.TWO_FIFTY, Celebration.select(ALL_TIME.count(26, 10, 1250), 10, 0, false));
		assertNull(Celebration.select(SINCE_ACTIVATION.count(26, 10, 1250), 10, 0, false));
	}

	@Test public void progressFollowsBasisWithoutChangingRecordedCounts()
	{
		BossKillMilestonesPlugin.Progress session = new BossKillMilestonesPlugin.Progress("Sarachnis", 9, 26, "~1249", 1300, SESSION);
		BossKillMilestonesPlugin.Progress lifetime = new BossKillMilestonesPlugin.Progress("Sarachnis", 9, 26, "~1249", 1300, ALL_TIME);
		assertEquals("1 away", session.nextMilestoneText());
		assertEquals("1 away", lifetime.nextMilestoneText());
		assertEquals(Integer.valueOf(1249), lifetime.milestoneCount());
		assertEquals(26, lifetime.saved);
		assertEquals(1300, lifetime.goal);
	}

	@Test public void unknownLifetimeDoesNotInventMilestoneProgress()
	{
		BossKillMilestonesPlugin.Progress progress = new BossKillMilestonesPlugin.Progress("Sarachnis", 9, 26, "Unknown", 0, ALL_TIME);
		assertNull(progress.milestoneCount());
		assertEquals("Sync lifetime total", progress.nextMilestoneText());
		assertEquals(Integer.valueOf(26), SINCE_ACTIVATION.count(26, 9, null));
	}
}
