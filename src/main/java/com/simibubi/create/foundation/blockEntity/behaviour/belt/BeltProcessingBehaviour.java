package com.simibubi.create.foundation.blockEntity.behaviour.belt;

import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.funnel.AbstractFunnelBlock;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Behaviour for BlockEntities which can process items on belts or depots beneath
 * them. Currently only supports placement location 2 spaces above the belt
 * block. Example use: Mechanical Press
 */
public class BeltProcessingBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<BeltProcessingBehaviour> TYPE = new BehaviourType<>();

	public static enum ProcessingResult {
		PASS, HOLD, REMOVE;
	}

	private ProcessingCallback onItemEnter;
	private ProcessingCallback continueProcessing;

	public BeltProcessingBehaviour(SmartBlockEntity be) {
		super(be);
		onItemEnter = (s, i) -> ProcessingResult.PASS;
		continueProcessing = (s, i) -> ProcessingResult.PASS;
	}

	public BeltProcessingBehaviour whenItemEnters(ProcessingCallback callback) {
		onItemEnter = callback;
		return this;
	}

	public BeltProcessingBehaviour whileItemHeld(ProcessingCallback callback) {
		continueProcessing = callback;
		return this;
	}

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

	public ProcessingResult handleReceivedItem(TransportedItemStack stack,
		TransportedItemStackHandlerBehaviour inventory) {
		return onItemEnter.apply(stack, inventory);
	}

	public ProcessingResult handleHeldItem(TransportedItemStack stack, TransportedItemStackHandlerBehaviour inventory) {
		return continueProcessing.apply(stack, inventory);
	}

	@FunctionalInterface
	public interface ProcessingCallback {
		public ProcessingResult apply(TransportedItemStack stack, TransportedItemStackHandlerBehaviour inventory);
	}

}
