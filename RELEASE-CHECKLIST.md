# Release candidate checklist

## Prepared

- Java 11 plugin with matching metadata and original source licence.
- Guard testing and external hiscore lookups off by default.
- 48 x 48 original skull icon for the Plugin Hub.
- Six original synthesised sounds; no third-party audio in the current source resources.
- Escalating milestones with deterministic single-cue priority.
- Persistent goal celebration markers; sync and existing completed goals do not replay rewards.
- Combat Achievements sync confirmed working in-game by the author.

## Final author checks

- Test original cue playback and acceptable loudness in-game.
- Verify a nearby personal goal celebrates once, including after restart.
- Verify session encouragement, mute, and an overlapping celebration.
- Confirm normal boss tracking remains correct after this update.
- Capture real screenshots for the README if desired.

## Submission (not performed automatically)

1. Review the final changes and commit/push the candidate to the plugin repository.
2. Use the full resulting commit hash in a Plugin Hub manifest on a separate branch of a plugin-hub fork.
3. Submit a pull request, wait for automated checks and reviewer feedback, and address any requests.

Follow the current official guide: https://github.com/runelite/plugin-hub#submitting-a-plugin

The standalone development launcher JAR is for local testing, not a substitute for Plugin Hub submission. No tag, GitHub release, or Plugin Hub PR has been created as part of this preparation.

Historical commits still contain the retired Pixabay clips. They are absent from this candidate, but removing current files does not erase history. Do not rewrite public history without the owner's explicit approval; resolve any historical distribution concern separately.
