package com.bosskillmilestones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.hiscore.HiscoreClient;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.hiscore.Skill;

@Singleton
final class FriendLeaderboard
{
	static final class Row
	{
		final String name;
		final Integer kills;
		final String status;
		Row(String name, Integer kills, String status)
		{
			this.name = name;
			this.kills = kills;
			this.status = status;
		}
	}

	static final class View
	{
		final List<Row> rows;
		final String status;
		View(List<Row> rows, String status)
		{
			this.rows = Collections.unmodifiableList(sorted(rows));
			this.status = status;
		}
	}

	private static final class Cached
	{
		final HiscoreResult result;
		final boolean failed;
		final long expires;
		Cached(HiscoreResult result, boolean failed)
		{
			this.result = result;
			this.failed = failed;
			this.expires = System.nanoTime() + TimeUnit.MINUTES.toNanos(failed ? 1 : 10);
		}
	}

	private final HiscoreClient hiscores;
	private final Map<String, Cached> cache = new LinkedHashMap<>();
	private ScheduledExecutorService worker;
	private ScheduledFuture<?> pending;
	private long generation;
	private long nextLookup;

	@Inject
	FriendLeaderboard(HiscoreClient hiscores)
	{
		this.hiscores = hiscores;
	}

	synchronized void start()
	{
		stop();
		worker = Executors.newSingleThreadScheduledExecutor(task -> {
			Thread thread = new Thread(task, "boss-friends-hiscores");
			thread.setDaemon(true);
			return thread;
		});
	}

	synchronized void cancel()
	{
		generation++;
		if (pending != null) pending.cancel(true);
		pending = null;
	}

	synchronized void clear()
	{
		cancel();
		cache.clear();
	}

	synchronized void stop()
	{
		clear();
		if (worker != null) worker.shutdownNow();
		worker = null;
	}

	synchronized void refresh(List<String> names, HiscoreSkill boss, Consumer<View> consumer)
	{
		cancel();
		if (worker == null) return;
		List<String> roster = uniqueNames(names);
		List<Row> rows = new ArrayList<>();
		for (String name : roster) rows.add(new Row(name, null, "Waiting"));
		consumer.accept(new View(rows, "Loading 0 / " + roster.size()));
		long request = generation;
		pending = worker.schedule(() -> next(request, roster, rows, boss, consumer, 0, 0), 0, TimeUnit.MILLISECONDS);
	}

	private void next(long request, List<String> names, List<Row> rows, HiscoreSkill boss,
		Consumer<View> consumer, int index, int failures)
	{
		if (index >= names.size()) return;
		String name = names.get(index);
		String key = name.toLowerCase(Locale.ROOT);
		Cached entry;
		synchronized (this)
		{
			if (request != generation || worker == null) return;
			entry = cache.get(key);
			if (entry == null || System.nanoTime() - entry.expires >= 0)
			{
				long delay = nextLookup - System.nanoTime();
				if (delay > 0)
				{
					pending = worker.schedule(() -> next(request, names, rows, boss, consumer, index, failures), delay, TimeUnit.NANOSECONDS);
					return;
				}
				nextLookup = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
			}
		}
		boolean fetched = entry == null || System.nanoTime() - entry.expires >= 0;
		if (fetched)
		{
			try { entry = new Cached(hiscores.lookup(name), false); }
			catch (Exception ex) { entry = new Cached(null, true); }
		}
		synchronized (this)
		{
			if (request != generation || worker == null) return;
			if (fetched)
			{
				if (cache.size() >= 500) cache.remove(cache.keySet().iterator().next());
				cache.put(key, entry);
			}
			rows.set(index, row(name, entry.result, boss, entry.failed));
			int errors = entry.failed ? failures + 1 : 0;
			boolean finished = index + 1 == names.size();
			String status = finished ? "Updated — cached up to 10 min" : "Loading " + (index + 1) + " / " + names.size();
			if (errors >= 3 && !finished) status = "Service unavailable — refresh later";
			consumer.accept(new View(rows, status));
			if (!finished && errors < 3)
			{
				pending = worker.schedule(() -> next(request, names, rows, boss, consumer, index + 1, errors),
					fetched ? 1000 : 0, TimeUnit.MILLISECONDS);
			}
		}
	}

	static Row row(String name, HiscoreResult result, HiscoreSkill boss, boolean failed)
	{
		if (failed) return new Row(name, null, "Unavailable");
		Skill skill = result == null ? null : result.getSkill(boss);
		if (skill == null || skill.getRank() < 0 || skill.getLevel() < 0) return new Row(name, null, "Unranked");
		return new Row(name, skill.getLevel(), "");
	}

	static List<Row> sorted(List<Row> rows)
	{
		List<Row> result = new ArrayList<>(rows);
		result.sort(Comparator.<Row, Integer>comparing(row -> row.kills, Comparator.nullsLast(Comparator.reverseOrder()))
			.thenComparing(row -> row.name, String.CASE_INSENSITIVE_ORDER));
		return result;
	}

	static List<String> uniqueNames(List<String> names)
	{
		Map<String, String> unique = new LinkedHashMap<>();
		for (String name : names)
		{
			if (name == null) continue;
			String clean = name.replace('\u00a0', ' ').trim();
			if (!clean.isEmpty()) unique.putIfAbsent(clean.toLowerCase(Locale.ROOT), clean);
		}
		return new ArrayList<>(unique.values());
	}
}
