package com.bosskillmilestones;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("boss-kill-milestones")
public interface BossKillMilestonesConfig extends Config
{
	@ConfigSection(name = "Debug", description = "Testing and troubleshooting options", position = 100, closedByDefault = true)
	String debugSection = "debug";

	@ConfigSection(name = "Overlay", description = "On-screen counter, sidebar and visual effects", position = 50, closedByDefault = false)
	String overlaySection = "overlay";

	@ConfigItem(keyName = "personalGoalBasis", name = "Personal goal basis", description = "Choose whether your saved goal target uses all-time kills, this session or kills since activation. Session goals can be reached again each session.")
	default MilestoneBasis personalGoalBasis() { return MilestoneBasis.ALL_TIME; }

	@ConfigItem(keyName = "milestoneBasis", name = "Milestone basis", description = "Choose which kill count triggers milestone sounds and fireworks. All-time uses your known lifetime total, including loot estimates. Personal goals use their own basis setting.")
	default MilestoneBasis milestoneBasis() { return MilestoneBasis.SINCE_ACTIVATION; }

	enum MilestoneBasis
	{
		ALL_TIME("All-time kills"), SESSION("This session"), SINCE_ACTIVATION("Since activation");
		private final String label;
		MilestoneBasis(String label) { this.label = label; }
		@Override public String toString() { return label; }
		Integer count(int saved, int session, Integer total)
		{
			if (this == ALL_TIME) return total;
			return this == SESSION ? session : saved;
		}
	}

	@ConfigItem(keyName = "sessionCelebrations", name = "Session encouragement", description = "Optional short celebrations every 10 or 25 session kills per boss. Larger milestones and personal goals take priority.")
	default SessionEncouragement sessionCelebrations() { return SessionEncouragement.OFF; }

	@ConfigItem(keyName = "milestoneFireworks", name = "Milestone fireworks", section = overlaySection, position = 6, description = "Show a brief on-screen firework celebration when a milestone is reached. Larger milestones show bigger displays.")
	default boolean milestoneFireworks() { return true; }

	@ConfigItem(keyName = "overlayLayout", name = "Overlay layout", section = overlaySection, position = 1, description = "Full: all details. Compact: counters, next milestone and personal goal. Minimal: boss, session and saved counts, and next milestone.")
	default OverlayLayout overlayLayout() { return OverlayLayout.FULL; }

	enum OverlayLayout
	{
		FULL("Full"), COMPACT("Compact"), MINIMAL("Minimal");
		private final String label;
		OverlayLayout(String label) { this.label = label; }
		@Override public String toString() { return label; }
	}

	enum SessionEncouragement
	{
		OFF(0), EVERY_10(10), EVERY_25(25);
		final int interval;
		SessionEncouragement(int interval) { this.interval = interval; }
		@Override public String toString() { return interval == 0 ? "Off" : "Every " + interval + " kills"; }
	}
	@ConfigItem(keyName = "showGrindGoals", name = "Show goals on overlay", section = overlaySection, position = 2, description = "Show milestone progress, personal goals and cached friend comparisons in the Full layout.")
	default boolean showGrindGoals() { return true; }

	@ConfigItem(keyName = "friendHiscores", name = "Friends hiscore leaderboard", section = overlaySection, position = 5,
		description = "Look up your account and friends' public main-game boss totals. Requests send each username to the official hiscores service; results are cached for 10 minutes.",
		warning = "This feature submits your IP address to a 3rd-party server not controlled or verified by RuneLite developers")
	default boolean friendHiscores()
	{
		return false;
	}

	@ConfigItem(keyName = "alwaysOnGui", name = "Overlay always on", section = overlaySection, position = 3, description = "Keep the kill counter visible before your first kill and ignore the timeout. Show kill counter must also be enabled.")
	default boolean alwaysOnGui()
	{
		return false;
	}

	@Range(min = 1, max = 60)
	@ConfigItem(keyName = "guiTimeoutMinutes", name = "Overlay timeout (minutes)", section = overlaySection, position = 4, description = "Hide the counter after this many minutes without a tracked kill. Ignored when Overlay always on is enabled.")
	default int guiTimeoutMinutes()
	{
		return 5;
	}

	@ConfigItem(keyName = "showOverlay", name = "Show kill counter", section = overlaySection, position = 0, description = "Show the current target, session kills, saved kills and lifetime total.")
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(keyName = "testGuard", name = "Varrock guard testing", description = "Track level 21 Guards in Varrock as a separate test target. Uses server loot even when boss loot backup is off.", section = debugSection, position = 0)
	default boolean testGuard()
	{
		return false;
	}

	@ConfigItem(keyName = "syncDiagnostics", name = "Debug lifetime sync", description = "Report unreadable Combat Achievements boss statistics in chat and the debug log. Turn off after troubleshooting.", section = debugSection, position = 1)
	default boolean syncDiagnostics() { return false; }

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

	@Range(min = 0, max = 100)
	@ConfigItem(keyName = "soundVolume", name = "Sound volume (%)", description = "Volume of milestone, personal goal and session sounds. 0 mutes; 100 is full volume. Applies to the next sound.")
	default int soundVolume() { return 100; }
}
