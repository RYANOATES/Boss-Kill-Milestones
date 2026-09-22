package com.bosskillmilestones;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Matches opposite signals one-to-one. Same-source kills remain separate. */
final class KillSignals
{
	static final int MATCH_TICKS = 8;
	private final List<Signal> signals = new ArrayList<>();

	boolean accept(String boss, boolean loot, int tick)
	{
		Iterator<Signal> iterator = signals.iterator();
		while (iterator.hasNext())
		{
			Signal signal = iterator.next();
			if (tick - signal.tick > MATCH_TICKS || tick < signal.tick)
			{
				iterator.remove();
			}
			else if (signal.boss.equals(boss) && signal.loot != loot)
			{
				iterator.remove();
				return false;
			}
		}
		signals.add(new Signal(boss, loot, tick));
		return true;
	}

	void clear()
	{
		signals.clear();
	}

	private static final class Signal
	{
		private final String boss;
		private final boolean loot;
		private final int tick;

		private Signal(String boss, boolean loot, int tick)
		{
			this.boss = boss;
			this.loot = loot;
			this.tick = tick;
		}
	}
}
