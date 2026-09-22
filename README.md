# Boss Kill Milestones

Tracks boss kills completed while the plugin is enabled and celebrates milestones with a sound and chat message. Progress is saved per RuneScape profile across RuneLite restarts. Bosses can be excluded by entering their names in the plugin settings, separated by commas.

Milestones are 10, 25, then every 50 kills (50, 100, 150, and so on). At each milestone, the plugin reports both its saved count and the total kill count reported by the game.

Sounds: 10 kills plays birthday-party-horn; 25 plays community-claps; 50 and subsequent 50-kill milestones play wow-thats-amazing. The supplied clips are bundled as PCM WAV resources, including converted copies of the two MP3s. Playback uses desktop audio and works independently of the game's sound slider. Disable **Play milestone sound** to mute them. Disabling the plugin stops its audio.

## On-screen counter and guard testing

The compact **Show kill counter** overlay displays the target you are fighting (or most recently tracked/synced target), kills this login session, saved kills since enabling the plugin, and the lifetime total. Session counts reset on logout, account change, client restart, or disabling the plugin; world hops preserve them. Saved milestone counts remain intact. A `~` before the total means a loot-based estimate, and `Unknown` means no lifetime total has been synced.

**Varrock guard testing** is enabled by default for this development version. Level 21 Guards on the surface in Varrock are recorded as a separate `Varrock guard (test)` target, using server loot events. This test switch operates independently of the boss loot-backup switch. It exercises the same milestone sounds and saved/session counters. Historical lifetime guard kills are unavailable, so Total remains Unknown. Turn the switch off to return to boss-only tracking; excluding `Varrock guard (test)` also suppresses test counting.

## Setup in game

Keep the game's boss kill-count messages enabled. These supply authoritative totals. **Loot backup**, enabled by default, also counts recognised boss server-loot events when chat is absent. You can switch the backup off in settings. It does not require the RuneLite Loot Tracker plugin, but does require the game to supply a server-loot event for that encounter. Chests, raids, and other encounters without a recognised NPC server-loot event still rely on chat.

Chat and loot signals for the same boss are paired one-to-one within eight game ticks (about 4.8 seconds) to avoid counting both. This is a bounded timing match, not a server-provided kill identifier; unusually delayed events or very fast consecutive kills with alternating missing signals need in-game verification. Ordinary NPC loot is ignored.

## Syncing lifetime totals

On login, an optional reminder asks you to open a boss's **Combat Achievements details** with its kill count visible. A successful read displays a confirmation and saves that boss's lifetime total per RuneScape profile. The reminder stops after the first successful sync; saved values survive client restarts. Opening a boss's details again refreshes its saved total.

Opening the Combat Achievements overview does not load every boss. Each boss needs its own displayed total. Only explicitly labelled, unambiguous kill/completion totals are accepted; achievement task progress and personal-best times are ignored. The interface reader still needs confirmation against the live game layout: if there is no sync confirmation, no total was imported.

Syncing corrects lifetime totals, including downward corrections, but never adds historical kills to the plugin's milestone counter or triggers old milestones. Backup kills advance a separate lifetime estimate once a starting total is known. Milestone messages label those totals as estimated; without a starting total they say the lifetime total is unconfirmed. Chat messages and later syncs replace the estimate with the reported game total. Kills made while the plugin is disabled do not get backfilled into milestone progress.

In the plugin settings, add comma-separated boss names to **Excluded bosses**. Matching ignores capitalization and extra spaces. Turn off **Play milestone sound** to keep the chat message without the celebratory sound.

## In-game checks

- Open a boss's CA details and check for a sync confirmation matching the displayed total. Reopen it and restart the client to check persistence and the initial reminder.
- With chat and loot backup enabled, confirm a milestone arrives at the expected kill count, not twice as fast. Disable chat briefly to check a supported boss's loot fallback, then restore it.
- Toggle the backup off and check that loot alone no longer advances the counter. Check an excluded boss and a different account.
- Resync after a kill and confirm it updates the lifetime total without advancing milestone progress.

## Development

Open this folder as a Gradle project in IntelliJ IDEA, then run the Gradle `run` task to launch RuneLite with the plugin loaded.

The project is based on RuneLite's [official example plugin](https://github.com/runelite/example-plugin) and follows the [Plugin Hub guide](https://github.com/runelite/plugin-hub#creating-new-plugins).

## TO-DO: 
MAYBE: GUI tracker average kill time?
CHANGE: make Excluded Bosses field bigger
ADD: GUI tracker timeout
MAYBE: side panel? could track current boss(showing an image or 3d model) and show lifetime, this session, since plugin activation, average time(?)
MAYBE: Compare friend totals and show small leaderboard in side panel? Underneath picture or 3d model of boss and the totals and average time?
