package com.bosskillmilestones;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.hiscore.HiscoreSkillType;

final class BossNames
{
	private static final Map<String, String> NAMES = new HashMap<>();
	static
	{
		for (HiscoreSkill skill : HiscoreSkill.values())
		{
			if (skill.getType() == HiscoreSkillType.BOSS)
			{
				NAMES.put(normalize(skill.getName()), skill.getName());
				if (skill.getName().startsWith("The "))
				{
					NAMES.put(normalize(skill.getName().substring(4)), skill.getName());
				}
			}
		}
		alias("The Nightmare", "Nightmare");
		alias("Dusk", "Grotesque Guardians");
		alias("Barrows chest", "Barrows Chests");
		alias("Barrows", "Barrows Chests");
		alias("Lunar chest", "Lunar Chests");
		alias("Theatre of Blood Entry Mode", "Theatre of Blood: Entry Mode");
		alias("Tombs of Amascut Entry Mode", "Tombs of Amascut: Entry Mode");
	}

	private static void alias(String alias, String name)
	{
		NAMES.put(normalize(alias), name);
	}

	static String known(String name)
	{
		return name == null ? null : NAMES.get(normalize(name));
	}

	static String normalize(String name)
	{
		return name.replace(':', ' ').replace('\u00a0', ' ').trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
	}

	static String key(String name)
	{
		return normalize(name).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
	}
}
