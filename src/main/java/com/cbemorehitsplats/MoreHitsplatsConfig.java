package com.cbemorehitsplats;

import net.runelite.client.config.*;

@ConfigGroup("cbemorehitsplats")
public interface MoreHitsplatsConfig extends Config
{
	@ConfigSection(
			position = 2,
			name = "Large Total",
			description = "Each tick's damage on a larger hitsplat below the game's four"
	)
	String largeTotalSection = "largeTotal";

	@ConfigItem(
			position = 0,
			keyName = "extrasPerSide",
			name = "Columns Per Side",
			description = "How many columns each half has, continuing the game's staggered hitsplat pattern above its four. Each tick's extras use the opposite half from the tick before."
	)
	@Range(min = 1, max = 8)
	default int extrasPerSide()
	{
		return 2;
	}

	@ConfigItem(
			position = 1,
			keyName = "estimateOwnHits",
			name = "Show Your Missing Hits",
			description = "In a crowd the server can leave your hitsplat out. When it does, your hit is worked out from your combat XP and shown under the game's left hitsplat."
	)
	default boolean estimateOwnHits()
	{
		return true;
	}

	@ConfigItem(
			position = 0,
			keyName = "showLargeTotal",
			name = "Show Large Total",
			description = "Shows a larger hitsplat below the game's four with the damage taken that tick, when there were two or more hits. In a crowd the server leaves some hits out. On a boss with the HP bar at the top of the screen, the total comes from the HP it lost and includes them; elsewhere it can only count the hits the server sent.",
			section = largeTotalSection
	)
	default boolean showLargeTotal()
	{
		return false;
	}

	@ConfigItem(
			position = 1,
			keyName = "markUncertainTotals",
			name = "Mark Uncertain Totals",
			description = "Adds * to a total that only adds up the hits the server sent, as it may be missing some. A total from a boss's HP bar has no *. Only applies with Show Large Total on.",
			section = largeTotalSection
	)
	default boolean markUncertainTotals()
	{
		return true;
	}
}
