package com.bosskillmilestones;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class GrindProgressTest
{
	@Test public void milestonesAdvanceAtEveryBoundary()
	{
		assertEquals(10, GrindProgress.nextMilestone(0));
		assertEquals(10, GrindProgress.nextMilestone(9));
		assertEquals(25, GrindProgress.nextMilestone(10));
		assertEquals(50, GrindProgress.nextMilestone(25));
		assertEquals(100, GrindProgress.nextMilestone(50));
		assertEquals(150, GrindProgress.nextMilestone(149));
		assertEquals(200, GrindProgress.nextMilestone(150));
		assertTrue(GrindProgress.nextMilestone(Integer.MAX_VALUE) > Integer.MAX_VALUE);
	}

	@Test public void lifetimeGoalHandlesUnknownAndEstimates()
	{
		assertNull(GrindProgress.total("Unknown"));
		assertEquals(Integer.valueOf(1250), GrindProgress.total("~1,250"));
	}

	@Test public void choosesNearestFriendAndCountsOvertakeNotTie()
	{
		FriendLeaderboard.View view = new FriendLeaderboard.View(Arrays.asList(
			new FriendLeaderboard.Row("Far", 200, ""), new FriendLeaderboard.Row("Near", 123, ""),
			new FriendLeaderboard.Row("Ryan", 100, "")), "Updated");
		assertEquals("Near: 24 to pass", GrindProgress.rival(view, "ryan"));
		assertEquals("You lead your friends!", GrindProgress.rival(view, "Far"));
		assertEquals("Your public KC is unavailable", GrindProgress.rival(view, "Missing"));
		view = new FriendLeaderboard.View(Arrays.asList(new FriendLeaderboard.Row("Ryan", 100, ""),
			new FriendLeaderboard.Row("Tie", 100, "")), "Updated");
		assertEquals("Tie: 1 to pass", GrindProgress.rival(view, "Ryan"));
	}

	@Test public void doesNotClaimFirstPlaceWithMissingResults()
	{
		FriendLeaderboard.View view = new FriendLeaderboard.View(Arrays.asList(new FriendLeaderboard.Row("Ryan", 100, ""),
			new FriendLeaderboard.Row("Pending", null, "Waiting")), "Loading");
		assertEquals("No rival in loaded results", GrindProgress.rival(view, "Ryan"));
	}
}
