package com.bosskillmilestones;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Boss Kill Milestones",
		description = "Tracks boss kill milestones and celebrates them with jingles.",
		 tags = {"boss", "kill count", "milestone", "jingle"}
)
public class BossKillMilestonesPlugin extends Plugin
{
	@Inject
	private BossKillMilestonesConfig config;

	@Override
	protected void startUp()
	{
		log.debug("Boss Kill Milestones started");
	}

	@Override
	protected void shutDown()
	{
		log.debug("Boss Kill Milestones stopped");
	}

	@Provides
	BossKillMilestonesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BossKillMilestonesConfig.class);
	}
}
