package com.simibubi.create.api.contraption.train;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllInteractionBehaviours;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import com.simibubi.create.content.processing.burner.BlockBasedTrainConductorInteractionBehaviour;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;


import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * All required methods to make your block a train conductor similar to the blaze burner
 */
public interface TrainConductorHandler {

	@ApiStatus.Internal
	List<TrainConductorHandler> CONDUCTOR_HANDLERS = new ArrayList<>();



	boolean isValidConductor(BlockState state);

	private static void registerHandler(TrainConductorHandler handler) {
		CONDUCTOR_HANDLERS.add(handler);
	}

	static void registerConductor(ResourceLocation blockRl, Predicate<BlockState> isValidConductor, UpdateScheduleCallback updateScheduleCallback) {
		AllInteractionBehaviours.registerBehaviour(blockRl, new BlockBasedTrainConductorInteractionBehaviour(isValidConductor, updateScheduleCallback));
		registerHandler(isValidConductor::test);
	}

	@ApiStatus.Internal
	static void registerBlazeBurner() {
		registerConductor(AllBlocks.BLAZE_BURNER.getId(), blockState ->  AllBlocks.BLAZE_BURNER.has(blockState)
					&& blockState.getValue(BlazeBurnerBlock.HEAT_LEVEL) != BlazeBurnerBlock.HeatLevel.NONE, UpdateScheduleCallback.EMPTY);
	}

	interface UpdateScheduleCallback {

		UpdateScheduleCallback EMPTY = (hasSchedule, blockState, blockStateSetter) -> {};

		void update(boolean hasSchedule, BlockState currentBlockState, Consumer<BlockState> blockStateSetter);
	}
}
