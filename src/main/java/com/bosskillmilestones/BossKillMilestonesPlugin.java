package com.bosskillmilestones;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.events.ServerNpcLoot;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.Text;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Boss Kill Milestones",
	description = "Tracks boss kill milestones and celebrates them with jingles.",
	tags = {"boss", "kill count", "milestone", "jingle"}
)
public class BossKillMilestonesPlugin extends Plugin
{
	private static final String PROFILE_GROUP = "boss-kill-milestones";
	private final KillSignals signals = new KillSignals();
	private final OverlayActivity overlayActivity = new OverlayActivity(System::nanoTime);
	private final Map<String, Integer> lastLootTicks = new HashMap<>();
	private String profile;
	private boolean reminded;
	private String lastPanel;
	private final Map<String, Integer> sessionKills = new HashMap<>();
	private String activeBoss;
	private volatile Progress progress = new Progress("Waiting for a target", 0, 0, "Unknown");

	static final class Progress
	{
		final String name;
		final int session;
		final int saved;
		final String total;
		final int goal;
		Progress(String name, int session, int saved, String total)
		{
			this(name, session, saved, total, 0);
		}
		Progress(String name, int session, int saved, String total, int goal)
		{
			this.goal = goal;
			this.name = name;
			this.session = session;
			this.saved = saved;
			this.total = total;
		}
	}

	Progress getProgress()
	{
		return progress;
	}

	boolean isCounterVisible()
	{
		return overlayActivity.visible(config.alwaysOnGui(), config.guiTimeoutMinutes());
	}
	private static final Pattern KILL_COUNT_PATTERN = Pattern.compile(
		"Your (?<prefix>completion count for |subdued |completed )?(?:<col=[0-9a-f]{6}>)?(?<boss>.+?)(?:</col>)? "
			+ "(?<suffix>(?:(?:kill|harvest|lap|completion|success|Total Ticket) )?(?:count )?)is: ?"
			+ "(?:<col=[0-9a-f]{6}>|@.+?@)(?<total>[0-9,]+)</col>");

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private BossKillMilestonesConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private BossMilestoneOverlay overlay;
	@Inject
	private MilestoneAudio milestoneAudio;
	@Inject
	private BossSidebar sidebar;

	@Override
	protected void startUp()
	{
		clearSession();
		milestoneAudio.start();
		overlayManager.add(overlay);
		sidebar.start();
		log.debug("Boss Kill Milestones started");
	}

	@Override
	protected void shutDown()
	{
		sidebar.stop();
		overlayManager.remove(overlay);
		milestoneAudio.stop();
		clearSession();
		resetTransientState();
		profile = null;
		log.debug("Boss Kill Milestones stopped");
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!ready())
		{
			return;
		}
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		Matcher matcher = KILL_COUNT_PATTERN.matcher(event.getMessage());
		if (!matcher.find() || !isBossKillCountMessage(matcher.group("prefix"), matcher.group("suffix")))
		{
			return;
		}

		String bossName = BossNames.known(Text.removeTags(matcher.group("boss")));
		if (bossName == null)
		{
			return;
		}

		int total;
		try
		{
			total = Integer.parseInt(matcher.group("total").replace(",", ""));
		}
		catch (NumberFormatException ex)
		{
			log.debug("Could not parse kill count for {}", bossName);
			return;
		}

		String key = BossNames.key(bossName);
		Integer previousTotal = configManager.getRSProfileConfiguration(PROFILE_GROUP, "total-" + key, int.class);
		Integer lastChat = configManager.getRSProfileConfiguration(PROFILE_GROUP, "last-chat-" + key, int.class);
		// The old version stored only total-. Use it until the first new chat receipt.
		if (lastChat == null)
		{
			lastChat = previousTotal;
		}
		if (lastChat != null && total <= lastChat)
		{
			return;
		}
		configManager.setRSProfileConfiguration(PROFILE_GROUP, "total-" + key, total);
		configManager.setRSProfileConfiguration(PROFILE_GROUP, "estimated-total-" + key, total);
		configManager.setRSProfileConfiguration(PROFILE_GROUP, "last-chat-" + key, total);
		if (isExcluded(bossName))
		{
			return;
		}
		if (signals.accept(key, false, client.getTickCount()))
		{
			recordKill(bossName, total, false);
		}
		activeBoss = bossName;
		refreshProgress();
	}

	private void recordKill(String bossName, Integer total, boolean estimated)
	{
		overlayActivity.killed();
		String key = BossNames.key(bossName);
		Integer savedMilestones = configManager.getRSProfileConfiguration(PROFILE_GROUP, "since-enabled-" + key, int.class);
		int sinceEnabled = savedMilestones == null ? 0 : savedMilestones;
		// Count the kill that generated this message, but don't replay kills from while
		// the plugin was off when the reported total jumps by more than one.
		sinceEnabled++;
		configManager.setRSProfileConfiguration(PROFILE_GROUP, "since-enabled-" + key, sinceEnabled);
		sessionKills.merge(key, 1, Integer::sum);
		activeBoss = bossName;
		refreshProgress();

		if (isMilestone(sinceEnabled))
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You've killed " + sinceEnabled + " " + bossName
				+ " since Boss Kill Milestones was enabled!" + (total == null ? " (lifetime total unconfirmed)"
				: " (" + total + (estimated ? " estimated total)" : " total)")), null);
			if (config.playMilestoneSound())
			{
				milestoneAudio.play(sinceEnabled);
			}
		}
	}

	private boolean isExcluded(String bossName)
	{
		String normalizedBoss = BossNames.normalize(bossName);
		for (String excluded : config.excludedBosses().split(","))
		{
			String known = BossNames.known(excluded);
			if (normalizedBoss.equals(BossNames.normalize(known == null ? excluded : known)))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean isMilestone(int kills)
	{
		return kills == 10 || kills == 25 || (kills >= 50 && kills % 50 == 0);
	}

	private static boolean isBossKillCountMessage(String prefix, String suffix)
	{
		return (suffix != null && (suffix.contains("kill count")
			|| suffix.contains("completion count")
			|| suffix.contains("success count")))
			|| (prefix != null && (prefix.startsWith("subdued ") || prefix.startsWith("completed ")
			|| prefix.startsWith("completion count for ")));
	}

	@Subscribe
	public void onServerNpcLoot(ServerNpcLoot event)
	{
		if (!ready() || event.getComposition() == null)
		{
			return;
		}
		boolean testGuard = config.testGuard() && client.getLocalPlayer() != null
			&& TestGuard.matches(event.getComposition().getName(), event.getComposition().getCombatLevel(), client.getLocalPlayer().getWorldLocation());
		if (!testGuard && !config.lootBackup()) return;
		String boss = testGuard ? TestGuard.NAME : BossNames.known(event.getComposition().getName());
		if (boss == null || isExcluded(boss))
		{
			return;
		}
		String key = BossNames.key(boss);
		int tick = client.getTickCount();
		Integer previous = lastLootTicks.put(key, tick);
		if (previous != null && previous == tick)
		{
			return;
		}
		if (signals.accept(key, true, tick))
		{
			Integer estimate = configManager.getRSProfileConfiguration(PROFILE_GROUP, "estimated-total-" + key, int.class);
			if (estimate == null)
			{
				estimate = configManager.getRSProfileConfiguration(PROFILE_GROUP, "total-" + key, int.class);
			}
			if (estimate != null)
			{
				estimate++;
				configManager.setRSProfileConfiguration(PROFILE_GROUP, "estimated-total-" + key, estimate);
			}
			recordKill(boss, estimate, true);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			clearSession();
		}
		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			resetTransientState();
		}
		sidebar.update();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (PROFILE_GROUP.equals(event.getGroup())) sidebar.requestUpdate();
	}

	private void resetTransientState()
	{
		signals.clear();
		lastLootTicks.clear();
		reminded = false;
		lastPanel = null;
	}

	private boolean ready()
	{
		if (client.getGameState() != GameState.LOGGED_IN || configManager.getRSProfileKey() == null)
		{
			return false;
		}
		String current = configManager.getRSProfileKey();
		if (!Objects.equals(profile, current))
		{
			clearSession();
			resetTransientState();
			profile = current;
			migrateNames();
		}
		return true;
	}

	private void migrateNames()
	{
		// Preserve records written before aliases were canonicalised.
		String[] aliases = {"The Nightmare", "Barrows chest", "Gauntlet", "Corrupted Gauntlet", "Hueycoatl", "Royal Titans", "Leviathan", "Whisperer"};
		for (String alias : aliases)
		{
			String canonical = BossNames.known(alias);
			if (canonical == null || BossNames.key(alias).equals(BossNames.key(canonical)))
			{
				continue;
			}
			for (String prefix : new String[]{"since-enabled-", "total-"})
			{
				String target = prefix + BossNames.key(canonical);
				Integer old = configManager.getRSProfileConfiguration(PROFILE_GROUP, prefix + BossNames.key(alias), int.class);
				if (old != null && configManager.getRSProfileConfiguration(PROFILE_GROUP, target, int.class) == null)
				{
					configManager.setRSProfileConfiguration(PROFILE_GROUP, target, old);
				}
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!ready())
		{
			return;
		}
		if (!reminded)
		{
			reminded = true;
			Boolean synced = configManager.getRSProfileConfiguration(PROFILE_GROUP, "initial-sync", boolean.class);
			if (config.syncReminder() && !Boolean.TRUE.equals(synced))
			{
				message("To sync lifetime totals, open a boss's Combat Achievements details showing its kill count. Each boss is saved separately and refreshed whenever you revisit it.");
			}
		}
		syncCombatAchievements();
		refreshProgress();
		sidebar.update();
	}

	private void syncCombatAchievements()
	{
		Widget title = client.getWidget(InterfaceID.CaBoss.BOSS_NAME);
		Widget stats = client.getWidget(InterfaceID.CaBoss.CA_BOSS_STATS);
		if (title == null || stats == null || title.isHidden() || stats.isHidden())
		{
			lastPanel = null;
			return;
		}
		String boss = BossNames.known(Text.removeTags(title.getText()));
		StringBuilder text = new StringBuilder();
		appendText(stats, text);
		String fingerprint = boss + "\n" + text;
		if (fingerprint.equals(lastPanel))
		{
			return;
		}
		lastPanel = fingerprint;
		Integer total = BossStats.parse(text.toString());
		if (boss == null || total == null)
		{
			return;
		}
		// Sync is a snapshot, never a kill event: do not modify milestone progress.
		configManager.setRSProfileConfiguration(PROFILE_GROUP, "total-" + BossNames.key(boss), total);
		configManager.setRSProfileConfiguration(PROFILE_GROUP, "estimated-total-" + BossNames.key(boss), total);
		configManager.setRSProfileConfiguration(PROFILE_GROUP, "last-chat-" + BossNames.key(boss), total);
		configManager.setRSProfileConfiguration(PROFILE_GROUP, "initial-sync", true);
		activeBoss = boss;
		refreshProgress();
		message("Synced " + boss + ": " + total + " lifetime kills. Your milestone progress is unchanged.");
	}

	private static void appendText(Widget widget, StringBuilder output)
	{
		if (widget.isHidden())
		{
			return;
		}
		String value = widget.getText();
		if (value != null && !value.isEmpty())
		{
			output.append(value).append('\n');
		}
		Widget[] children = widget.getChildren();
		if (children != null)
		{
			for (Widget child : children)
			{
				if (child != null)
				{
					appendText(child, output);
				}
			}
		}
	}

	private void message(String text)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Boss Kill Milestones: " + text, null);
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (!ready() || event.getSource() != client.getLocalPlayer() || !(event.getTarget() instanceof NPC)) return;
		NPC npc = (NPC) event.getTarget();
		String boss = config.testGuard() && TestGuard.matches(npc.getName(), npc.getCombatLevel(), npc.getWorldLocation())
			? TestGuard.NAME : BossNames.known(npc.getName());
		if (boss != null && !isExcluded(boss))
		{
			activeBoss = boss;
			refreshProgress();
		}
	}

	private void clearSession()
	{
		overlayActivity.reset();
		sessionKills.clear();
		activeBoss = null;
		progress = new Progress("Waiting for a target", 0, 0, "Unknown");
	}

	private void refreshProgress()
	{
		if (activeBoss == null) return;
		if (isExcluded(activeBoss) || (TestGuard.NAME.equals(activeBoss) && !config.testGuard()))
		{
			activeBoss = null;
			progress = new Progress("Waiting for a target", 0, 0, "Unknown");
			return;
		}
		progress = progressFor(activeBoss);
	}

	String getActiveBoss()
	{
		return activeBoss;
	}

	Progress progressFor(String boss)
	{
		if (boss == null) return new Progress("Choose a boss", 0, 0, "Unknown");
		String key = BossNames.key(boss);
		Integer saved = configManager.getRSProfileConfiguration(PROFILE_GROUP, "since-enabled-" + key, int.class);
		Integer total = configManager.getRSProfileConfiguration(PROFILE_GROUP, "total-" + key, int.class);
		Integer estimate = configManager.getRSProfileConfiguration(PROFILE_GROUP, "estimated-total-" + key, int.class);
		String totalText = total == null ? "Unknown" : total.toString();
		if (estimate != null && !estimate.equals(total)) totalText = "~" + estimate;
		Integer goal = configManager.getRSProfileConfiguration(PROFILE_GROUP, "goal-" + key, int.class);
		return new Progress(boss, sessionKills.getOrDefault(key, 0), saved == null ? 0 : saved, totalText, goal == null ? 0 : goal);
	}

	void saveGoal(String boss, int goal)
	{
		if (!ready() || !BossNames.all().contains(boss) || goal < 0) return;
		configManager.setRSProfileConfiguration(PROFILE_GROUP, "goal-" + BossNames.key(boss), goal);
		refreshProgress();
	}

	String rivalFor(String boss)
	{
		return sidebar.rivalFor(boss);
	}

	@Provides
	BossKillMilestonesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BossKillMilestonesConfig.class);
	}
}
