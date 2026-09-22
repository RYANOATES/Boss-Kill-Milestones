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
		assertEquals("/audio/birthday-party-horn.wav", MilestoneAudio.resourceFor(10));
		assertEquals("/audio/community-claps.wav", MilestoneAudio.resourceFor(25));
		assertEquals("/audio/wow-thats-amazing.wav", MilestoneAudio.resourceFor(50));
		assertEquals("/audio/wow-thats-amazing.wav", MilestoneAudio.resourceFor(100));
	}

	@Test
	public void bundledSoundsAreDecodableWithoutAnMp3Codec() throws Exception
	{
		for (int milestone : new int[]{10, 25, 50})
		{
			try (InputStream resource = MilestoneAudio.class.getResourceAsStream(MilestoneAudio.resourceFor(milestone)))
			{
				assertNotNull(resource);
				try (AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(resource)))
				{
					assertEquals(16, audio.getFormat().getSampleSizeInBits());
					assertTrue(audio.getFrameLength() > 0);
				}
			}
		}
	}
}
