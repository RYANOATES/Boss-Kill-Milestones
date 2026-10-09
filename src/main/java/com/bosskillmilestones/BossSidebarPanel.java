package com.bosskillmilestones;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

final class BossSidebarPanel extends PluginPanel
{
	private static final String FOLLOW = "Follow current boss";
	private static final Color BACKGROUND = new Color(23, 15, 20);
	private static final Color SURFACE = new Color(47, 27, 33);
	private static final Color GOLD = new Color(240, 187, 88);
	private static final Color MUTED = new Color(184, 173, 157);
	private static final Color EDGE = new Color(158, 111, 46);
	private static final BufferedImage ICON = ImageUtil.loadImageResource(BossSidebarPanel.class, "/boss-milestones-icon.png");
	private final JLabel title = new JLabel("Choose a boss", SwingConstants.CENTER);
	private final JLabel image = new JLabel(new ImageIcon(icon(96)), SwingConstants.CENTER);
	private final JLabel total = new JLabel("Unknown");
	private final JLabel saved = new JLabel("0");
	private final JLabel session = new JLabel("0");
	private final JTextArea status = new JTextArea("Log in to view your account's friends.");
	private final DefaultTableModel table = new DefaultTableModel(new String[]{"#", "Friend", "Kills"}, 0)
	{
		@Override public boolean isCellEditable(int row, int column) { return false; }
	};
	private final Runnable activate;
	private final Runnable deactivate;
	private String shownBoss;
	private boolean hasOwnSummary;
	private final JProgressBar milestone = progressBar(new Color(255, 190, 80));
	private final JProgressBar personal = progressBar(new Color(90, 200, 180));
	private final JLabel rival = new JLabel("Open sidebar to load friends");
	private final JSpinner goalInput = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 50));
	private int shownGoal = -1;

	BossSidebarPanel(Consumer<String> selection, Runnable refresh, Runnable activate, Runnable deactivate)
	{
		this(selection, refresh, activate, deactivate, (boss, goal) -> {});
	}

	BossSidebarPanel(Consumer<String> selection, Runnable refresh, Runnable activate, Runnable deactivate,
		java.util.function.BiConsumer<String, Integer> saveGoal)
	{
		this.activate = activate;
		this.deactivate = deactivate;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 2, 0, 2, EDGE),
			BorderFactory.createEmptyBorder(12, 8, 12, 8)));
		setBackground(BACKGROUND);
		JLabel brand = new JLabel("BOSS MILESTONES", new ImageIcon(icon(28)), SwingConstants.CENTER);
		brand.setFont(brand.getFont().deriveFont(Font.BOLD, 13f));
		brand.setForeground(GOLD);
		brand.setIconTextGap(8);
		brand.setOpaque(true);
		brand.setBackground(SURFACE);
		brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
		brand.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 0, 2, 0, GOLD),
			BorderFactory.createEmptyBorder(8, 4, 8, 4)));
		add(brand);
		add(Box.createVerticalStrut(10));
		JComboBox<String> bosses = new JComboBox<>();
		bosses.addItem(FOLLOW);
		for (String name : BossNames.all()) bosses.addItem(name);
		bosses.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		bosses.setBackground(SURFACE);
		bosses.setForeground(GOLD);
		bosses.setBorder(BorderFactory.createLineBorder(EDGE));
		bosses.addActionListener(event -> selection.accept(FOLLOW.equals(bosses.getSelectedItem()) ? null : (String) bosses.getSelectedItem()));
		add(bosses);
		add(Box.createVerticalStrut(14));
		title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
		title.setForeground(new Color(255, 190, 80));
		title.setAlignmentX(CENTER_ALIGNMENT);
		add(title);
		image.setAlignmentX(CENTER_ALIGNMENT);
		image.setPreferredSize(new Dimension(110, 110));
		image.setOpaque(true);
		image.setBackground(SURFACE);
		image.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GOLD, 2),
			BorderFactory.createEmptyBorder(8, 8, 8, 8)));
		add(image);
		add(Box.createVerticalStrut(10));
		add(stat("Total kills", total));
		add(stat("Since activation", saved));
		add(stat("This session", session));
		JLabel estimateNote = new JLabel("~ = estimate; Unknown = not synced");
		estimateNote.setFont(estimateNote.getFont().deriveFont(10f));
		estimateNote.setForeground(MUTED);
		add(estimateNote);
		add(Box.createVerticalStrut(10));
		add(milestone);
		add(Box.createVerticalStrut(6));
		add(personal);
		JPanel goalControls = new JPanel(new BorderLayout(5, 0));
		goalControls.setOpaque(false);
		goalControls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		goalInput.setToolTipText("Kill target for this boss using Personal goal basis; 0 clears the goal.");
		goalControls.add(goalInput, BorderLayout.CENTER);
		JButton save = new JButton("Set goal");
		styleButton(save);
		JFormattedTextField goalText = ((JSpinner.DefaultEditor) goalInput.getEditor()).getTextField();
		goalText.setBackground(SURFACE);
		goalText.setForeground(GOLD);
		goalText.setCaretColor(GOLD);
		save.addActionListener(event -> {
			try { goalInput.commitEdit(); saveGoal.accept(shownBoss, (Integer) goalInput.getValue()); }
			catch (java.text.ParseException ex) { goalInput.setValue(Math.max(0, shownGoal)); }
		});
		goalControls.add(save, BorderLayout.EAST);
		add(goalControls);
		rival.setForeground(new Color(130, 210, 255));
		rival.setFont(rival.getFont().deriveFont(11f));
		add(rival);
		add(Box.createVerticalStrut(18));
		JLabel heading = new JLabel("Friends leaderboard");
		heading.setFont(heading.getFont().deriveFont(Font.BOLD, 14f));
		heading.setForeground(GOLD);
		heading.setOpaque(true);
		heading.setBackground(SURFACE);
		heading.setHorizontalAlignment(SwingConstants.CENTER);
		heading.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
		heading.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, GOLD),
			BorderFactory.createEmptyBorder(8, 4, 8, 4)));
		add(heading);
		JLabel subtitle = new JLabel("Main-game lifetime hiscores");
		subtitle.setForeground(MUTED);
		add(subtitle);
		add(Box.createVerticalStrut(6));
		JButton reload = new JButton("Refresh friends");
		styleButton(reload);
		reload.setToolTipText("Refresh your friends list; reuse hiscore results less than 10 minutes old.");
		reload.addActionListener(event -> refresh.run());
		add(reload);
		status.setEditable(false);
		status.setLineWrap(true);
		status.setWrapStyleWord(true);
		status.setBackground(getBackground());
		status.setForeground(MUTED);
		status.setFont(status.getFont().deriveFont(11f));
		status.setRows(3);
		status.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
		status.setMinimumSize(new Dimension(0, 40));
		add(status);
		JTable rows = new JTable(table);
		rows.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
		{
			@Override public java.awt.Component getTableCellRendererComponent(JTable grid, Object value,
				boolean selected, boolean focused, int row, int column)
			{
				java.awt.Component cell = super.getTableCellRendererComponent(grid, value, selected, focused, row, column);
				cell.setForeground(hasOwnSummary && grid.convertRowIndexToModel(row) == 0
					? new Color(100, 230, 120) : selected ? grid.getSelectionForeground() : grid.getForeground());
				cell.setBackground(selected ? grid.getSelectionBackground()
					: hasOwnSummary && grid.convertRowIndexToModel(row) == 0 ? new Color(29, 53, 36)
					: row % 2 == 0 ? SURFACE : BACKGROUND);
				setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : column == 2 ? SwingConstants.RIGHT : SwingConstants.LEFT);
				setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
				return cell;
			}
		});
		rows.setRowHeight(24);
		rows.setFillsViewportHeight(true);
		rows.setBackground(BACKGROUND);
		rows.setForeground(Color.WHITE);
		rows.setShowGrid(false);
		rows.setSelectionBackground(new Color(78, 59, 35));
		rows.setSelectionForeground(Color.WHITE);
		DefaultTableCellRenderer header = new DefaultTableCellRenderer();
		header.setOpaque(true);
		header.setBackground(SURFACE);
		header.setForeground(GOLD);
		header.setFont(rows.getFont().deriveFont(Font.BOLD));
		header.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
		rows.getTableHeader().setDefaultRenderer(header);
		rows.getTableHeader().setReorderingAllowed(false);
		rows.getColumnModel().getColumn(0).setMaxWidth(30);
		rows.getColumnModel().getColumn(1).setPreferredWidth(110);
		rows.getColumnModel().getColumn(2).setPreferredWidth(85);
		JScrollPane scroll = new JScrollPane(rows);
		scroll.setBorder(BorderFactory.createLineBorder(EDGE));
		scroll.getViewport().setBackground(BACKGROUND);
		scroll.setColumnHeaderView(rows.getTableHeader());
		scroll.setPreferredSize(new Dimension(210, 250));
		scroll.setMinimumSize(new Dimension(0, 150));
		add(scroll);
		add(Box.createVerticalStrut(8));
		JTextArea thanks = new JTextArea("Thanks Cow and the Lucipurr boys for support and the idea.");
		thanks.setEditable(false);
		thanks.setFocusable(false);
		thanks.setLineWrap(true);
		thanks.setWrapStyleWord(true);
		thanks.setOpaque(false);
		thanks.setForeground(MUTED);
		thanks.setFont(thanks.getFont().deriveFont(10f));
		thanks.setRows(3);
		thanks.setMinimumSize(new Dimension(0, 42));
		thanks.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
		add(thanks);
		for (java.awt.Component component : getComponents())
			if (component instanceof JComponent) ((JComponent) component).setAlignmentX(CENTER_ALIGNMENT);
	}

	private JPanel stat(String label, JLabel value)
	{
		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(SURFACE);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, EDGE),
			BorderFactory.createEmptyBorder(4, 8, 4, 8)));
		JLabel caption = new JLabel(label);
		caption.setForeground(MUTED);
		value.setForeground(GOLD);
		value.setFont(value.getFont().deriveFont(Font.BOLD));
		row.add(caption, BorderLayout.WEST);
		row.add(value, BorderLayout.EAST);
		return row;
	}

	private static void styleButton(JButton button)
	{
		button.setBackground(SURFACE);
		button.setForeground(GOLD);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EDGE),
			BorderFactory.createEmptyBorder(6, 10, 6, 10)));
	}

	boolean showProgress(BossKillMilestonesPlugin.Progress progress)
	{
		boolean changed = !Objects.equals(shownBoss, progress.name);
		if (changed || shownGoal != progress.goal)
		{
			shownGoal = progress.goal;
			goalInput.setValue(progress.goal);
		}
		Integer count = progress.milestoneCount();
		long next = GrindProgress.nextMilestone(count == null ? 0 : count);
		milestone.setValue(count == null ? 0 : (int) (100.0 * count / next));
		milestone.setString(count == null ? "Sync lifetime total" : "Next jingle: " + count + " / " + next);
		milestone.setToolTipText("Milestone basis: " + progress.basis);
		Integer lifetime = progress.goalCount();
		personal.setToolTipText("Personal goal basis: " + progress.goalBasis);
		goalInput.setToolTipText("Target based on " + progress.goalBasis + "; 0 clears the goal.");
		personal.setValue(progress.goal > 0 && lifetime != null ? (int) Math.min(100, 100.0 * lifetime / progress.goal) : 0);
		personal.setString(progress.goal <= 0 ? "Set a goal below" : lifetime == null ? "Goal " + progress.goal + " — sync total"
			: lifetime >= progress.goal ? "Goal reached! " + progress.goal : "Goal: " + progress.goalCountText() + " / " + progress.goal);
		shownBoss = progress.name;
		title.setText(progress.name);
		title.setToolTipText(progress.name);
		total.setText(progress.total);
		saved.setText(Integer.toString(progress.saved));
		session.setText(Integer.toString(progress.session));
		if (changed) image.setIcon(new ImageIcon(icon(96)));
		return changed;
	}

	void showImage(String boss, BufferedImage sprite)
	{
		if (!Objects.equals(shownBoss, boss)) return;
		BufferedImage enlarged = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = enlarged.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		double scale = Math.min(96.0 / sprite.getWidth(), 96.0 / sprite.getHeight());
		int width = (int) (sprite.getWidth() * scale), height = (int) (sprite.getHeight() * scale);
		graphics.drawImage(sprite, (96 - width) / 2, (96 - height) / 2, width, height, null);
		graphics.dispose();
		image.setIcon(new ImageIcon(enlarged));
	}

	void showRival(String text)
	{
		rival.setText(text);
		rival.setToolTipText("Cached public hiscores: " + text);
	}

	private static JProgressBar progressBar(Color color)
	{
		JProgressBar bar = new JProgressBar(0, 100);
		bar.setStringPainted(true);
		bar.setForeground(color);
		bar.setBackground(SURFACE);
		bar.setBorder(BorderFactory.createLineBorder(EDGE));
		bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		return bar;
	}

	void showBoard(FriendLeaderboard.View view, String ownName)
	{
		table.setRowCount(0);
		hasOwnSummary = false;
		int rank = 0;
		for (FriendLeaderboard.Row row : view.rows)
		{
			table.addRow(new Object[]{row.kills == null ? "—" : Integer.toString(++rank),
				row.name + (row.name.equalsIgnoreCase(ownName) ? " (you)" : ""),
				row.kills == null ? row.status : String.format("%,d", row.kills)});
			if (row.name.replace('\u00a0', ' ').trim().equalsIgnoreCase(ownName == null ? "" : ownName.replace('\u00a0', ' ').trim()))
			{
				hasOwnSummary = true;
				// Copy this exact rank; the summary must not affect the ordered ranks.
				Object[] summary = {row.kills == null ? "—" : Integer.toString(rank), row.name + " (you)",
					row.kills == null ? row.status : String.format("%,d", row.kills)};
				table.insertRow(0, summary);
			}
		}
		status.setText(view.status);
	}

	void message(String text)
	{
		hasOwnSummary = false;
		table.setRowCount(0);
		status.setText(text);
	}

	@Override public void onActivate() { activate.run(); }
	@Override public void onDeactivate() { deactivate.run(); }

	static BufferedImage icon(int size)
	{
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(ICON, 0, 0, size, size, null);
		g.dispose();
		return image;
	}
}
