package com.bosskillmilestones;

import org.junit.Test;
import static org.junit.Assert.*;

public class BossStatsTest
{
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
