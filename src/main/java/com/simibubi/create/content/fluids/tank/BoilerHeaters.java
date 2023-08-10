package com.simibubi.create.content.fluids.tank;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.utility.AttachedRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class BoilerHeaters {
	private static final AttachedRegistry<Block, Heater> BLOCK_HEATERS = new AttachedRegistry<>(ForgeRegistries.BLOCKS);
	private static final List<HeaterProvider> GLOBAL_HEATERS = new ArrayList<>();

	public static void registerHeater(ResourceLocation block, Heater heater) {
		BLOCK_HEATERS.register(block, heater);
	}

	public static void registerHeater(Block block, Heater heater) {
		BLOCK_HEATERS.register(block, heater);
	}

	public static void registerHeaterProvider(HeaterProvider provider) {
		GLOBAL_HEATERS.add(provider);
	}

	/**
	 * A return value of {@code -1} represents no heat.
	 * A return value of {@code 0} represents passive heat.
	 * All other positive values are used as the amount of active heat.
	 */
	public static float getActiveHeat(Level level, BlockPos pos, BlockState state) {
		Heater heater = BLOCK_HEATERS.get(state.getBlock());
		if (heater != null) {
			return heater.getActiveHeat(level, pos, state);
		}

		for (HeaterProvider provider : GLOBAL_HEATERS) {
			heater = provider.getHeater(level, pos, state);
			if (heater != null) {
				return heater.getActiveHeat(level, pos, state);
			}
		}

		return -1;
	}

	public static void registerDefaults() {
		registerHeater(AllBlocks.BLAZE_BURNER.get(), (level, pos, state) -> {
			HeatLevel value = state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
			if (value == HeatLevel.NONE) {
				return -1;
			}
			if (value == HeatLevel.SEETHING) {
				return 2;
			}
			if (value.isAtLeast(HeatLevel.FADING)) {
				return 1;
			}
			return 0;
		});

		registerHeaterProvider((level, pos, state) -> {
			if (AllBlockTags.PASSIVE_BOILER_HEATERS.matches(state)) {
				return (level1, pos1, state1) -> 0;
			}
			return null;
		});
	}

	public interface Heater {
		/**
		 * A return value of {@code -1} represents no heat.
		 * A return value of {@code 0} represents passive heat.
		 * All other positive values are used as the amount of active heat.
		 */
		float getActiveHeat(Level level, BlockPos pos, BlockState state);
	}

	public interface HeaterProvider {
		@Nullable
		Heater getHeater(Level level, BlockPos pos, BlockState state);
	}
}
