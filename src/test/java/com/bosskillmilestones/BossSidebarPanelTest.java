package com.bosskillmilestones;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import org.junit.Test;
import static org.junit.Assert.*;

public class BossSidebarPanelTest
{
	@Test public void rendersPanelAndUpdatesCounters() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			BossSidebarPanel panel = new BossSidebarPanel(name -> {}, () -> {}, () -> {}, () -> {});
			assertTrue(panel.showProgress(new BossKillMilestonesPlugin.Progress("Sarachnis", 25, 150, "1250")));
			assertFalse(panel.showProgress(new BossKillMilestonesPlugin.Progress("Sarachnis", 26, 151, "1251", 2000)));
			panel.showRival("Boss Hunter: 1051 to pass");
			panel.showBoard(new FriendLeaderboard.View(Arrays.asList(
				new FriendLeaderboard.Row("Boss Hunter", 2300, ""),
				new FriendLeaderboard.Row("Ryan", 1250, ""),
				new FriendLeaderboard.Row("A Friend", null, "Unranked")), "Updated — cached up to 10 min"), "Ryan");
			JTable board = board(panel);
			assertEquals(4, board.getRowCount());
			assertEquals("2", board.getValueAt(0, 0));
			assertEquals("Ryan (you)", board.getValueAt(0, 1));
			assertEquals("1,250", board.getValueAt(0, 2));
			assertEquals("1", board.getValueAt(1, 0));
			assertEquals("Ryan (you)", board.getValueAt(2, 1));
			java.awt.Color green = board.prepareRenderer(board.getCellRenderer(0, 0), 0, 0).getForeground();
			assertTrue(green.getGreen() > green.getRed());
			assertEquals(board.getForeground(), board.prepareRenderer(board.getCellRenderer(1, 0), 1, 0).getForeground());
			panel.setSize(240, 800);
			layout(panel);
			BufferedImage image = new BufferedImage(240, 800, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = image.createGraphics();
			panel.paint(graphics);
			graphics.dispose();
			try { ImageIO.write(image, "png", new File("build/sidebar-preview.png")); }
			catch (java.io.IOException ex) { throw new AssertionError(ex); }
		});
	}

	@Test public void unrankedSummaryAndClearing() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			BossSidebarPanel panel = new BossSidebarPanel(name -> {}, () -> {}, () -> {}, () -> {});
			panel.showBoard(new FriendLeaderboard.View(Arrays.asList(
				new FriendLeaderboard.Row("Ryan", null, "Unranked")), "Updated"), "ryan");
			JTable board = board(panel);
			assertEquals(2, board.getRowCount());
			assertEquals("—", board.getValueAt(0, 0));
			assertEquals("Unranked", board.getValueAt(0, 2));
			panel.message("Logged out");
			assertEquals(0, board.getRowCount());
		});
	}

	private static JTable board(BossSidebarPanel panel)
	{
		for (Component child : panel.getComponents())
			if (child instanceof JScrollPane) return (JTable) ((JScrollPane) child).getViewport().getView();
		throw new AssertionError("Leaderboard missing");
	}

	private static void layout(Container container)
	{
		container.doLayout();
		for (Component child : container.getComponents())
			if (child instanceof Container) layout((Container) child);
	}
}
