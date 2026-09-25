# Asset notices

## Original audio

The current plugin contains six original, programmatically composed and synthesised sounds. No external recordings, sample packs, speech clips, or Pixabay audio are used in these assets.

| Resource | Use | Duration |
| --- | --- | --- |
| little-victory.wav | First 10 and every 50 saved kills | 1.7 seconds |
| golden-steps.wav | First 25 saved kills | 2.6 seconds |
| champion-fanfare.wav | Every 250 saved kills | 3.8 seconds |
| legendary-victory.wav | Every 1,000 saved kills | 5.3 seconds |
| goal-complete.wav | Personal goal completion | 3.6 seconds |
| session-spark.wav | Optional session encouragement | 0.95 seconds |

Audio files live in `src/main/resources/audio`. The reproducible composition and synthesis source is [tools/generate_audio.py](tools/generate_audio.py), a development-only tool. The project's [BSD-2-Clause licence](LICENSE) applies to this original source and the generated audio to the extent rights apply. Existing copyright notices in template-derived files remain intact.

## Retired third-party audio

Earlier development revisions used Birthday Party Horn by Universfield, Crowd Cheering by storegraphic (trimmed and sped up by the author), and Wow that's amazing - Girl by Lucy_voice_character from Pixabay. Those recordings and their converted copies have been removed from the current source tree and are not included in the release candidate. Local originals were preserved outside the plugin repository.

Previous public commits may still contain those recordings. Their original terms remain separate from this project's licence; replacing them does not retroactively relicense or remove historical copies.

Historical sources:
- https://pixabay.com/sound-effects/film-special-effects-birthday-party-horn-250238/
- https://pixabay.com/sound-effects/people-crowd-cheering-310544/
- https://pixabay.com/sound-effects/people-wow-thatx27s-amazing-girl-229854/
- https://pixabay.com/service/terms/

## Graphics

The gold skull icon is drawn by the plugin's own Java2D code. Boss sprites are obtained from the running game through RuneLite's sprite API, not distributed as copied image resources or relicensed by this project.

## Acknowledgements

Thanks Cow and the Lucipurr boys for support and the idea.
