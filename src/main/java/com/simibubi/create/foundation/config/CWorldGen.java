package com.simibubi.create.foundation.config;

public class CWorldGen extends ConfigBase {

	public final ConfigBool disable = b(false, "disableWorldGen", Comments.disable);

	@Override
	public String getName() {
		return "worldgen";
	}

	private static class Comments {
		static String disable = "Prevents all worldgen added by Create from taking effect";
	}

}
