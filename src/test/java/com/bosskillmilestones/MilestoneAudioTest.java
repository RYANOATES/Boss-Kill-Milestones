package com.bosskillmilestones;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
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
	public void bundledSoundsAreDecodableWithoutAnMp3Codec() throws Exception
	{
		for (Celebration celebration : Celebration.values())
		{
			try (InputStream resource = MilestoneAudio.class.getResourceAsStream(celebration.resource))
			{
				assertNotNull(resource);
				try (AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(resource)))
				{
					assertEquals(16, audio.getFormat().getSampleSizeInBits());
					assertTrue(audio.getFrameLength() > 0);
					assertTrue(audio.getFrameLength() / audio.getFormat().getFrameRate() < 6);
				}
			}
		}
	}

	@Test public void legacyClipsAreNotPackaged()
	{
		assertNull(MilestoneAudio.class.getResource("/audio/birthday-party-horn.wav"));
		assertNull(MilestoneAudio.class.getResource("/audio/community-claps.wav"));
		assertNull(MilestoneAudio.class.getResource("/audio/wow-thats-amazing.wav"));
	}
}
