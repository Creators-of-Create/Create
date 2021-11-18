package com.simibubi.create.foundation.config;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.simibubi.create.foundation.worldgen.AllWorldFeatures;

public class CWorldGen extends ConfigBase {

	public final ConfigBool disable = b(false, "disableWorldGen", Comments.disable);

	@Override
	protected void registerAll(ConfigSpec builder) {
		super.registerAll(builder);
		AllWorldFeatures.fillConfig(builder);
	}

	@Override
	public String getName() {
		return "worldgen.v" + AllWorldFeatures.forcedUpdateVersion;
	}

	private static class Comments {
		static String disable = "Prevents all worldgen added by Create from taking effect";
	}

}
