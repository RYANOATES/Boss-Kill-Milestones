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
import net.runelite.client.ui.overlay.components.ProgressBarComponent;

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
		panelComponent.setPreferredSize(new Dimension(260, 0));
		panelComponent.setBackgroundColor(new Color(20, 25, 32, 225));
		panelComponent.setBorder(new java.awt.Rectangle(10, 10, 10, 10));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay() || client.getGameState() != GameState.LOGGED_IN || !plugin.isCounterVisible()) return null;
		BossKillMilestonesPlugin.Progress progress = plugin.getProgress();
		panelComponent.getChildren().clear();
		panelComponent.getChildren().add(TitleComponent.builder().text(progress.name).color(new Color(255, 190, 80)).build());
		add("This session", Integer.toString(progress.session));
		add("Since enabled", Integer.toString(progress.saved));
		add("Total", progress.total);
		if (config.showGrindGoals())
		{
			long next = GrindProgress.nextMilestone(progress.saved);
			add("NEXT JINGLE", (next - progress.saved) + " kills away");
			bar(progress.saved, next, progress.saved + " / " + next, new Color(225, 170, 65));
			Integer total = GrindProgress.total(progress.total);
			if (progress.goal > 0)
			{
				add("PERSONAL GOAL", total == null ? "Sync lifetime total" : total >= progress.goal ? "Reached!" : (progress.goal - total) + " to go");
				bar(total == null ? 0 : total, progress.goal, progress.total + " / " + progress.goal, new Color(65, 165, 145));
			}
			else add("Personal goal", "Set in sidebar");
			add("FRIEND TO BEAT", "Cached hiscores");
			panelComponent.getChildren().add(LineComponent.builder().left(plugin.rivalFor(progress.name))
				.leftColor(new Color(130, 210, 255)).build());
		}
		return super.render(graphics);
	}

	private void bar(long value, long maximum, String label, Color color)
	{
		ProgressBarComponent bar = new ProgressBarComponent();
		bar.setMaximum(maximum);
		bar.setValue(Math.min(value, maximum));
		bar.setForegroundColor(color);
		bar.setBackgroundColor(new Color(40, 47, 57));
		bar.setCenterLabel(label);
		bar.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.TEXT_ONLY);
		panelComponent.getChildren().add(bar);
	}

	private void add(String label, String value)
	{
		panelComponent.getChildren().add(LineComponent.builder().left(label).right(value).build());
	}
}
