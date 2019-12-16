package com.simibubi.create.modules.contraptions.relays.belt;

import java.util.HashMap;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTunnelBlock.Shape;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BeltTunnelTileEntity extends SyncedTileEntity implements ITickableTileEntity {

	public HashMap<Direction, InterpolatedChasingValue> flaps;
	private LazyOptional<IItemHandler> cap = LazyOptional.empty();
	private boolean initialize;

	private Direction flapToSend;
	private boolean flapInward;

	public BeltTunnelTileEntity() {
		super(AllTileEntities.BELT_TUNNEL.type);
		flaps = new HashMap<>();
		initialize = true;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {

		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (!this.cap.isPresent()) {
				if (AllBlocks.BELT.typeOf(world.getBlockState(pos.down()))) {
					TileEntity teBelow = world.getTileEntity(pos.down());
					if (teBelow != null) {
						T capBelow = teBelow.getCapability(capability, Direction.UP).orElse(null);
						if (capBelow != null) {
							cap = LazyOptional.of(() -> capBelow).cast();
						}
					}
				}
			}
			return this.cap.cast();
		}
		return super.getCapability(capability, side);
	}

	@Override
	public void remove() {
		super.remove();
		cap.invalidate();
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT tag) {
		CompoundNBT writeToClient = super.writeToClient(tag);
		if (flapToSend != null) {
			writeToClient.putInt("Flap", flapToSend.getIndex());
			writeToClient.putBoolean("FlapInward", flapInward);
			flapToSend = null;
		}
		return writeToClient;
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		super.readClientUpdate(tag);
		if (tag.contains("Flap")) {
			Direction side = Direction.byIndex(tag.getInt("Flap"));
			flap(side, tag.getBoolean("FlapInward"));
		} else
			initFlaps();
	}

	public void initFlaps() {
		if (!world.isRemote) {
			sendData();
			return;
		}

		initialize = false;
		flaps.clear();
		BlockState tunnelState = getBlockState();
		for (Direction direction : Direction.values()) {
			if (direction.getAxis().isVertical())
				continue;
			if (AllBlocks.BELT_TUNNEL.typeOf(world.getBlockState(pos.offset(direction))))
				continue;
			if (direction.getAxis() != tunnelState.get(BlockStateProperties.HORIZONTAL_AXIS)) {
				boolean positive = direction.getAxisDirection() == AxisDirection.POSITIVE
						^ direction.getAxis() == Axis.Z;
				Shape shape = tunnelState.get(BeltTunnelBlock.SHAPE);
				if (BeltTunnelBlock.isStraight(tunnelState))
					continue;
				if (positive && shape == Shape.T_LEFT)
					continue;
				if (!positive && shape == Shape.T_RIGHT)
					continue;
			}
			flaps.put(direction, new InterpolatedChasingValue().target(0).withSpeed(.05f));
		}
	}

	public void flap(Direction side, boolean inward) {
		if (world.isRemote) {
			if (flaps.containsKey(side))
				flaps.get(side).set(inward ? -1 : 1);
			return;
		}

		flapToSend = side;
		flapInward = inward;
		sendData();
	}

	@Override
	public void tick() {
		if (!world.isRemote)
			return;
		if (initialize)
			initFlaps();
		flaps.forEach((d, value) -> value.tick());
	}

}
