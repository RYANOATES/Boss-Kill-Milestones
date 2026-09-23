package com.bosskillmilestones;

import net.runelite.api.coords.WorldPoint;

final class TestGuard
{
	static final String NAME = "Varrock guard (test)";

	static boolean matches(String name, int combatLevel, WorldPoint location)
	{
		// ServerNpcLoot has no NPC location, so limit its generic Guard composition
		// to a player on the surface in Varrock, including the city gates.
		return "Guard".equalsIgnoreCase(name) && combatLevel == 21 && location != null
			&& location.getPlane() == 0 && location.getX() >= 3170 && location.getX() <= 3290
			&& location.getY() >= 3370 && location.getY() <= 3510;
	}
}
