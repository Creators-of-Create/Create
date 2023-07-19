package com.simibubi.create.content.kinetics.fan;

import com.google.common.collect.Lists;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.LitBlazeBurnerBlock;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

import static com.simibubi.create.content.processing.burner.BlazeBurnerBlock.getHeatLevelOf;

public class ProcessingTypeTransformerRegistry {

	private static final List<Pair<Integer, ProcessingTypeTransformer>> REG = Lists.newArrayList();
	private static List<ProcessingTypeTransformer> REG_SORTED = Collections.emptyList();

	static {
		registerProcessingTypeTransformer(0, (BlockGetter reader, BlockPos pos) -> {
			FluidState fluidState = reader.getFluidState(pos);
			if (fluidState.getType() == Fluids.WATER || fluidState.getType() == Fluids.FLOWING_WATER)
				return FanProcessing.SPLASHING;
			BlockState blockState = reader.getBlockState(pos);
			Block block = blockState.getBlock();
			if (block == Blocks.SOUL_FIRE
					|| block == Blocks.SOUL_CAMPFIRE && blockState.getOptionalValue(CampfireBlock.LIT)
					.orElse(false)
					|| AllBlocks.LIT_BLAZE_BURNER.has(blockState)
					&& blockState.getOptionalValue(LitBlazeBurnerBlock.FLAME_TYPE)
					.map(flame -> flame == LitBlazeBurnerBlock.FlameType.SOUL)
					.orElse(false))
				return FanProcessing.HAUNTING;
			if (block == Blocks.FIRE
					|| blockState.is(BlockTags.CAMPFIRES) && blockState.getOptionalValue(CampfireBlock.LIT)
					.orElse(false)
					|| AllBlocks.LIT_BLAZE_BURNER.has(blockState)
					&& blockState.getOptionalValue(LitBlazeBurnerBlock.FLAME_TYPE)
					.map(flame -> flame == LitBlazeBurnerBlock.FlameType.REGULAR)
					.orElse(false)
					|| getHeatLevelOf(blockState) == BlazeBurnerBlock.HeatLevel.SMOULDERING)
				return FanProcessing.SMOKING;
			if (block == Blocks.LAVA || getHeatLevelOf(blockState).isAtLeast(BlazeBurnerBlock.HeatLevel.FADING))
				return FanProcessing.BLASTING;
			return AbstractFanProcessingType.NONE;
		});
	}

	/**
	 * Add Air Current Transformation.
	 * @param priority Priority of this transform (smallest comes first)
	 * @param transformer Function that provides a Processing Type or {@link AbstractFanProcessingType#NONE}
	 *                    if no processing type is available
	 */

	public static void registerProcessingTypeTransformer(int priority, ProcessingTypeTransformer transformer) {
		REG.add(Pair.of(priority, transformer));
		REG_SORTED = rebuild();
	}

	public static AbstractFanProcessingType byBlock(BlockGetter reader, BlockPos pos) {
		for (ProcessingTypeTransformer ptt : REG_SORTED) {
			AbstractFanProcessingType processingType = ptt.apply(reader, pos);
			if (processingType != AbstractFanProcessingType.NONE) return processingType;
		}
		return AbstractFanProcessingType.NONE;
	}

	private static List<ProcessingTypeTransformer> rebuild() {
		return REG.stream().sorted(Comparator.comparing(Pair::getFirst)).map(Pair::getSecond).toList();
	}

	public interface ProcessingTypeTransformer extends BiFunction<BlockGetter, BlockPos, AbstractFanProcessingType> {}
}
