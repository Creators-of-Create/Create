package com.simibubi.create.infrastructure.config;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.ConfigBase;
import com.simibubi.create.infrastructure.worldgen.AllOreFeatureConfigEntries;

import net.minecraftforge.common.ForgeConfigSpec.Builder;

public class CWorldGen extends ConfigBase {

	/**
	 * Increment this number if all worldgen config entries should be overwritten
	 * in this update. Worlds from the previous version will overwrite potentially
	 * changed values with the new defaults.
	 */
	public static final int FORCED_UPDATE_VERSION = 2;

	public final ConfigBool disable = b(false, "disableWorldGen", Comments.disable);

	@Override
	public void registerAll(Builder builder) {
		super.registerAll(builder);
		AllOreFeatureConfigEntries.fillConfig(builder, Create.ID);
	}

	@Override
	public String getName() {
		return "worldgen.v" + FORCED_UPDATE_VERSION;
	}

	private static class Comments {
		static String disable = "Prevents all worldgen added by Create from taking effect";
	}

}
