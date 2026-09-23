# Boss Kill Milestones
### Make every grind count.

**Boss goals. Friendly competition. A little celebration along the way.**

Built for the long grind: the rare drop that refuses to appear, the collection-log slot that stays empty, and the boss you have defeated a thousand times already. Boss Kill Milestones gives each session something visible to work towards—your next jingle, a personal kill target, or the friend just ahead of you.

Track your progress in a boss sidebar and a compact on-screen overlay, with records saved separately for each RuneScape profile.

> **Development build:** this repository contains the plugin and its development launcher. Plugin Hub availability is not implied. The Varrock guard test option is currently enabled by default; switch it off for normal boss-only use.

[Getting started](#getting-started) · [Features](#features) · [Settings](#settings) · [Counting and syncing](#counting-and-syncing) · [Troubleshooting](#troubleshooting) · [Development](#development)

---

## Features

### Celebrate the small wins

A chat message and optional sound celebrate kills tracked while the plugin is enabled.

| Tracked kills for a boss | Celebration |
| :--- | :--- |
| 10 | Birthday party horn |
| 25 | Community claps |
| 50, 100, 150, and every further 50 | Wow, that's amazing |

For example, a 150-kill milestone with a confirmed lifetime total displays:

> You've killed 150 Sarachnis since Boss Kill Milestones was enabled! (1250 total)

Milestones are separate for each boss. Existing lifetime kills are not replayed as celebrations when you first enable the plugin.

Sounds are bundled as PCM WAV resources, including converted versions of the supplied MP3s. They use desktop audio independently of the game's sound slider. Turn off **Play milestone sound** to keep milestone chat messages without audio.

### Know exactly what your counters mean

| Counter | What it measures | Persistence |
| :--- | :--- | :--- |
| **This session** | Kills tracked during your current login/plugin session | Resets on logout, account change, client restart, or disabling the plugin; world hops preserve it |
| **Since activation / Since enabled** | Kills tracked while the plugin is enabled | Saved per boss and RuneScape profile; does not reset on a normal restart |
| **Total kills** | The lifetime total reported by the game, or an estimate based on that total | Saved per boss and profile; updated by kill-count messages and supported syncs |

A **~** marks an estimated lifetime total. **Unknown** means no lifetime baseline has been obtained. Neither means zero kills.

### Turn a long grind into a visible goal

- **Next milestone:** a gold progress bar shows your saved kill count against the next jingle threshold.
- **Personal goal:** a teal bar tracks a lifetime target you choose for that boss.
- **Goal reached:** a completed goal stays visible until you change or clear it.

To set a goal, select a boss in the sidebar, enter the desired **lifetime total**, and click **Set goal**. Enter **0** to clear it.

For example, if your lifetime total is 1,250 and you set a goal of 2,000, you have 750 kills to go. Goals survive restarts and are kept separately for each boss and account profile. An unknown total requires a sync before progress can be calculated; estimates remain labelled.

Personal goals do not change the regular jingle schedule or trigger an additional goal-completion sound.

### A sidebar built around your current boss

Click the **gold skull** in RuneLite's sidebar to see:

- The boss name and built-in boss icon.
- Lifetime, since-activation, and session counters.
- Milestone progress and personal-goal controls.
- Your next friend to beat and the optional friends leaderboard.

Choose **Follow current boss** to follow your current or most recently tracked target, or select another boss to browse its records. The overlay continues following your active boss independently of the sidebar selection.

Boss images are static game icons, not interactive 3D models. Targets without a supported icon use a skull placeholder.

### Friendly competition, using your actual friends list

Enable **Friends hiscore leaderboard** to compare the selected boss's public lifetime kill counts.

The plugin reads your logged-in account's friends list—including offline friends—and includes your own account. Friends do not need this plugin, and there is no separate list to maintain.

- Ranked results are ordered by kill count, highest first.
- A **green summary row** directly under the headers shows your own rank and kills.
- Your account also stays in its normal ranked position below.
- **Unranked**, **Unavailable**, and **Waiting** results are not treated as zero.
- Unsupported targets, including the test guard and entry modes without a public boss entry, have no leaderboard.

**Friend to beat** picks the nearest loaded friend at or above your public kill count and shows the kills needed to overtake them. If you are tied, one more kill is needed. For example, a friend with 123 kills compared with your 100 gives **24 to pass**.

Comparisons use cached main-game hiscores—not live session counts or a separate Ironman leaderboard. Missing results are handled cautiously rather than declaring you ahead of everyone.

#### Refreshing, caching, and privacy

This feature is **off by default**. When enabled, public lookups send the queried player usernames to the official OSRS hiscores service, which also receives the request's IP address.

Requests load gradually in the background while the sidebar is open. Successful player results are cached in memory for up to **10 minutes** and reused across boss selections. **Refresh friends** rereads the roster and fetches missing or expired results; it does not bypass fresh cached results.

Large friends lists can take several minutes on their first load. Closing the sidebar cancels queued lookups, although an already-started request may finish. Area loading preserves the leaderboard; logout, account change, or disabling the plugin clears its cache.

Hiscores can lag behind the game. They never overwrite your local kill counters or goals. The overlay reuses loaded friend comparisons and does not start additional network requests.

### An overlay that knows when to get out of the way

The on-screen counter uses a dark panel, a gold boss heading, gold milestone progress, teal personal-goal progress, and blue friend-comparison text.

By default it:

1. Remains hidden until the first tracked kill of the session.
2. Appears when a kill is counted.
3. Hides after **5 minutes** without another tracked kill.

Change the timeout from **1–60 minutes**, or enable **Always on GUI** to bypass both the first-kill requirement and the idle timeout.

Attacking a boss, syncing a total, and receiving a duplicate kill signal do not restart the timer. Hiding the overlay never erases progress. The sidebar is independent of this timeout.

## Getting started

1. Launch the plugin's development client; see [Development](#development).
2. Enable **Boss Kill Milestones** in RuneLite's plugin settings.
3. Keep the game's boss kill-count messages enabled for authoritative totals.
4. For normal play, turn off **Varrock guard testing**.
5. Open a boss's Combat Achievements details with its kill count visible to sync a lifetime baseline.
6. Open the gold-skull sidebar, choose a boss, and optionally set a personal goal.
7. Enable **Friends hiscore leaderboard** if you want public friend comparisons.
8. Kill a supported boss: the overlay appears and your session and saved counts advance.

## Settings

| Setting | Default | Purpose |
| :--- | :--- | :--- |
| Show kill counter | On | Master switch for the on-screen overlay |
| Show goals on overlay | On | Include milestone progress, personal goals, and cached friend comparisons |
| Always on GUI | Off | Show the overlay before the first kill and ignore idle timeout |
| GUI timeout (minutes) | 5 | Hide the overlay after 1–60 minutes without a tracked kill |
| Play milestone sound | On | Play the bundled sound at each regular milestone |
| Friends hiscore leaderboard | Off | Opt into public hiscore lookups for your account and friends |
| Loot backup | On | Use recognised server-loot events when a matching kill message is absent |
| Initial sync reminder | On | Prompt at login until the first successful Combat Achievements sync for the profile |
| Excluded bosses | None | Comma-separated boss names to omit from tracked milestones |
| Varrock guard testing | On in this development build | Track level 21 surface Varrock Guards as a separate test target |

Exclusions ignore capitalization and extra spaces and recognise supported boss aliases. For example: `Sarachnis, Vorkath`.

Personal targets are set in the sidebar, not in the settings list. Turning off the goal sections or hiding the overlay does not clear those targets.

## Counting and syncing

### Primary detection: game kill-count messages

Recognised boss kill/completion messages supply authoritative lifetime totals. The plugin counts the newly observed kill, rather than importing the difference between old and new lifetime totals.

Kills made while the plugin was disabled are not backfilled into milestone progress.

### Optional backup: server loot

With **Loot backup** enabled, recognised boss server-loot events can count kills when the corresponding chat message is missing. This does not require RuneLite's Loot Tracker plugin.

Chat and loot signals for the same boss are paired one-to-one within eight game ticks—about 4.8 seconds—to avoid counting both. This is a bounded timing match, not a server-provided unique kill identifier. Unusually delayed events or rapid consecutive kills with alternating missing signals still need in-game checking.

Not every encounter produces a recognised NPC loot event. Chests, raids, and other unsupported loot paths continue to rely on recognised chat messages. Ordinary NPC loot is ignored, apart from the explicitly enabled guard test.

### Combat Achievements lifetime sync

Open the individual boss's **Combat Achievements details**, with its kill count visible. A successful read:

- Displays a sync confirmation.
- Saves or corrects that boss's lifetime total.
- Leaves session and since-activation counts unchanged.
- Does not trigger historical milestone sounds.

Opening the overview does **not** sync every boss. Each boss needs its own displayed total. Only explicit kill/completion labels are accepted; task progress and personal-best times are ignored. If no sync confirmation appears, no total was imported.

After the first successful sync, the initial reminder stops for that profile. Reopening a boss's details can refresh its saved total, including downward corrections. Backup kills advance an estimate once a baseline exists; later chat or sync totals replace the estimate.

## Troubleshooting

<details>
<summary><strong>Why is the overlay hidden?</strong></summary>

It waits for the first tracked kill and hides after the configured idle period. Check **Show kill counter**, or enable **Always on GUI** while testing. You must be logged in.

</details>

<details>
<summary><strong>Why is Total unknown, or different from the leaderboard?</strong></summary>

Unknown means the game has not supplied a lifetime baseline. Open the boss's Combat Achievements details or obtain a recognised kill-count message.

The leaderboard uses cached public hiscores, while the local total uses game messages, syncs, and optional estimates. These sources do not necessarily update together.

</details>

<details>
<summary><strong>Why does Refresh friends not immediately change a score?</strong></summary>

Results less than 10 minutes old are reused, and the public hiscores themselves can be behind the game. The button updates the friends roster and reloads missing or expired results, rather than forcing every player lookup.

</details>

<details>
<summary><strong>Why has the friend-to-beat target not changed after a kill?</strong></summary>

It compares cached public totals, not your live local kill count. Open the sidebar to load the boss's friends results, and refresh once those results expire. It needs a ranked public total for your own account.

</details>

<details>
<summary><strong>How can I test without killing a boss?</strong></summary>

Enable **Varrock guard testing** and kill a level 21 Guard on the surface in Varrock. It appears as **Varrock guard (test)** and exercises saved/session counts, milestone sounds, and overlay visibility using server-loot events.

This setting works independently of **Loot backup**. Historical guard lifetime kills, a public leaderboard, and lifetime-goal progress are unavailable. Disable the setting afterwards to return to boss-only tracking.

</details>

## Development

The project targets **Java 11** and uses the Gradle wrapper. Open this folder as a Gradle project, or use these commands from the repository root:

```powershell
# Launch the development client
.\gradlew.bat run

# Run automated tests
.\gradlew.bat test

# Build the development launcher JAR
.\gradlew.bat shadowJar
```

For Jagex-account login, follow RuneLite's [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts) instructions.

The project is based on RuneLite's [official example plugin](https://github.com/runelite/example-plugin). Submission and packaging guidance lives in the [Plugin Hub guide](https://github.com/runelite/plugin-hub#creating-new-plugins). Building locally is not the same as publishing or receiving Plugin Hub approval.

### In-game verification checklist

Automated tests cover calculations and UI behaviour in isolation; they do not replace live account testing.

- [ ] Check chat and backup detection count a supported kill once, not twice.
- [ ] Check exclusions and the backup toggle.
- [ ] Sync a boss total without increasing milestone or session counts.
- [ ] Reach 10, 25, and 50 tracked kills and confirm the corresponding sounds.
- [ ] Check first-kill visibility, idle timeout, and always-on mode.
- [ ] Set, reach, change, and clear a personal goal; verify persistence after restart.
- [ ] Browse a different boss while the overlay continues following the active target.
- [ ] Compare ranked, offline, and unranked friends against public hiscores.
- [ ] Confirm the green summary row and normal ranked row agree.
- [ ] Load a new region and confirm the leaderboard remains intact.
- [ ] Log out or change accounts and confirm old account data is no longer displayed.

### Ideas for future polish

These are suggestions, **not implemented features**:

- Sound volume control and a preview button.
- A small, optional notification when a personal goal is reached.
- A visible last-refreshed time for friend comparisons.
- Grouped settings and a more convenient boss exclusion picker.
- Session recaps and average kill times where encounter timing is reliable.
- A release-ready first-run setup and guard testing disabled by default.
- Real in-game screenshots and a short demonstration clip for this README.

---

**One more kill. One step closer.**
