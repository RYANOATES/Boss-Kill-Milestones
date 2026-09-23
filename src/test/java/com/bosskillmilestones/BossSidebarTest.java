package com.bosskillmilestones;

import net.runelite.api.GameState;
import org.junit.Test;
import static org.junit.Assert.*;

public class BossSidebarTest
{
	@Test public void regionLoadsPreserveExistingAccount()
	{
		assertTrue(BossSidebar.preserveDuringTransition(GameState.LOADING, "account", "account"));
		assertTrue(BossSidebar.preserveDuringTransition(GameState.LOADING, "account", null));
		assertTrue(BossSidebar.preserveDuringTransition(GameState.HOPPING, "account", "account"));
		assertTrue(BossSidebar.preserveDuringTransition(GameState.CONNECTION_LOST, "account", null));
	}

	@Test public void logoutAndAccountChangesAreNotPreserved()
	{
		assertFalse(BossSidebar.preserveDuringTransition(GameState.LOGIN_SCREEN, "account", "account"));
		assertFalse(BossSidebar.preserveDuringTransition(GameState.LOADING, "account", "other"));
		assertFalse(BossSidebar.preserveDuringTransition(GameState.LOADING, null, null));
		assertFalse(BossSidebar.preserveDuringTransition(GameState.LOGGED_IN, "account", "account"));
	}
}
