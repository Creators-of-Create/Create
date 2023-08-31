package com.simibubi.create.content.decoration.encasing;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class EncasingRegistry {
	private static final Map<Block, List<Block>> ENCASED_VARIANTS = new HashMap<>();

	/**
	 * <strong>This method must not be called before block registration is finished.</strong>
	 */
	public static <B extends Block & EncasableBlock, E extends Block & EncasedBlock, P> void addVariant(B encasable, E encased) {
		ENCASED_VARIANTS.computeIfAbsent(encasable, b -> new ArrayList<>()).add(encased);
	}

	public static List<Block> getVariants(Block block) {
		return ENCASED_VARIANTS.getOrDefault(block, Collections.emptyList());
	}

	public static <B extends Block & EncasedBlock, P, E extends Block & EncasableBlock> NonNullUnaryOperator<BlockBuilder<B, P>> addVariantTo(Supplier<E> encasable) {
		return builder -> {
			builder.onRegisterAfter(Registries.BLOCK, b -> addVariant(encasable.get(), b));
			return builder;
		};
	}
}
