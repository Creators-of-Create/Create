package com.simibubi.create.content.kinetics.belt.behaviour;

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

/**
 * Behaviour for BlockEntities which can process items on belts or depots beneath
 * them. Currently only supports placement location 2 spaces above the belt
 * block. Example use: Mechanical Press
 */
public class BeltProcessingBehaviour extends ProcessingBehaviour {

	public static final BehaviourType<BeltProcessingBehaviour> TYPE = new BehaviourType<>();

	public BeltProcessingBehaviour(SmartBlockEntity be) {
		super(be);
	}

	public BeltProcessingBehaviour whenItemEnters(ProcessingCallback callback) {
		onItemEnter = callback;
		return this;
	}

	/**
	 * Sets the callback for the machine to process a given item, when the item is stopped for processing
	 *
	 * Executed in
	 * @see BeltProcessingBehaviour#handleHeldItem(TransportedItemStack, TransportedItemStackHandlerBehaviour)
	 *
	 * @param callback for the machine to do its processing
	 * @return this
	 */
	public BeltProcessingBehaviour whileItemHeld(ProcessingCallback callback) {
		continueProcessing = callback;
		return this;
	}

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
