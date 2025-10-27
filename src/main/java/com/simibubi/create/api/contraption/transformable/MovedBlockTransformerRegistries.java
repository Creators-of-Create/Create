package com.simibubi.create.api.contraption.transformable;

import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.contraptions.StructureTransform;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Registry for custom transformations to apply to blocks after they've been moved by a contraption.
 * These interfaces are alternatives to the {@link TransformableBlock} and {@link TransformableBlockEntity} interfaces.
 */
public class MovedBlockTransformerRegistries {
	public static final SimpleRegistry<Block, BlockTransformer> BLOCK_TRANSFORMERS = SimpleRegistry.create();
	public static final SimpleRegistry<BlockEntityType<?>, BlockEntityTransformer> BLOCK_ENTITY_TRANSFORMERS = SimpleRegistry.create();

	@FunctionalInterface
	public interface BlockTransformer {
		BlockState transform(BlockState state, StructureTransform transform);
	}

	@FunctionalInterface
	public interface BlockEntityTransformer {
		void transform(BlockEntity be, StructureTransform transform);
	}

	private MovedBlockTransformerRegistries() {
		throw new AssertionError("This class should not be instantiated");
	}
}
