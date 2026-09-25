package com.bosskillmilestones;

import org.junit.Test;
import static org.junit.Assert.*;

public class CelebrationTest
{
	@Test public void escalatesAndRepeatsWithoutOverlappingSounds()
	{
		assertEquals(Celebration.TEN, Celebration.select(10, 10, 10, false));
		assertEquals(Celebration.TWENTY_FIVE, Celebration.select(25, 25, 25, false));
		assertEquals(Celebration.FIFTY, Celebration.select(50, 50, 10, false));
		assertEquals(Celebration.FIFTY, Celebration.select(150, 10, 10, false));
		assertEquals(Celebration.TWO_FIFTY, Celebration.select(250, 250, 25, false));
		assertEquals(Celebration.TWO_FIFTY, Celebration.select(750, 250, 25, false));
		assertEquals(Celebration.THOUSAND, Celebration.select(1000, 250, 25, false));
		assertEquals(Celebration.THOUSAND, Celebration.select(2000, 10, 10, false));
		assertEquals(Celebration.GOAL, Celebration.select(1000, 1000, 10, true));
	}

	@Test public void sessionEncouragementIsOptionalAndSeparate()
	{
		assertNull(Celebration.select(60, 10, 0, false));
		assertEquals(Celebration.SESSION, Celebration.select(60, 10, 10, false));
		assertEquals(Celebration.SESSION, Celebration.select(175, 25, 25, false));
		assertNull(Celebration.select(61, 11, 10, false));
		assertNull(Celebration.select(0, 0, 10, false));
	}

	@Test public void goalCompletionIsOneTimePerTarget()
	{
		assertFalse(Celebration.goalReached(0, 100, null));
		assertFalse(Celebration.goalReached(100, null, null));
		assertFalse(Celebration.goalReached(100, 99, null));
		assertTrue(Celebration.goalReached(100, 100, null));
		assertFalse(Celebration.goalReached(100, 101, 100));
		assertTrue(Celebration.goalReached(200, 200, 100));
	}
}
