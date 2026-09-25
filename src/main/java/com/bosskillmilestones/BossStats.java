package com.bosskillmilestones;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.client.util.Text;

final class BossStats
{
	private static final Pattern COUNT = Pattern.compile(
		"(?im)(?:^|\\n)[ \\t]*(?:kill\\s*count|kills|completion count|completions|success count|subdued)[ \\t]*:?[ \\t]*(?:\\n[ \\t]*)?([0-9]+|[0-9]{1,3}(?:,[0-9]{3})+)[ \\t]*(?=$|\\n)");

	/** Only accept explicit totals; achievement progress and times are not kill counts. */
	static Integer parse(String text)
	{
		if (text == null) return null;
		String plain = Text.removeTags(text.replaceAll("(?i)<br\\s*/?>", "\n"))
			.replace('\u00a0', ' ').replace("\r\n", "\n");
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
