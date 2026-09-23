package com.bosskillmilestones;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("boss-kill-milestones")
public interface BossKillMilestonesConfig extends Config
{
	@ConfigItem(keyName = "showGrindGoals", name = "Show goals on overlay", description = "Show milestone progress, personal goals and cached friend comparisons on the on-screen counter.")
	default boolean showGrindGoals() { return true; }

	@ConfigItem(keyName = "friendHiscores", name = "Friends hiscore leaderboard",
		description = "Look up your account and friends' public main-game boss totals. Requests send each username to the official hiscores service; results are cached for 10 minutes.",
		warning = "This feature submits your IP address to a 3rd-party server not controlled or verified by RuneLite developers")
	default boolean friendHiscores()
	{
		return false;
	}

	@ConfigItem(keyName = "alwaysOnGui", name = "Always on GUI", description = "Keep the kill counter visible before your first kill and ignore the idle timeout. Show kill counter must also be enabled.")
	default boolean alwaysOnGui()
	{
		return false;
	}

	@Range(min = 1, max = 60)
	@ConfigItem(keyName = "guiTimeoutMinutes", name = "GUI timeout (minutes)", description = "Hide the counter after this many minutes without a tracked kill. Ignored when Always on GUI is enabled.")
	default int guiTimeoutMinutes()
	{
		return 5;
	}

	@ConfigItem(keyName = "showOverlay", name = "Show kill counter", description = "Show the current target, session kills, saved kills and lifetime total.")
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(keyName = "testGuard", name = "Varrock guard testing", description = "Track level 21 Guards in Varrock as a separate test target. Uses server loot even when boss loot backup is off.")
	default boolean testGuard()
	{
		return true;
	}

	@ConfigItem(keyName = "lootBackup", name = "Loot backup", description = "Count recognised boss server-loot events when a kill-count message is missing.")
	default boolean lootBackup()
	{
		return true;
	}

	@ConfigItem(keyName = "syncReminder", name = "Initial sync reminder", description = "Remind you at login until a boss total has been read from Combat Achievements.")
	default boolean syncReminder()
	{
		return true;
	}

	@ConfigItem(
		keyName = "excludedBosses",
		name = "Excluded bosses",
		description = "Comma-separated boss names to exclude, matching the name shown in the kill-count message."
	)
	default String excludedBosses()
	{
		return "\n \n \n";
	}

	@ConfigItem(
		keyName = "playMilestoneSound",
		name = "Play milestone sound",
		description = "Play a celebratory sound when a boss milestone is reached."
	)
	default boolean playMilestoneSound()
	{
		return true;
	}
}
