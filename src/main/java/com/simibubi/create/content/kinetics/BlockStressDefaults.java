package com.simibubi.create.content.kinetics;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.createmod.catnip.utility.Couple;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class BlockStressDefaults {

	/**
	 * Increment this number if all stress entries should be forced to update in the
	 * next release. Worlds from the previous version will overwrite potentially
	 * changed values with the new defaults.
	 */
	public static final int FORCED_UPDATE_VERSION = 2;

	public static final Map<ResourceLocation, Double> DEFAULT_IMPACTS = new HashMap<>();
	public static final Map<ResourceLocation, Double> DEFAULT_CAPACITIES = new HashMap<>();
	public static final Map<ResourceLocation, Supplier<Couple<Integer>>> GENERATOR_SPEEDS = new HashMap<>();

	public static void setDefaultImpact(ResourceLocation blockId, double impact) {
		DEFAULT_IMPACTS.put(blockId, impact);
	}

	public static void setDefaultCapacity(ResourceLocation blockId, double capacity) {
		DEFAULT_CAPACITIES.put(blockId, capacity);
	}

	public static void setGeneratorSpeed(ResourceLocation blockId, Supplier<Couple<Integer>> provider) {
		GENERATOR_SPEEDS.put(blockId, provider);
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setNoImpact() {
		return setImpact(0);
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setImpact(double impact) {
		return b -> {
			setDefaultImpact(new ResourceLocation(b.getOwner()
				.getModid(), b.getName()), impact);
			return b;
		};
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setCapacity(double capacity) {
		return b -> {
			setDefaultCapacity(new ResourceLocation(b.getOwner()
				.getModid(), b.getName()), capacity);
			return b;
		};
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setGeneratorSpeed(
		Supplier<Couple<Integer>> provider) {
		return b -> {
			setGeneratorSpeed(new ResourceLocation(b.getOwner()
				.getModid(), b.getName()), provider);
			return b;
		};
	}

}
