package com.bosskillmilestones;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Singleton;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
final class MilestoneAudio
{
	private ExecutorService worker;
	private Clip playing;
	private int generation;

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
		if (playing != null)
		{
			playing.close();
			playing = null;
		}
	}

	static String resourceFor(int kills)
	{
		if (kills == 10) return "/audio/birthday-party-horn.wav";
		if (kills == 25) return "/audio/community-claps.wav";
		return "/audio/wow-thats-amazing.wav";
	}

	synchronized void play(int kills)
	{
		if (worker == null) return;
		int requestedGeneration = generation;
		worker.execute(() -> loadAndPlay(resourceFor(kills), requestedGeneration));
	}

	private void loadAndPlay(String resource, int requestedGeneration)
	{
		Clip clip = null;
		try (InputStream input = MilestoneAudio.class.getResourceAsStream(resource))
		{
			if (input == null) throw new IllegalStateException("Missing sound: " + resource);
			try (AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(input)))
			{
				clip = AudioSystem.getClip();
				clip.open(audio);
			}
			synchronized (this)
			{
				if (worker == null || generation != requestedGeneration)
				{
					clip.close();
					return;
				}
				if (playing != null) playing.close();
				playing = clip;
				Clip active = clip;
				clip.addLineListener(event -> {
					if (event.getType() == LineEvent.Type.STOP) active.close();
				});
				clip.start();
			}
		}
		catch (Exception ex)
		{
			if (clip != null) clip.close();
			log.warn("Could not play milestone sound {}", resource, ex);
		}
	}
}
