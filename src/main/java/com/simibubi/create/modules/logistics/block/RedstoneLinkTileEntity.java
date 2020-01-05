package com.simibubi.create.modules.logistics.block;

import static net.minecraft.state.properties.BlockStateProperties.POWERED;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.linked.LinkBehaviour;
import com.simibubi.create.foundation.behaviour.linked.LinkBehaviour.SlotPositioning;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RedstoneLinkTileEntity extends SmartTileEntity {

	private static LinkBehaviour.SlotPositioning slots;
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
		if (slots == null)
			createSlotPositioning();
		createLink();
		behaviours.add(link);
	}

	protected void createLink() {
		if (transmitter)
			link = LinkBehaviour.transmitter(this, this::getSignal);
		else
			link = LinkBehaviour.receiver(this, this::setSignal);
		link.withSlotPositioning(slots);
	}

	public boolean getSignal() {
		return transmittedSignal;
	}

	public void setSignal(boolean powered) {
		receivedSignal = powered;
	}

	public void transmit(boolean signal) {
		transmittedSignal = signal;
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
		receivedSignal = compound.getBoolean("Receive");
		transmittedSignal = compound.getBoolean("Transmit");
		super.read(compound);
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
		if (receivedSignal != getBlockState().get(POWERED)) {
			world.setBlockState(pos, getBlockState().cycle(POWERED));
			Direction attachedFace = getBlockState().get(BlockStateProperties.FACING).getOpposite();
			BlockPos attachedPos = pos.offset(attachedFace);
			world.notifyNeighbors(attachedPos, world.getBlockState(attachedPos).getBlock());
			return;
		}
	}

	protected Boolean isTransmitterBlock() {
		return !getBlockState().get(RedstoneLinkBlock.RECEIVER);
	}

	protected void createSlotPositioning() {
		slots = new SlotPositioning(state -> {
			Direction facing = state.get(RedstoneLinkBlock.FACING);
			Vec3d first = Vec3d.ZERO;
			Vec3d second = Vec3d.ZERO;

			if (facing.getAxis().isHorizontal()) {
				first = VecHelper.voxelSpace(10f, 5.5f, 2.5f);
				second = VecHelper.voxelSpace(10f, 10.5f, 2.5f);

				float angle = facing.getHorizontalAngle();
				if (facing.getAxis() == Axis.X)
					angle = -angle;

				first = VecHelper.rotateCentered(first, angle, Axis.Y);
				second = VecHelper.rotateCentered(second, angle, Axis.Y);

			} else {
				first = VecHelper.voxelSpace(10f, 2.5f, 5.5f);
				second = VecHelper.voxelSpace(10f, 2.5f, 10.5f);

				if (facing == Direction.DOWN) {
					first = VecHelper.rotateCentered(first, 180, Axis.X);
					second = VecHelper.rotateCentered(second, 180, Axis.X);
				}
			}
			return Pair.of(first, second);
		}, state -> {
			Direction facing = state.get(RedstoneLinkBlock.FACING);
			float yRot = facing.getAxis().isVertical() ? 180 : AngleHelper.horizontalAngle(facing);
			float zRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
			return new Vec3d(0, yRot + 180, zRot);
		}).scale(.5f);
	}

}
