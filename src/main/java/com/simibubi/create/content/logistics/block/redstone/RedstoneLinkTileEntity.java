package com.simibubi.create.content.logistics.block.redstone;

import static net.minecraft.state.properties.BlockStateProperties.POWERED;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RedstoneLinkTileEntity extends SmartTileEntity {

	private boolean receivedSignalChanged;
	private int receivedSignal;
	private int transmittedSignal;
	private LinkBehaviour link;
	private boolean transmitter;

	public RedstoneLinkTileEntity(TileEntityType<? extends RedstoneLinkTileEntity> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
	}

	@Override
	public void addBehavioursDeferred(List<TileEntityBehaviour> behaviours) {
		createLink();
		behaviours.add(link);
	}

	protected void createLink() {
		Pair<ValueBoxTransform, ValueBoxTransform> slots =
			ValueBoxTransform.Dual.makeSlots(RedstoneLinkFrequencySlot::new);
		link = transmitter ? LinkBehaviour.transmitter(this, slots, this::getSignal)
				: LinkBehaviour.receiver(this, slots, this::setSignal);
	}

	public int getSignal() {
		return transmittedSignal;
	}

	public void setSignal(int power) {
		if (receivedSignal != power)
			receivedSignalChanged = true;
		receivedSignal = power;
	}

	public void transmit(int strength) {
		transmittedSignal = strength;
		if (link != null)
			link.notifySignalChange();
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putBoolean("Transmitter", transmitter);
		compound.putInt("Receive", getReceivedSignal());
		compound.putBoolean("ReceivedChanged", receivedSignalChanged);
		compound.putInt("Transmit", transmittedSignal);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		transmitter = compound.getBoolean("Transmitter");
		super.fromTag(state, compound, clientPacket);
		
		receivedSignal = compound.getInt("Receive");
		receivedSignalChanged = compound.getBoolean("ReceivedChanged");
		if (level == null || level.isClientSide || !link.newPosition)
			transmittedSignal = compound.getInt("Transmit");
	}

	@Override
	public void tick() {
		super.tick();

		if (isTransmitterBlock() != transmitter) {
			transmitter = isTransmitterBlock();
			LinkBehaviour prevlink = link;
			removeBehaviour(LinkBehaviour.TYPE);
			createLink();
			link.copyItemsFrom(prevlink);
			attachBehaviourLate(link);
		}

		if (transmitter)
			return;
		if (level.isClientSide)
			return;
		
		BlockState blockState = getBlockState();
		if (!AllBlocks.REDSTONE_LINK.has(blockState))
			return;

		if ((getReceivedSignal() > 0) != blockState.getValue(POWERED)) {
			receivedSignalChanged = true;
			level.setBlockAndUpdate(worldPosition, blockState.cycle(POWERED));
		}
		
		if (receivedSignalChanged) {
			Direction attachedFace = blockState.getValue(RedstoneLinkBlock.FACING).getOpposite();
			BlockPos attachedPos = worldPosition.relative(attachedFace);
			level.blockUpdated(worldPosition, level.getBlockState(worldPosition).getBlock());
			level.blockUpdated(attachedPos, level.getBlockState(attachedPos).getBlock());
			receivedSignalChanged = false;
		}
	}

	protected Boolean isTransmitterBlock() {
		return !getBlockState().getValue(RedstoneLinkBlock.RECEIVER);
	}

	public int getReceivedSignal() {
		return receivedSignal;
	}

}
