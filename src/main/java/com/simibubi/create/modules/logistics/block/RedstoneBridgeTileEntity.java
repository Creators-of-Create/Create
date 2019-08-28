package com.simibubi.create.modules.logistics.block;

import static net.minecraft.state.properties.BlockStateProperties.POWERED;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.logistics.IReceiveWireless;
import com.simibubi.create.modules.logistics.ITransmitWireless;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RedstoneBridgeTileEntity extends LinkedTileEntity
		implements ITickableTileEntity, IReceiveWireless, ITransmitWireless {

	public boolean receivedSignal;
	public boolean transmittedSignal;

	public RedstoneBridgeTileEntity() {
		super(AllTileEntities.REDSTONE_BRIDGE.type);
	}

	@Override
	public boolean getSignal() {
		return transmittedSignal;
	}

	@Override
	public void setSignal(boolean powered) {
		receivedSignal = powered;
	}

	public void transmit(boolean signal) {
		transmittedSignal = signal;
		notifySignalChange();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("Transmit", transmittedSignal);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		transmittedSignal = compound.getBoolean("Transmit");
		super.read(compound);
	}

	@Override
	public void tick() {
		if (!getBlockState().get(RedstoneBridgeBlock.RECEIVER))
			return;
		if (world.isRemote)
			return;
		if (receivedSignal != getBlockState().get(POWERED)) {
			world.setBlockState(pos, getBlockState().cycle(POWERED));
			Direction attachedFace = getBlockState().get(BlockStateProperties.FACING).getOpposite();
			BlockPos attachedPos = pos.offset(attachedFace);
			world.notifyNeighbors(attachedPos, world.getBlockState(attachedPos).getBlock());
			return;
		}
	}

}
