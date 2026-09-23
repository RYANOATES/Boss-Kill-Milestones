package com.bosskillmilestones;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/** Uses elapsed time so changing the system clock cannot extend or shorten visibility. */
final class OverlayActivity
{
	private final LongSupplier clock;
	private volatile Long lastKill;

	OverlayActivity(LongSupplier clock)
	{
		this.clock = clock;
	}

	void killed()
	{
		lastKill = clock.getAsLong();
	}

	void reset()
	{
		lastKill = null;
	}

	boolean visible(boolean alwaysOn, int timeoutMinutes)
	{
		Long last = lastKill;
		return alwaysOn || (last != null && clock.getAsLong() - last
			< TimeUnit.MINUTES.toNanos(Math.max(1, Math.min(60, timeoutMinutes))));
	}
}
