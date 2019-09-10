package com.simibubi.create;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class CreateClientConfig {

	public static final ForgeConfigSpec specification;
	public static final CreateClientConfig instance;

	static {
		final Pair<CreateClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
				.configure(CreateClientConfig::new);
		
		specification = specPair.getRight();
		instance = specPair.getLeft();
	}

	CreateClientConfig(final ForgeConfigSpec.Builder builder) {
		builder.comment("Client-only settings").push("client");
		
		builder.pop();
	}

}
