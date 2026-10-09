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
	private static final Color GOLD = new Color(240, 187, 88);
	private static final Color MUTED = new Color(184, 173, 157);
	private static final Color EDGE = new Color(158, 111, 46, 220);
	private static final Color GOAL = new Color(90, 200, 180);
	private final Client client;
	private final BossKillMilestonesPlugin plugin;
	private final BossKillMilestonesConfig config;

	@Inject
	BossMilestoneOverlay(Client client, BossKillMilestonesPlugin plugin, BossKillMilestonesConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setResizable(false);
		setPosition(OverlayPosition.TOP_LEFT);
		panelComponent.setPreferredSize(new Dimension(260, 0));
		panelComponent.setBackgroundColor(new Color(23, 15, 20, 235));
		panelComponent.setBorder(new java.awt.Rectangle(10, 10, 10, 10));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay() || client.getGameState() != GameState.LOGGED_IN || !plugin.isCounterVisible()) return null;
		BossKillMilestonesPlugin.Progress progress = plugin.getProgress();
		BossKillMilestonesConfig.OverlayLayout layout = config.overlayLayout();
		int width = layout == BossKillMilestonesConfig.OverlayLayout.FULL ? 260
			: layout == BossKillMilestonesConfig.OverlayLayout.COMPACT ? 210 : 175;
		panelComponent.setPreferredSize(new Dimension(width, 0));
		setPreferredSize(new Dimension(width, 0));
		panelComponent.setBorder(new java.awt.Rectangle(7, 7, 7, 7));
		panelComponent.getChildren().clear();
		panelComponent.getChildren().add(TitleComponent.builder().text(progress.name).color(GOLD).build());
		add("This session", Integer.toString(progress.session));
		add("Since enabled", Integer.toString(progress.saved));
		if (layout == BossKillMilestonesConfig.OverlayLayout.MINIMAL)
		{
			add("Next milestone", progress.nextMilestoneText());
			add("Personal goal", progress.goal <= 0 ? "Not set"
				: progress.goalCount() == null ? "Sync total"
				: progress.goalCount() >= progress.goal ? "Reached!"
				: progress.goalCountText() + " / " + progress.goal);
			return renderThemedPanel(graphics);
		}
		add("Total", progress.total);
		if (layout == BossKillMilestonesConfig.OverlayLayout.COMPACT)
		{
			add("Next milestone", progress.nextMilestoneText());
			Integer lifetime = progress.goalCount();
			if (progress.goal > 0)
			{
				add("Personal goal", lifetime == null ? "Sync total" : lifetime >= progress.goal ? "Reached!" : (progress.goal - lifetime) + " away");
				bar(lifetime == null ? 0 : lifetime, progress.goal, progress.goalCountText() + " / " + progress.goal, GOAL);
			}
			else add("Personal goal", "Set in sidebar");
			return renderThemedPanel(graphics);
		}
		if (config.showGrindGoals())
		{
			Integer count = progress.milestoneCount();
			add("NEXT JINGLE", progress.nextMilestoneText());
			add("Based on", progress.basis.toString());
			if (count != null)
			{
				long next = GrindProgress.nextMilestone(count);
				bar(count, next, count + " / " + next, GOLD);
			}
			Integer total = progress.goalCount();
			if (progress.goal > 0)
			{
				add("PERSONAL GOAL", total == null ? "Sync lifetime total" : total >= progress.goal ? "Reached!" : (progress.goal - total) + " to go");
				add("Goal basis", progress.goalBasis.toString());
				bar(total == null ? 0 : total, progress.goal, progress.goalCountText() + " / " + progress.goal, GOAL);
			}
			else add("Personal goal", "Set in sidebar");
			add("FRIEND TO BEAT", "Cached hiscores");
			panelComponent.getChildren().add(LineComponent.builder().left(plugin.rivalFor(progress.name))
				.leftColor(new Color(130, 210, 255)).build());
		}
		return renderThemedPanel(graphics);
	}

	private Dimension renderThemedPanel(Graphics2D graphics)
	{
		Dimension size = super.render(graphics);
		if (size != null && size.width > 0 && size.height > 0)
		{
			Graphics2D frame = (Graphics2D) graphics.create();
			try
			{
				frame.setColor(EDGE);
				frame.drawRect(0, 0, size.width - 1, size.height - 1);
				frame.setColor(GOLD);
				frame.drawLine(7, 0, size.width - 8, 0);
			}
			finally { frame.dispose(); }
		}
		return size;
	}

	private void bar(long value, long maximum, String label, Color color)
	{
		ProgressBarComponent bar = new ProgressBarComponent();
		bar.setMaximum(maximum);
		bar.setValue(Math.min(value, maximum));
		bar.setForegroundColor(color);
		bar.setBackgroundColor(new Color(47, 27, 33));
		bar.setCenterLabel(label);
		bar.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.TEXT_ONLY);
		panelComponent.getChildren().add(bar);
	}

	private void add(String label, String value)
	{
		panelComponent.getChildren().add(LineComponent.builder().left(label).leftColor(MUTED)
			.right(value).rightColor(GOLD).build());
	}
}
