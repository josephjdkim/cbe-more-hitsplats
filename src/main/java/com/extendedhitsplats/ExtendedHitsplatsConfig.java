package com.extendedhitsplats;

import net.runelite.client.config.*;

@ConfigGroup("extendedhitsplats")
public interface ExtendedHitsplatsConfig extends Config
{
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
}
