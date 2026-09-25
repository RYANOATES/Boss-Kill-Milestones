# Boss Milestones: Rank Among Friends

Compare boss kills with friends, climb the leaderboard, and celebrate milestones.

## Getting started

1. Search for **Boss Milestones: Rank Among Friends** in RuneLite's **Plugin Hub** and install it.
2. Keep the game's boss kill-count messages enabled.
3. Click the **gold skull** sidebar icon to view your progress or select a boss.
4. Open a boss's **Combat Achievements details** to sync its lifetime kill count.
5. Set a lifetime goal in the sidebar, or enable **Friends hiscore leaderboard** for friendly competition.

## Features

- **Three kill counters:** this session, since the plugin was enabled, and lifetime total.
- **Personal goals:** set a lifetime target for each boss, with a progress bar and completion celebration. Enter **0** to clear a goal.
- **Escalating jingles:** bigger milestones earn bigger celebrations.
- **Friends leaderboard:** compare public boss kills with your actual friends list, including offline friends. Your own row is highlighted in green.
- **Friend to beat:** see how many kills you need to overtake the next friend.
- **Saved progress:** goals and tracked kills persist per boss and account profile across restarts.

## Celebrations

| Milestone | Sound |
| --- | --- |
| First 10 tracked kills | Little Victory |
| First 25 tracked kills | Golden Steps |
| Every 50 tracked kills | Little Victory |
| Every 250 tracked kills | Champion Fanfare |
| Every 1,000 tracked kills | Legendary Victory |
| Personal goal reached | Goal Complete |
| Optional: every 10 or 25 session kills | Session Spark |

Milestones use **kills tracked since activation**, not historical lifetime kills. Only one sound plays per kill: personal goals take priority, followed by the largest milestone. All six sounds are original.

## Make it yours

- **Play milestone sound:** mute celebrations while keeping chat messages.
- **Session encouragement:** optional rewards every 10 or 25 session kills.
- **Always on GUI:** keep the overlay visible before your first kill and while idle.
- **GUI timeout:** hide the overlay after 1–60 minutes without a kill; default **5 minutes**.
- **Show goals on overlay:** switch between the fuller display and a compact counter.
- **Excluded bosses:** enter comma-separated names, such as `Sarachnis, Vorkath`.
- **Loot backup:** count recognised boss loot events when a kill-count message is missing.

The overlay normally appears after your first tracked kill. Session counts reset on logout or disabling the plugin; saved progress remains. World hops preserve session counts.

## Totals and friend scores

**Unknown** means no lifetime total has been obtained. Open the individual boss's Combat Achievements details and look for a sync confirmation. A **~** marks a loot-based estimate. Syncing does not add historical kills to milestone progress or replay celebrations.

The friends leaderboard is **off by default**. Enabling it sends queried usernames to the official OSRS hiscores service, which also receives your IP address. Results load while the sidebar is open and are cached for up to **10 minutes**. **Refresh friends** updates the roster and reloads missing or expired results. These are public main-game totals, not live scores; unranked or unavailable results are not treated as zero.

## Support

Found a bug? [Open an issue](https://github.com/RYANOATES/Boss-Kill-Milestones/issues) with the boss name, what happened, and a screenshot if possible.

[BSD-2-Clause licence](LICENSE) · [Asset credits](ASSET-NOTICES.md)

*Thanks Cow and the Lucipurr boys for support and the idea.*
