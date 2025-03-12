package com.simibubi.create.content.kinetics.belt.behaviour;

import com.simibubi.create.content.itemprocessing.ItemProcessor;
import com.simibubi.create.content.itemprocessing.specifics.ProcessingSpecifics;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.funnel.AbstractFunnelBlock;
import com.simibubi.create.content.processing.ProcessingBehaviour;
import com.simibubi.create.content.processing.ProcessingCallback;
import com.simibubi.create.content.processing.ProcessingResult;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

/**
 * Entity can process items which are running on a belt
 * Use this inside your entities for general item processing and belt processing.
 *
 * @param <T> processing specifics
 * @see ProcessingSpecifics
 */
public abstract class BeltProcessingBehaviour<T extends ProcessingSpecifics> extends ItemProcessor<T> {

	public static BehaviourType<BeltProcessingBehaviour<?>> TYPE = new BehaviourType<>("belt_processing_behaviour");

	public BeltProcessingBehaviour(int cycle, SmartBlockEntity be, T specifics) {
		super(cycle, be, specifics);
		onItemEnter = this::whenItemEnters;
		continueProcessing = this::whileItemHeld;
	}

	public abstract ProcessingResult whenItemEnters(TransportedItemStack itemStack, TransportedItemStackHandlerBehaviour handler);

	/**
	 * Called as long as the item is held on the belt.
	 * @param itemStack the stack being on the belt
	 * @param handler the belt
	 */
	public abstract ProcessingResult whileItemHeld(TransportedItemStack itemStack, TransportedItemStackHandlerBehaviour handler);


	/**
	 * Checks if a block is above the belt, Funnels are ignored from blocking the belt.
	 * A blocked means, that items are not transported in the belt direction.
	 *
	 * @param world where the belt is located
	 * @param processingSpace location of the current item process on the belt
	 * @return whether the belt is blocked or not
	 */
	public static boolean isBlocked(BlockGetter world, BlockPos processingSpace) {
		BlockState blockState = world.getBlockState(processingSpace.above());
		if (AbstractFunnelBlock.isFunnel(blockState))
			return false;
		return !blockState.getCollisionShape(world, processingSpace.above())
			.isEmpty();
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}
}
