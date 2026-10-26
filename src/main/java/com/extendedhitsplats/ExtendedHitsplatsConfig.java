package com.extendedhitsplats;

import net.runelite.client.config.*;

@ConfigGroup("extendedhitsplats")
public interface ExtendedHitsplatsConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "extrasPerSide",
			name = "Extras Per Side",
			description = "How many extra hitsplats fit in each row of the left and right halves. Each tick's extras use the opposite half from the tick before."
	)
	@Range(min = 1, max = 5)
	default int extrasPerSide()
	{
		return 2;
	}
}
