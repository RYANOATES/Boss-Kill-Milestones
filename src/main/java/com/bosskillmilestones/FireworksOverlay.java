package com.bosskillmilestones;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
final class FireworksOverlay extends Overlay
{
	private static final long DISPLAY_NANOS = 3_400_000_000L;
	private static final Color[] COLORS = {
		new Color(255, 205, 85), new Color(255, 93, 72), new Color(115, 220, 255),
		new Color(255, 125, 210), new Color(175, 255, 125), new Color(205, 155, 255)
	};

	private final Client client;
	private final List<Spark> sparks = new ArrayList<>();

	@Inject
	FireworksOverlay(Client client)
	{
		this.client = client;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(PRIORITY_HIGH);
	}

	synchronized void launch(Celebration celebration)
	{
		int width = client.getCanvasWidth();
		int height = client.getCanvasHeight();
		if (width <= 0 || height <= 0)
		{
			return;
		}

		int bursts = burstCount(celebration);
		long now = System.nanoTime();
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int burst = 0; burst < bursts; burst++)
		{
			// Spread the bursts across the visible scene and vary their height.
			double sceneFraction = (burst + 0.5) / bursts;
			int centerX = (int) (width * (0.12 + 0.76 * sceneFraction))
				+ random.nextInt(-Math.max(1, width / 24), Math.max(2, width / 24));
			int centerY = (int) (height * random.nextDouble(0.18, 0.52));
			int count = sparksPerBurst(celebration);
			for (int i = 0; i < count; i++)
			{
				double angle = Math.PI * 2 * i / count + random.nextDouble(-0.045, 0.045);
				double speed = random.nextDouble(35, 100);
				long delay = burst * 140_000_000L;
				sparks.add(new Spark(centerX, centerY, Math.cos(angle) * speed,
					Math.sin(angle) * speed, now + delay, random.nextInt(3, 6), COLORS[random.nextInt(COLORS.length)]));
			}
		}
		// Guard against a rapid sequence of milestones retaining too many particles.
		if (sparks.size() > 1600)
		{
			sparks.subList(0, sparks.size() - 1600).clear();
		}
	}

	static int burstCount(Celebration celebration)
	{
		switch (celebration)
		{
			case TEN:
		case SESSION:
			return 2;
			case TWENTY_FIVE:
			return 3;
			case FIFTY:
			return 5;
			case GOAL:
		case TWO_FIFTY:
			return 8;
			case THOUSAND:
			return 12;
			default:
			return 1;
		}
	}

	private static int sparksPerBurst(Celebration celebration)
	{
		switch (celebration)
		{
			case TEN:
		case SESSION:
			return 18;
			case TWENTY_FIVE:
			return 24;
			case FIFTY:
			return 28;
			case GOAL:
		case TWO_FIFTY:
			return 34;
			case THOUSAND:
			return 42;
			default:
			return 16;
		}
	}

	@Override
	public synchronized Dimension render(Graphics2D graphics)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			sparks.clear();
			return null;
		}

		long now = System.nanoTime();
		Iterator<Spark> iterator = sparks.iterator();
		while (iterator.hasNext())
		{
			Spark spark = iterator.next();
			long ageNanos = now - spark.started;
			if (ageNanos < 0)
			{
				continue;
			}
			if (ageNanos >= DISPLAY_NANOS)
			{
				iterator.remove();
				continue;
			}

			double seconds = ageNanos / 1_000_000_000.0;
			double x = spark.x + spark.velocityX * seconds;
			double y = spark.y + spark.velocityY * seconds + 24 * seconds * seconds;
			float fade = (float) (1.0 - seconds / (DISPLAY_NANOS / 1_000_000_000.0));
			int alpha = Math.max(0, Math.min(255, (int) (fade * 255)));
			Color color = new Color(spark.color.getRed(), spark.color.getGreen(), spark.color.getBlue(), alpha);
			graphics.setColor(color);
			graphics.fillOval((int) x - spark.size / 2, (int) y - spark.size / 2, spark.size, spark.size);
			if (spark.size >= 4)
			{
				graphics.setColor(new Color(255, 255, 235, alpha));
				graphics.fillOval((int) x - 1, (int) y - 1, 2, 2);
			}
		}
		return null;
	}

	private static final class Spark
	{
		private final int x;
		private final int y;
		private final double velocityX;
		private final double velocityY;
		private final long started;
		private final int size;
		private final Color color;

		private Spark(int x, int y, double velocityX, double velocityY, long started, int size, Color color)
		{
			this.x = x;
			this.y = y;
			this.velocityX = velocityX;
			this.velocityY = velocityY;
			this.started = started;
			this.size = size;
			this.color = color;
		}
	}
}
