package com.bosskillmilestones;

final class GrindProgress
{
	static long nextMilestone(int kills)
	{
		return kills < 10 ? 10 : kills < 25 ? 25 : ((long) kills / 50 + 1) * 50;
	}

	static Integer total(String text)
	{
		try { return Integer.valueOf(text.replace("~", "").replace(",", "")); }
		catch (NumberFormatException ex) { return null; }
	}

	static String rival(FriendLeaderboard.View view, String own)
	{
		Integer kills = null;
		for (FriendLeaderboard.Row row : view.rows)
			if (sameName(row.name, own)) kills = row.kills;
		if (kills == null) return "Your public KC is unavailable";
		FriendLeaderboard.Row next = null;
		boolean incomplete = false;
		for (FriendLeaderboard.Row row : view.rows)
		{
			if (row.kills == null) incomplete = true;
			if (!sameName(row.name, own) && row.kills != null && row.kills >= kills
				&& (next == null || row.kills < next.kills)) next = row;
		}
		if (next == null) return incomplete ? "No rival in loaded results" : "You lead your friends!";
		return next.name + ": " + ((long) next.kills - kills + 1) + " to pass";
	}

	private static boolean sameName(String left, String right)
	{
		return right != null && left.replace('\u00a0', ' ').trim().equalsIgnoreCase(right.replace('\u00a0', ' ').trim());
	}
}
