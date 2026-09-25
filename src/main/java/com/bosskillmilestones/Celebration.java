package com.bosskillmilestones;

enum Celebration
{
	SESSION("session-spark", "Session milestone"),
	TEN("little-victory", "First ten"),
	TWENTY_FIVE("golden-steps", "First twenty-five"),
	FIFTY("little-victory", "Milestone"),
	TWO_FIFTY("champion-fanfare", "250-kill milestone"),
	THOUSAND("legendary-victory", "1,000-kill milestone"),
	GOAL("goal-complete", "Personal goal reached");

	final String resource;
	final String title;
	Celebration(String resource, String title)
	{
		this.resource = "/audio/" + resource + ".wav";
		this.title = title;
	}

	static Celebration select(int saved, int session, int sessionInterval, boolean goalReached)
	{
		if (goalReached) return GOAL;
		if (saved > 0 && saved % 1000 == 0) return THOUSAND;
		if (saved > 0 && saved % 250 == 0) return TWO_FIFTY;
		if (saved > 0 && saved % 50 == 0) return FIFTY;
		if (saved == 25) return TWENTY_FIVE;
		if (saved == 10) return TEN;
		if ((sessionInterval == 10 || sessionInterval == 25) && session > 0 && session % sessionInterval == 0) return SESSION;
		return null;
	}

	static boolean goalReached(int target, Integer total, Integer celebrated)
	{
		return target > 0 && total != null && total >= target && !Integer.valueOf(target).equals(celebrated);
	}
}
