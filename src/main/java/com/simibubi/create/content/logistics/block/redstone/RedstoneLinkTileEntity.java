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
	protected void read(CompoundNBT compound, boolean clientPacket) {
		transmitter = compound.getBoolean("Transmitter");
		super.fromTag(compound, clientPacket);
		
		receivedSignal = compound.getInt("Receive");
		receivedSignalChanged = compound.getBoolean("ReceivedChanged");
		if (world == null || world.isRemote || !link.newPosition)
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
		if (world.isRemote)
			return;
		
		BlockState blockState = getBlockState();
		if (!AllBlocks.REDSTONE_LINK.has(blockState))
			return;

		if ((getReceivedSignal() > 0) != blockState.get(POWERED)) {
			receivedSignalChanged = true;
			world.setBlockState(pos, blockState.cycle(POWERED));
		}
		
		if (receivedSignalChanged) {
			Direction attachedFace = blockState.get(RedstoneLinkBlock.FACING).getOpposite();
			BlockPos attachedPos = pos.offset(attachedFace);
			world.updateNeighbors(pos, world.getBlockState(pos).getBlock());
			world.updateNeighbors(attachedPos, world.getBlockState(attachedPos).getBlock());
		}
	}

	protected Boolean isTransmitterBlock() {
		return !getBlockState().get(RedstoneLinkBlock.RECEIVER);
	}

	public int getReceivedSignal() {
		return receivedSignal;
	}

}
