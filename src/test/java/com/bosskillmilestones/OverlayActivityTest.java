package com.bosskillmilestones;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import static org.junit.Assert.*;

public class OverlayActivityTest
{
	@Test
	public void requiresKillAndExpiresAtTimeout()
	{
		AtomicLong clock = new AtomicLong();
		OverlayActivity activity = new OverlayActivity(clock::get);
		assertFalse(activity.visible(false, 5));
		activity.killed();
		assertTrue(activity.visible(false, 5));
		clock.set(TimeUnit.MINUTES.toNanos(5) - 1);
		assertTrue(activity.visible(false, 5));
		clock.incrementAndGet();
		assertFalse(activity.visible(false, 5));
		activity.killed();
		assertTrue(activity.visible(false, 5));
	}

	@Test
	public void everyKillRefreshesTimerAndSettingsApplyImmediately()
	{
		AtomicLong clock = new AtomicLong();
		OverlayActivity activity = new OverlayActivity(clock::get);
		activity.killed();
		clock.set(TimeUnit.MINUTES.toNanos(4));
		activity.killed();
		clock.set(TimeUnit.MINUTES.toNanos(6));
		assertTrue(activity.visible(false, 5));
		assertFalse(activity.visible(false, 1));
		assertTrue(activity.visible(true, 1));
		assertFalse(activity.visible(false, 1));
	}

	@Test
	public void resetHidesUntilNewKillUnlessAlwaysOn()
	{
		OverlayActivity activity = new OverlayActivity(() -> 100L);
		assertTrue(activity.visible(true, 5));
		activity.killed();
		activity.reset();
		assertFalse(activity.visible(false, 5));
		assertTrue(activity.visible(true, 5));
	}
}
