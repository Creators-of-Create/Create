package com.simibubi.create.modules.logistics.block;

import static net.minecraft.state.properties.BlockStateProperties.POWERED;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.linked.LinkBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RedstoneLinkTileEntity extends SmartTileEntity {

	private boolean receivedSignal;
	private boolean transmittedSignal;
	private LinkBehaviour link;
	private boolean transmitter;

	public RedstoneLinkTileEntity() {
		super(AllTileEntities.REDSTONE_BRIDGE.type);
	}

	public RedstoneLinkTileEntity(boolean transmitter) {
		this();
		this.transmitter = transmitter;
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

	public boolean getSignal() {
		return transmittedSignal;
	}

	public void setSignal(boolean powered) {
		receivedSignal = powered;
	}

	public void transmit(boolean signal) {
		transmittedSignal = signal;
		if (link != null)
			link.notifySignalChange();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("Transmitter", transmitter);
		compound.putBoolean("Receive", receivedSignal);
		compound.putBoolean("Transmit", transmittedSignal);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		transmitter = compound.getBoolean("Transmitter");
		super.read(compound);
		
		receivedSignal = compound.getBoolean("Receive");
		if (world == null || world.isRemote || !link.newPosition)
			transmittedSignal = compound.getBoolean("Transmit");
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
			putBehaviour(link);
		}

		if (transmitter)
			return;
		if (world.isRemote)
			return;
		BlockState blockState = getBlockState();
		if (!AllBlocks.REDSTONE_BRIDGE.typeOf(blockState))
			return;

		if (receivedSignal != blockState.get(POWERED)) {
			world.setBlockState(pos, blockState.cycle(POWERED));
			Direction attachedFace = blockState.get(RedstoneLinkBlock.FACING).getOpposite();
			BlockPos attachedPos = pos.offset(attachedFace);
			world.notifyNeighbors(attachedPos, world.getBlockState(attachedPos).getBlock());
			return;
		}
	}

	protected Boolean isTransmitterBlock() {
		return !getBlockState().get(RedstoneLinkBlock.RECEIVER);
	}

}
