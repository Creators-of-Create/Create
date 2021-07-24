package com.simibubi.create.foundation.config;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.Create;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public class StressConfigDefaults {

	/**
	 * Increment this number if all stress entries should be forced to update in the next release.
	 * Worlds from the previous version will overwrite potentially changed values
	 * with the new defaults.
	 */
	public static final int forcedUpdateVersion = 2;

	static Map<ResourceLocation, Double> registeredDefaultImpacts = new HashMap<>();
	static Map<ResourceLocation, Double> registeredDefaultCapacities = new HashMap<>();

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setNoImpact() {
		return setImpact(0);
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setImpact(double impact) {
		return b -> {
			registeredDefaultImpacts.put(Create.asResource(b.getName()), impact);
			return b;
		};
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setCapacity(double capacity) {
		return b -> {
			registeredDefaultCapacities.put(Create.asResource(b.getName()), capacity);
			return b;
		};
	}

}
