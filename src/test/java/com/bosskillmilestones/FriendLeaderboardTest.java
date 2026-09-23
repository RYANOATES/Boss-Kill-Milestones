package com.bosskillmilestones;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.hiscore.Skill;
import org.junit.Test;
import static org.junit.Assert.*;

public class FriendLeaderboardTest
{
	@Test public void normalizesAndDeduplicatesFriends()
	{
		assertEquals(Arrays.asList("Ryan", "A friend"), FriendLeaderboard.uniqueNames(
			Arrays.asList(" Ryan ", "ryan", null, "", "A\u00a0friend", "a friend")));
	}

	@Test public void sortsKillsDescendingWithoutTreatingMissingAsZero()
	{
		List<FriendLeaderboard.Row> rows = FriendLeaderboard.sorted(Arrays.asList(
			new FriendLeaderboard.Row("Unknown", null, "Unranked"),
			new FriendLeaderboard.Row("Zero", 0, ""),
			new FriendLeaderboard.Row("Zed", 100, ""),
			new FriendLeaderboard.Row("Amy", 100, "")));
		assertEquals("Amy", rows.get(0).name);
		assertEquals("Zed", rows.get(1).name);
		assertEquals(Integer.valueOf(0), rows.get(2).kills);
		assertNull(rows.get(3).kills);
	}

	@Test public void readsBossScoreNotRankOrExperience()
	{
		HiscoreSkill boss = BossNames.hiscore("Sarachnis");
		assertNotNull(boss);
		HiscoreResult result = new HiscoreResult("Ryan", Collections.singletonMap(boss, new Skill(12, 1250, -1)));
		assertEquals(Integer.valueOf(1250), FriendLeaderboard.row("Ryan", result, boss, false).kills);
		assertEquals("Unavailable", FriendLeaderboard.row("Ryan", result, boss, true).status);
		assertEquals("Unranked", FriendLeaderboard.row("Ryan", null, boss, false).status);
		HiscoreResult unranked = new HiscoreResult("Ryan", Collections.singletonMap(boss, new Skill(-1, -1, -1)));
		assertNull(FriendLeaderboard.row("Ryan", unranked, boss, false).kills);
	}

	@Test public void unsupportedTargetsDoNotRequestHiscores()
	{
		assertNull(BossNames.hiscore(TestGuard.NAME));
		assertNull(BossNames.hiscore("Theatre of Blood: Entry Mode"));
		assertNull(BossNames.hiscore(null));
		assertTrue(BossNames.all().contains(TestGuard.NAME));
	}
}
