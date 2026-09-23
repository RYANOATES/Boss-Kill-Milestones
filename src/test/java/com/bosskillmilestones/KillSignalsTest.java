package com.bosskillmilestones;

import org.junit.Test;
import static org.junit.Assert.*;

public class KillSignalsTest
{
	@Test
	public void bothEventOrdersCountOnce()
	{
		for (boolean lootFirst : new boolean[]{false, true})
		{
			KillSignals signals = new KillSignals();
			assertTrue(signals.accept("sarachnis", lootFirst, 10));
			assertFalse(signals.accept("sarachnis", !lootFirst, 12));
		}
	}

	@Test
	public void matchesMultipleKillsOneToOne()
	{
		KillSignals signals = new KillSignals();
		assertTrue(signals.accept("boss", true, 10));
		assertTrue(signals.accept("boss", true, 11));
		assertFalse(signals.accept("boss", false, 12));
		assertFalse(signals.accept("boss", false, 13));
		assertTrue(signals.accept("boss", false, 14));
	}

	@Test
	public void differentBossesAndOldSignalsDoNotMatch()
	{
		KillSignals signals = new KillSignals();
		assertTrue(signals.accept("sarachnis", true, 10));
		assertTrue(signals.accept("zulrah", false, 11));
		assertTrue(signals.accept("sarachnis", false, 10 + KillSignals.MATCH_TICKS + 1));
	}

	@Test
	public void accountChangeClearsPairing()
	{
		KillSignals signals = new KillSignals();
		assertTrue(signals.accept("boss", true, 10));
		signals.clear();
		assertTrue(signals.accept("boss", false, 11));
	}
}
