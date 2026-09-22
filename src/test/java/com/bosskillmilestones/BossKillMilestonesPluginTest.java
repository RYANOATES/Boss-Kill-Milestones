package com.bosskillmilestones;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BossKillMilestonesPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BossKillMilestonesPlugin.class);
		RuneLite.main(args);
	}
}
