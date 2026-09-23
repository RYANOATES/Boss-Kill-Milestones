package com.bosskillmilestones;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Nameable;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Singleton
final class BossSidebar
{
	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private ClientToolbar toolbar;
	@Inject private SpriteManager sprites;
	@Inject private ConfigManager configManager;
	@Inject private BossKillMilestonesConfig config;
	@Inject private BossKillMilestonesPlugin plugin;
	@Inject private FriendLeaderboard leaderboard;
	private final AtomicLong lifecycle = new AtomicLong();
	private final AtomicLong boardVersion = new AtomicLong();
	private volatile BossSidebarPanel panel;
	private NavigationButton navigation;
	private volatile boolean running;
	// The following state is owned by the client thread.
	private boolean active;
	private String selected;
	private volatile String profile;
	private String context;
	private String pictured;
	private final java.util.Map<String, String> rivals = new java.util.concurrent.ConcurrentHashMap<>();

	String rivalFor(String boss)
	{
		return config.friendHiscores() ? rivals.getOrDefault(boss, "Open sidebar to load friends") : "Enable friends hiscores to compare";
	}

	void start()
	{
		running = true;
		long life = lifecycle.incrementAndGet();
		leaderboard.start();
		SwingUtilities.invokeLater(() -> {
			if (!running || life != lifecycle.get()) return;
			if (navigation != null) toolbar.removeNavigation(navigation);
			panel = new BossSidebarPanel(boss -> clientThread.invokeLater(() -> {
				selected = boss; context = null; update();
			}), () -> clientThread.invokeLater(() -> { context = null; update(); }),
				() -> clientThread.invokeLater(() -> { active = true; context = null; update(); }),
				() -> clientThread.invokeLater(() -> {
					active = false; context = null; boardVersion.incrementAndGet(); leaderboard.cancel();
				}), (boss, goal) -> {
				String account = profile;
				clientThread.invokeLater(() -> {
					if (Objects.equals(account, configManager.getRSProfileKey())) { plugin.saveGoal(boss, goal); update(); }
				});
			});
			navigation = NavigationButton.builder().tooltip("Boss Kill Milestones")
				.icon(BossSidebarPanel.icon(24)).priority(7).panel(panel).build();
			toolbar.addNavigation(navigation);
			clientThread.invokeLater(() -> {
				if (life != lifecycle.get()) return;
				active = false; selected = null; profile = null; context = null; pictured = null; update();
			});
		});
	}

	void stop()
	{
		running = false;
		long life = lifecycle.incrementAndGet();
		boardVersion.incrementAndGet();
		leaderboard.stop();
		rivals.clear();
		SwingUtilities.invokeLater(() -> {
			if (life != lifecycle.get()) return;
			if (navigation != null) toolbar.removeNavigation(navigation);
			navigation = null; panel = null;
		});
	}

	void requestUpdate()
	{
		clientThread.invokeLater(this::update);
	}

	void update()
	{
		BossSidebarPanel target = panel;
		if (!running || target == null) return;
		// Region loading is not a logout: player/profile data may briefly be absent.
		if (preserveDuringTransition(client.getGameState(), profile, configManager.getRSProfileKey()))
		{
			if (!config.friendHiscores())
			{
				boardVersion.incrementAndGet(); leaderboard.cancel(); context = null;
				ui(target, () -> target.message("Enable Friends hiscore leaderboard in the plugin settings."));
			}
			return;
		}
		boolean loggedIn = client.getGameState() == GameState.LOGGED_IN
			&& configManager.getRSProfileKey() != null && client.getLocalPlayer() != null;
		String account = loggedIn ? configManager.getRSProfileKey() : null;
		if (!Objects.equals(profile, account))
		{
			profile = account; context = null; pictured = null;
			boardVersion.incrementAndGet(); leaderboard.clear();
			rivals.clear();
		}
		String boss = loggedIn ? (selected == null ? plugin.getActiveBoss() : selected) : null;
		BossKillMilestonesPlugin.Progress progress = loggedIn ? plugin.progressFor(boss)
			: new BossKillMilestonesPlugin.Progress("Log in to view kills", 0, 0, "Unknown");
		ui(target, () -> target.showProgress(progress));
		ui(target, () -> target.showRival(boss == null ? "Choose a boss" : rivalFor(boss)));
		HiscoreSkill skill = BossNames.hiscore(boss);
		if (!Objects.equals(pictured, boss))
		{
			pictured = boss;
			if (skill != null && skill.getSpriteId() >= 0)
			{
				sprites.getSpriteAsync(skill.getSpriteId(), 0, image -> ui(target, () -> target.showImage(boss, image)));
			}
		}
		String message = !loggedIn ? "Log in to view your account's friends."
			: !config.friendHiscores() ? "Enable Friends hiscore leaderboard in the plugin settings."
			: skill == null ? "Choose a boss with public hiscores. No hiscores are available for the test guard or entry modes."
			: !active ? "Open this panel to load friends." : null;
		List<String> names = new ArrayList<>();
		String own = loggedIn ? client.getLocalPlayer().getName() : null;
		if (message == null)
		{
			if (client.getFriendContainer() != null)
				for (Nameable friend : client.getFriendContainer().getMembers()) names.add(friend.getName());
			names.sort(String.CASE_INSENSITIVE_ORDER);
			names.add(0, own);
		}
		List<String> roster = FriendLeaderboard.uniqueNames(names);
		String nextContext = account + "|" + boss + "|" + message + "|" + roster;
		if (nextContext.equals(context)) return;
		context = nextContext;
		long version = boardVersion.incrementAndGet();
		leaderboard.cancel();
		if (message != null)
		{
			ui(target, () -> { if (version == boardVersion.get()) target.message(message); });
			return;
		}
		leaderboard.refresh(roster, skill, view -> ui(target, () -> {
			if (version == boardVersion.get())
			{
				String rival = GrindProgress.rival(view, own);
				rivals.put(boss, rival);
				target.showRival(rival);
				target.showBoard(view, own);
			}
		}));
	}

	static boolean preserveDuringTransition(GameState state, String previousProfile, String currentProfile)
	{
		return previousProfile != null && (currentProfile == null || previousProfile.equals(currentProfile))
			&& (state == GameState.LOADING || state == GameState.HOPPING || state == GameState.CONNECTION_LOST);
	}

	private void ui(BossSidebarPanel target, Runnable action)
	{
		long life = lifecycle.get();
		SwingUtilities.invokeLater(() -> {
			if (running && life == lifecycle.get() && panel == target) action.run();
		});
	}
}
