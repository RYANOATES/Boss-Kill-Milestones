package com.bosskillmilestones;

import org.junit.Test;
import static org.junit.Assert.*;

public class BossStatsTest
{
	@Test public void handlesLineBreakVariantsAndRejectsMalformedNumbers()
	{
		assertEquals(Integer.valueOf(1250), BossStats.parse("Kill Count:<BR/><col=fff>1,250</col><br />Best time: 1:30"));
		assertEquals(Integer.valueOf(24), BossStats.parse("Kills:\r\n24\r\n"));
		assertNull(BossStats.parse("Kills: 1,2,50"));
		assertNull(BossStats.parse(null));
		assertNull(BossStats.parse("Kills: 1,250\nCompletions: 100"));
	}
	@Test
	public void readsLabelledTotalsIncludingZero()
	{
		assertEquals(Integer.valueOf(1250), BossStats.parse("Kill count: <col=ff0000>1,250</col><br>Best time: 1:30"));
		assertEquals(Integer.valueOf(0), BossStats.parse("Kills:\n0\n"));
	}

	@Test
	public void rejectsAchievementsTimesAndAmbiguousModes()
	{
		assertNull(BossStats.parse("Tasks completed: 10/25\nBest time: 1:30"));
		assertNull(BossStats.parse("Kills: 10/25"));
		assertNull(BossStats.parse("Kills: 50\nKills: 100\n"));
		assertNull(BossStats.parse("Kill count: 9999999999999999"));
	}

	@Test
	public void bossNamesRejectOrdinaryNpcsAndShareKeys()
	{
		assertNull(BossNames.known("Goblin"));
		assertEquals("Sarachnis", BossNames.known("  SARACHNIS  "));
		assertEquals(BossNames.key(BossNames.known("The Nightmare")), BossNames.key(BossNames.known("Nightmare")));
	}
}
