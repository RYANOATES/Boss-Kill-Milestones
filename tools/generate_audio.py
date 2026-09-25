"""Development-only synthesis of original plugin audio; no external samples.
Run manually with Python 3. Never executed by the plugin.
"""
import math
import random
import struct
import wave
from pathlib import Path

RATE = 44100
OUT = Path(__file__).resolve().parents[1] / 'src/main/resources/audio'
OUT.mkdir(parents=True, exist_ok=True)


def render(filename, seconds, notes, drums=()):
    channels = [[0.0] * int(seconds * RATE) for _ in range(2)]
    for start, length, midi, gain, tone, pan in notes:
        freq = 440 * 2 ** ((midi - 69) / 12)
        for i in range(int((length + 0.24) * RATE)):
            t = i / RATE
            if t < length:
                env = min(1, t / 0.018) * (0.75 + 0.25 * math.exp(-t * 12))
            else:
                env = 0.75 * max(0, 1 - (t - length) / 0.24) ** 2
            phase = 2 * math.pi * freq * t
            if tone == 'brass':
                val = sum(math.sin(phase * k) / k ** 1.5 for k in range(1, 7)) / 1.8
            else:
                val = (math.sin(phase) + 0.28 * math.sin(phase * 2.003) * math.exp(-t * 8)
                       + 0.12 * math.sin(phase * 3.99) * math.exp(-t * 15)) * math.exp(-t * 2)
            frame = int(start * RATE) + i
            if frame >= len(channels[0]):
                break
            for channel, weight in enumerate((math.sqrt((1 - pan) / 2), math.sqrt((1 + pan) / 2))):
                channels[channel][frame] += val * env * gain * weight
    rng = random.Random(2409)
    for start in drums:
        for i in range(int(0.15 * RATE)):
            t = i / RATE
            frame = int(start * RATE) + i
            val = (rng.uniform(-1, 1) * 0.14 + math.sin(2 * math.pi * 110 * t) * 0.18) * math.exp(-t * 35)
            if frame < len(channels[0]):
                for channel in channels:
                    channel[frame] += val
    # Quiet stereo echoes give the short musical cues space without a long tail.
    dry = [channel[:] for channel in channels]
    for delay, gain in ((0.075, 0.10), (0.14, 0.07), (0.23, 0.04)):
        offset = int(delay * RATE)
        for c in range(2):
            for i in range(offset, len(channels[c])):
                channels[c][i] += dry[1-c][i-offset] * gain
    peak = max(abs(v) for channel in channels for v in channel)
    scale = 0.65 / max(peak, 0.001)
    data = bytearray()
    for i in range(len(channels[0])):
        fade = min(1, i / 300, (len(channels[0]) - i - 1) / 1800)
        for channel in channels:
            data.extend(struct.pack('<h', round(channel[i] * scale * fade * 32767)))
    with wave.open(str(OUT / filename), 'wb') as output:
        output.setnchannels(2)
        output.setsampwidth(2)
        output.setframerate(RATE)
        output.writeframes(data)
    with wave.open(str(OUT / filename)) as check:
        assert check.getnframes() == int(seconds * RATE)
        assert check.getnchannels() == 2
    print(f'{filename}: {seconds:.1f}s, stereo PCM WAV, peak below -3 dBFS')


render('little-victory.wav', 1.7, [
    (0.08, 0.16, 67, 0.7, 'brass', -0.15),
    (0.28, 0.16, 72, 0.7, 'brass', 0.15),
    (0.50, 0.53, 76, 0.7, 'brass', 0),
    (0.50, 0.53, 60, 0.35, 'brass', -0.3),
])

render('golden-steps.wav', 2.6, [
    (0.08 + i * 0.16, 0.30, pitch, 0.65, 'bell', (-0.3 if i % 2 else 0.3))
    for i, pitch in enumerate((72, 76, 79, 83, 79, 84))
] + [(1.20, 0.65, pitch, 0.4, 'bell', pan) for pitch, pan in ((60, -0.3), (67, 0.3), (76, 0))],
       (0.08, 0.40, 0.72, 1.20))

render('champion-fanfare.wav', 3.8, [
    (start, length, pitch, 0.6, 'brass', 0)
    for start, length, pitch in ((0.1, 0.18, 67), (0.34, 0.18, 67), (0.62, 0.30, 72),
                                  (1.04, 0.25, 76), (1.39, 0.30, 74), (1.82, 0.90, 79))
] + [(1.82, 0.95, pitch, 0.30, 'brass', pan) for pitch, pan in ((48, -0.4), (60, 0.4), (64, -0.2), (72, 0.2))]
  + [(2.05 + i * 0.12, 0.35, pitch, 0.2, 'bell', 0.3) for i, pitch in enumerate((84, 88, 91))],
       (0.10, 0.34, 0.62, 1.04, 1.39, 1.82))

# Larger rewards extend the same original major-key musical vocabulary.
render('legendary-victory.wav', 5.3, [
    (0.08 + i * 0.17, 0.25, pitch, 0.45, 'bell', (-0.35 if i % 2 else 0.35))
    for i, pitch in enumerate((60, 64, 67, 72, 76, 79, 84))
] + [
    (start, length, pitch, 0.62, 'brass', 0)
    for start, length, pitch in ((1.38, 0.18, 79), (1.62, 0.18, 79), (1.93, 0.32, 81),
                                (2.34, 0.32, 83), (2.80, 1.30, 84))
] + [(2.80, 1.30, pitch, 0.28, 'brass', pan)
     for pitch, pan in ((48, -0.4), (55, 0.4), (64, -0.2), (67, 0.2))]
  + [(3.12 + i * 0.14, 0.40, pitch, 0.20, 'bell', 0.25)
     for i, pitch in enumerate((88, 91, 96))],
       (0.08, 0.42, 0.76, 1.38, 1.62, 1.93, 2.34, 2.80))

render('goal-complete.wav', 3.6, [
    (start, length, pitch, 0.60, 'bell', pan)
    for start, length, pitch, pan in ((0.08, 0.35, 76, -0.25), (0.30, 0.35, 79, 0.25),
                                    (0.54, 0.40, 84, -0.25), (0.92, 0.30, 83, 0.25),
                                    (1.25, 0.95, 84, 0))
] + [(1.25, 1.0, pitch, 0.3, 'brass', pan) for pitch, pan in ((53, -0.3), (60, 0.3), (69, 0))]
  + [(2.0 + i * 0.12, 0.32, pitch, 0.22, 'bell', 0.2)
     for i, pitch in enumerate((88, 91, 96))], (0.08, 0.54, 1.25))

render('session-spark.wav', 0.95, [
    (0.04, 0.13, 79, 0.50, 'bell', -0.15),
    (0.20, 0.22, 84, 0.50, 'bell', 0.15),
])

