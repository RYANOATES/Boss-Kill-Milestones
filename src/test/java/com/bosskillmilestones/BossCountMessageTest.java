package com.bosskillmilestones;

import java.util.regex.Matcher;
import org.junit.Test;
import static org.junit.Assert.*;

public class BossCountMessageTest
{
	@Test public void acceptsBarrowsChestCount()
	{
		for (String number : new String[]{"<col=ff0000>1", "<col=ff0000>1,250", "@red@26"})
		{
			Matcher match = BossKillMilestonesPlugin.KILL_COUNT_PATTERN.matcher(
				"Your Barrows chest count is: " + number + "</col>.");
			assertTrue(match.find());
			assertEquals("Barrows Chests", BossNames.known(match.group("boss")));
			assertTrue(BossKillMilestonesPlugin.isBossKillCountMessage(
				match.group("boss"), match.group("prefix"), match.group("suffix")));
			assertTrue(Integer.parseInt(match.group("total").replace(",", "")) > 0);
		}
	}

	@Test public void keepsNormalBossMessagesAndRejectsUnrelatedCounts()
	{
		assertTrue(accepted("Your Sarachnis kill count is: <col=ff0000>150</col>."));
		assertTrue(accepted("Your completion count for The Gauntlet is: <col=ff0000>50</col>."));
		assertFalse(accepted("Your Sarachnis count is: <col=ff0000>150</col>."));
		assertFalse(accepted("Your Crystal chest count is: <col=ff0000>50</col>."));
		assertFalse(accepted("Your Barrows chest lap count is: <col=ff0000>50</col>."));
	}

	private static boolean accepted(String text)
	{
		Matcher match = BossKillMilestonesPlugin.KILL_COUNT_PATTERN.matcher(text);
		return match.find() && BossKillMilestonesPlugin.isBossKillCountMessage(
			match.group("boss"), match.group("prefix"), match.group("suffix"));
	}
}
