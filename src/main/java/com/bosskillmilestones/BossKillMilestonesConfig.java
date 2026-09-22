package com.bosskillmilestones;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("boss-kill-milestones")
public interface BossKillMilestonesConfig extends Config
{
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
		return "";
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
