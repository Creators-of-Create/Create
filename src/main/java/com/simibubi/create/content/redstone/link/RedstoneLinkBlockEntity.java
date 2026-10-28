package com.simibubi.create.content.redstone.link;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSupportBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.apache.commons.lang3.tuple.Pair;

public class RedstoneLinkBlockEntity extends SmartBlockEntity {

	private int signal;
	private LinkBehaviour link;
	private boolean transmitter;

	public FactoryPanelSupportBehaviour panelSupport;

	public RedstoneLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(panelSupport = new FactoryPanelSupportBehaviour(this, () -> link != null && link.isListening(),
			() -> signal > 0, () -> AllBlocks.REDSTONE_LINK.get()
				.updateTransmittedSignal(getBlockState(), level, worldPosition)));
	}

	@Override
	public void addBehavioursDeferred(List<BlockEntityBehaviour> behaviours) {
		createLink();
		behaviours.add(link);
	}

	protected void createLink() {
		Pair<ValueBoxTransform, ValueBoxTransform> slots =
			ValueBoxTransform.Dual.makeSlots(RedstoneLinkFrequencySlot::new);
		link = transmitter ? LinkBehaviour.transmitter(this, slots, this::getSignal)
			: LinkBehaviour.receiver(this, slots, this::getSignal, this::setSignal);
	}

	public int getSignal() {
		return signal;
	}

	public void setSignal(int power) {
		if (signal == power) return;
		signal = power;
		updateSelfAndAttached(getBlockState());
	}

	public void transmit(int strength) {
		if (signal == strength) return;
		int oldSignal = signal;
		signal = strength;
		if (link != null) link.notifySignalChange(oldSignal);
	}

	@Override
	public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		if (hasLevel() && !getLevel().isClientSide && !initialized) {
			initialized = true;
			initialize();
			recreateLink();
		}

		compound.putBoolean("Transmitter", transmitter);
		compound.putInt("Receive", getSignal());
		compound.putInt("Transmit", signal);
		super.write(compound, registries, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		transmitter = compound.getBoolean("Transmitter");
		super.read(compound, registries, clientPacket);

		if (hasLevel() && !getLevel().isClientSide && !initialized) {
			initialized = true;
			initialize();
		}

		signal = compound.getInt("Receive");
		if (level == null || level.isClientSide || !link.newPosition)
			signal = compound.getInt("Transmit");
	}

	public void recreateLink(){
		transmitter = isTransmitterBlock();
		LinkBehaviour prevlink = link;
		removeBehaviour(LinkBehaviour.TYPE);
		createLink();
		link.copyItemsFrom(prevlink);
		attachBehaviourLate(link);
	}

	@Override
	public void remove() {
		super.remove();

		updateSelfAndAttached(getBlockState());
	}

	public void updateSelfAndAttached(BlockState blockState) {
		Direction attachedFace = blockState.getValue(RedstoneLinkBlock.FACING).getOpposite();
		BlockPos attachedPos = this.worldPosition.relative(attachedFace);
		if ((getSignal() > 0) != blockState.getValue(RedstoneLinkBlock.POWERED)) {
			level.setBlockAndUpdate(worldPosition, blockState.cycle(RedstoneLinkBlock.POWERED));
		} else {
			level.blockUpdated(worldPosition, blockState.getBlock());
		}
		level.blockUpdated(attachedPos, level.getBlockState(attachedPos).getBlock());
		panelSupport.notifyPanels();
	}

	protected Boolean isTransmitterBlock() {
		return !getBlockState().getValue(RedstoneLinkBlock.RECEIVER);
	}

	/**
	 * Call {@link RedstoneLinkBlockEntity#getSignal()} instead
	 */
	@Deprecated(forRemoval = true)
	public int getReceivedSignal() {
		return signal;
	}

}
