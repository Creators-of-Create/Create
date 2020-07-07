package com.simibubi.create.content.logistics.block.belts.tunnel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock.Shape;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BeltTunnelTileEntity extends SyncedTileEntity implements ITickableTileEntity {

	public HashMap<Direction, InterpolatedChasingValue> flaps;
	public HashMap<Direction, ItemStack> syncedFlaps;
	private LazyOptional<IItemHandler> cap = LazyOptional.empty();
	private boolean initialize;

	private List<Pair<Direction, Boolean>> flapsToSend;

	public BeltTunnelTileEntity(TileEntityType<? extends BeltTunnelTileEntity> type) {
		super(type);
		flaps = new HashMap<>();
		syncedFlaps = new HashMap<>();
		initialize = true;
		flapsToSend = new LinkedList<>();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {

		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (!this.cap.isPresent()) {
				if (AllBlocks.BELT.has(world.getBlockState(pos.down()))) {
					TileEntity teBelow = world.getTileEntity(pos.down());
					if (teBelow != null) {
						T capBelow = teBelow.getCapability(capability, Direction.UP)
							.orElse(null);
						if (capBelow != null) {
							cap = LazyOptional.of(() -> capBelow)
								.cast();
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
	public CompoundNBT write(CompoundNBT compound) {
		CompoundNBT dyedFlapsNBT = new CompoundNBT();
		syncedFlaps.forEach((direction, pair) -> {
			dyedFlapsNBT.putBoolean(direction.name(), true);
		});
		compound.put("syncedFlaps", dyedFlapsNBT);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		if (compound.contains("syncedFlaps")) {
			syncedFlaps.clear();
			CompoundNBT dyedFlapsNBT = compound.getCompound("syncedFlaps");
			for (Direction direction : Direction.values()) {
				if (dyedFlapsNBT.contains(direction.name()))
					syncedFlaps.put(direction, ItemStack.EMPTY);
			}
		}
		super.read(compound);
	}

	public boolean toggleSyncForFlap(Direction face) {
		if (!flaps.containsKey(face))
			return false;
		if (syncedFlaps.containsKey(face))
			syncedFlaps.remove(face);
		else
			syncedFlaps.put(face, ItemStack.EMPTY);

		markDirty();
		sendData();
		return true;
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT tag) {
		CompoundNBT writeToClient = super.writeToClient(tag);
		if (!flapsToSend.isEmpty()) {
			ListNBT flapsNBT = new ListNBT();
			for (Pair<Direction, Boolean> pair : flapsToSend) {
				CompoundNBT flap = new CompoundNBT();
				flap.putInt("Flap", pair.getKey()
					.getIndex());
				flap.putBoolean("FlapInward", pair.getValue());
				flapsNBT.add(flap);
			}
			writeToClient.put("Flaps", flapsNBT);
			flapsToSend.clear();
		}
		return writeToClient;
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		super.readClientUpdate(tag);
		if (tag.contains("Flaps")) {
			ListNBT flapsNBT = tag.getList("Flaps", NBT.TAG_COMPOUND);
			for (INBT inbt : flapsNBT) {
				CompoundNBT flap = (CompoundNBT) inbt;
				Direction side = Direction.byIndex(flap.getInt("Flap"));
				flap(side, flap.getBoolean("FlapInward"));
			}
		} else
			initFlaps();
	}

	public void initFlaps() {
		if (!world.isRemote) {
			sendData();
		}

		initialize = false;
		flaps.clear();
		BlockState tunnelState = getBlockState();
		for (Direction direction : Direction.values()) {
			if (direction.getAxis()
				.isVertical())
				continue;
			BlockState blockState = world.getBlockState(pos.offset(direction));
			if (blockState.getBlock() instanceof BeltTunnelBlock)
				continue;
			if (direction.getAxis() != tunnelState.get(BlockStateProperties.HORIZONTAL_AXIS)) {
				boolean positive =
					direction.getAxisDirection() == AxisDirection.POSITIVE ^ direction.getAxis() == Axis.Z;
				Shape shape = tunnelState.get(BeltTunnelBlock.SHAPE);
				if (BeltTunnelBlock.isStraight(tunnelState))
					continue;
				if (positive && shape == Shape.T_LEFT)
					continue;
				if (!positive && shape == Shape.T_RIGHT)
					continue;
			}
			flaps.put(direction, new InterpolatedChasingValue().target(0)
				.withSpeed(.05f));
		}
	}

	public void flap(Direction side, boolean inward) {
		if (world.isRemote) {
			if (flaps.containsKey(side))
				flaps.get(side)
					.set(inward ? -1 : 1);
			return;
		}

		flapsToSend.add(Pair.of(side, inward));
	}

	@Override
	public void tick() {
		if (initialize)
			initFlaps();
		if (!world.isRemote) {
			if (!flapsToSend.isEmpty())
				sendData();
			return;
		}
		flaps.forEach((d, value) -> value.tick());
	}

}
