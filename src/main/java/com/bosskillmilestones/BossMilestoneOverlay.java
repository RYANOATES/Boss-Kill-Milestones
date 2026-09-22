package com.bosskillmilestones;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

final class BossMilestoneOverlay extends OverlayPanel
{
	private final Client client;
	private final BossKillMilestonesPlugin plugin;
	private final BossKillMilestonesConfig config;

	@Inject
	BossMilestoneOverlay(Client client, BossKillMilestonesPlugin plugin, BossKillMilestonesConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		panelComponent.setPreferredSize(new Dimension(225, 0));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay() || client.getGameState() != GameState.LOGGED_IN) return null;
		BossKillMilestonesPlugin.Progress progress = plugin.getProgress();
		panelComponent.getChildren().clear();
		panelComponent.getChildren().add(TitleComponent.builder().text("Boss Kill Milestones").color(new Color(255, 190, 80)).build());
		add("Boss", progress.name);
		add("This session", Integer.toString(progress.session));
		add("Since enabled", Integer.toString(progress.saved));
		add("Total", progress.total);
		return super.render(graphics);
	}

	private void add(String label, String value)
	{
		panelComponent.getChildren().add(LineComponent.builder().left(label).right(value).build());
	}
}
