package com.cbemorehitsplats;

import net.runelite.client.config.*;

@ConfigGroup("cbemorehitsplats")
public interface MoreHitsplatsConfig extends Config
{
	@ConfigSection(
			position = 0,
			name = "Display",
			description = "How the extra hitsplats are laid out"
	)
	String displaySection = "display";

	@ConfigSection(
			position = 1,
			name = "Player Hitsplat",
			description = "Your own hitsplat, when the server leaves it out"
	)
	String playerHitsplatSection = "playerHitsplat";

	@ConfigSection(
			position = 2,
			name = "Total Damage Hitsplat",
			description = "Each tick's damage on a larger hitsplat below the game's four"
	)
	String totalDamageSection = "totalDamage";

	@ConfigItem(
			position = 0,
			keyName = "extrasPerSide",
			name = "Columns per side",
			description = "How many columns each half has, continuing the game's staggered hitsplat pattern above its four. Each tick's extras use the opposite half from the tick before.",
			section = displaySection
	)
	@Range(min = 1, max = 8)
	default int extrasPerSide()
	{
		return 2;
	}

	@ConfigItem(
			position = 0,
			keyName = "estimateOwnHits",
			name = "Show your missing hits",
			description = "In a crowd the server can leave your hitsplat out. When it does, your hit is worked out from your combat XP and shown under the game's left hitsplat.",
			section = playerHitsplatSection
	)
	default boolean estimateOwnHits()
	{
		return true;
	}

	@ConfigItem(
			position = 0,
			keyName = "showLargeTotal",
			name = "Display total damage",
			description = "Shows a larger hitsplat below the game's four with the damage taken that tick, when there were two or more hits. In a crowd the server leaves some hits out. On a boss with the HP bar at the top of the screen, the total comes from the HP it lost and includes them; elsewhere it can only count the hits the server sent.",
			section = totalDamageSection
	)
	default boolean showLargeTotal()
	{
		return false;
	}

	@ConfigItem(
			position = 1,
			keyName = "markUncertainTotals",
			name = "Mark unconfirmed totals",
			description = "Adds * to a total the boss HP bar didn't confirm. It only adds up the hits the server sent, so it may be missing some. Only applies when Display total damage is on.",
			section = totalDamageSection
	)
	default boolean markUncertainTotals()
	{
		return true;
	}
}
