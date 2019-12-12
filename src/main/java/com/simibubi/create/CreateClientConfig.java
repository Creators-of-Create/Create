package com.simibubi.create;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;

public class CreateClientConfig {

	public static final ForgeConfigSpec specification;
	public static final CreateClientConfig instance;

	static {
		final Pair<CreateClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
				.configure(CreateClientConfig::new);

		specification = specPair.getRight();
		instance = specPair.getLeft();
	}

	public BooleanValue enableTooltips;
	public DoubleValue fanParticleDensity;

	CreateClientConfig(final ForgeConfigSpec.Builder builder) {
		builder.comment(
				"Client-only settings - If you're looking for server/common settings, look inside your worlds serverconfig folder!")
				.push("client");
		String basePath = "create.config.client.";

		String name = "enableTooltips";
		enableTooltips = builder.comment("", "Show item descriptions on Shift and controls on Ctrl.")
				.translation(basePath + name).define(name, true);

		name = "fanParticleDensity";
		fanParticleDensity = builder.comment("", "Controls the average amount of fan particles spawned per tick.")
				.translation(basePath + name).defineInRange(name, .5D, 0D, 1D);

		builder.pop();
	}

}
