package com.simibubi.create.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;

public class CExtras extends ConfigBase {

	public final ConfigBool enableAdvancedRegex = b(false, "enableAdvancedRegex", "[requires restart]", Comments.enableAdvancedRegex);

	@Override
	public String getName() {
		return "extras";
	}

	private static class Comments {
		static String enableAdvancedRegex = "Whether to enable the use of regex-based address matching in package logistics and train schedules.";
	}
}
