package com.bosskillmilestones;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import net.runelite.client.audio.AudioPlayer;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilestoneAudioTest
{
	@Test
	public void mapsMilestonesToRequestedClips()
	{
		assertEquals("/audio/little-victory.wav", MilestoneAudio.resourceFor(10));
		assertEquals("/audio/golden-steps.wav", MilestoneAudio.resourceFor(25));
		assertEquals("/audio/little-victory.wav", MilestoneAudio.resourceFor(50));
		assertEquals("/audio/little-victory.wav", MilestoneAudio.resourceFor(100));
		assertEquals("/audio/champion-fanfare.wav", MilestoneAudio.resourceFor(250));
		assertEquals("/audio/legendary-victory.wav", MilestoneAudio.resourceFor(1000));
		assertNull(MilestoneAudio.resourceFor(0));
	}

	@Test
	public void bundledSoundsAreStandardPcmWav() throws Exception
	{
		for (Celebration celebration : Celebration.values())
		{
			try (InputStream resource = MilestoneAudio.class.getResourceAsStream(celebration.resource))
			{
				assertNotNull(resource);
				byte[] bytes = resource.readAllBytes();
				ByteBuffer header = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
				assertEquals("RIFF", new String(bytes, 0, 4, StandardCharsets.US_ASCII));
				assertEquals("WAVE", new String(bytes, 8, 4, StandardCharsets.US_ASCII));
				assertEquals("fmt ", new String(bytes, 12, 4, StandardCharsets.US_ASCII));
				assertEquals(16, header.getInt(16));
				assertEquals(1, header.getShort(20)); // PCM, no codec required
				assertEquals(2, header.getShort(22));
				assertEquals(44100, header.getInt(24));
				assertEquals(16, header.getShort(34));
				assertEquals("data", new String(bytes, 36, 4, StandardCharsets.US_ASCII));
				assertEquals(bytes.length - 44, header.getInt(40));
				assertTrue(header.getInt(40) > 0);
				assertTrue((double) header.getInt(40) / header.getInt(28) < 6);
			}
		}
	}

	@Test public void delegatesPlaybackAndIgnoresRequestsWhileStopped() throws Exception
	{
		LinkedBlockingQueue<String> requests = new LinkedBlockingQueue<>();
		AudioPlayer player = new AudioPlayer()
		{
			@Override public void play(Class<?> owner, String path, float gain)
			{
				requests.add(owner.getName() + ":" + path + ":" + gain);
			}
		};
		MilestoneAudio audio = new MilestoneAudio(player);
		try
		{
			audio.play(Celebration.TEN);
			assertTrue(requests.isEmpty());
			audio.start();
			audio.play(Celebration.GOAL);
			assertEquals(MilestoneAudio.class.getName() + ":" + Celebration.GOAL.resource + ":0.0",
				requests.poll(3, TimeUnit.SECONDS));
			audio.stop();
			audio.play(Celebration.THOUSAND);
			assertTrue(requests.isEmpty());
		}
		finally { audio.stop(); }
	}

	@Test public void legacyClipsAreNotPackaged()
	{
		assertNull(MilestoneAudio.class.getResource("/audio/birthday-party-horn.wav"));
		assertNull(MilestoneAudio.class.getResource("/audio/community-claps.wav"));
		assertNull(MilestoneAudio.class.getResource("/audio/wow-thats-amazing.wav"));
	}
}
