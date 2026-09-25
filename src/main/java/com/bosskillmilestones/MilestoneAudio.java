package com.bosskillmilestones;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Singleton;
import javax.inject.Inject;
import net.runelite.client.audio.AudioPlayer;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
final class MilestoneAudio
{
	private ExecutorService worker;
	private final AudioPlayer audioPlayer;
	private int generation;

	@Inject
	MilestoneAudio(AudioPlayer audioPlayer)
	{
		this.audioPlayer = audioPlayer;
	}

	synchronized void start()
	{
		stop();
		worker = Executors.newSingleThreadExecutor(task -> {
			Thread thread = new Thread(task, "boss-milestone-audio");
			thread.setDaemon(true);
			return thread;
		});
	}

	synchronized void stop()
	{
		generation++;
		if (worker != null)
		{
			worker.shutdownNow();
			worker = null;
		}
	}

	static String resourceFor(int kills)
	{
		Celebration celebration = Celebration.select(kills, 0, 0, false);
		return celebration == null ? null : celebration.resource;
	}

	synchronized void play(Celebration celebration)
	{
		if (worker == null) return;
		int requestedGeneration = generation;
		worker.execute(() -> loadAndPlay(celebration.resource, requestedGeneration));
	}

	private void loadAndPlay(String resource, int requestedGeneration)
	{
		synchronized (this)
		{
			if (worker == null || generation != requestedGeneration) return;
		}
		try
		{
			// Resource decoding and playback stay off the client thread. RuneLite owns
			// the audio line lifecycle; an already-started cue finishes naturally.
			audioPlayer.play(MilestoneAudio.class, resource, 0f);
		}
		catch (Exception ex)
		{
			log.warn("Could not play milestone sound {}", resource, ex);
		}
	}
}
