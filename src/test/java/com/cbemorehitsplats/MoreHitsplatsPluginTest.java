package com.cbemorehitsplats;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MoreHitsplatsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MoreHitsplatsPlugin.class);
		RuneLite.main(args);
	}
}