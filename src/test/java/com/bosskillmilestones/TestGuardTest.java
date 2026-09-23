package com.bosskillmilestones;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestGuardTest
{
	@Test
	public void restrictsTestTargetToVarrockLevel21Guards()
	{
		WorldPoint varrock = new WorldPoint(3210, 3420, 0);
		assertTrue(TestGuard.matches("Guard", 21, varrock));
		assertFalse(TestGuard.matches("Guard", 22, varrock));
		assertFalse(TestGuard.matches("Goblin", 21, varrock));
		assertFalse(TestGuard.matches("Guard", 21, new WorldPoint(2965, 3380, 0)));
		assertFalse(TestGuard.matches("Guard", 21, new WorldPoint(3210, 3420, 1)));
		assertFalse(TestGuard.matches("Guard", 21, null));
	}
}
