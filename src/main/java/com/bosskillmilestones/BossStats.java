package com.bosskillmilestones;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.client.util.Text;

final class BossStats
{
	private static final Pattern COUNT = Pattern.compile(
		"(?im)(?:^|\\n)\\s*(?:kill\\s*count|kills|completion count|completions|success count|subdued)\\s*:?\\s*([0-9][0-9,]*)\\s*(?=$|\\n)");

	/** Only accept explicit totals; achievement progress and times are not kill counts. */
	static Integer parse(String text)
	{
		String plain = Text.removeTags(text.replace("<br>", "\n")).replace('\u00a0', ' ');
		Matcher matcher = COUNT.matcher(plain);
		if (!matcher.find())
		{
			return null;
		}
		try
		{
			int count = Integer.parseInt(matcher.group(1).replace(",", ""));
			return matcher.find() ? null : count;
		}
		catch (NumberFormatException ex)
		{
			return null;
		}
	}
}
