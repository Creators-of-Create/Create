package com.simibubi.create.content.logistics.block.belts.tunnel;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.jozufozu.flywheel.backend.instancing.IInstanceRendered;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock.Shape;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.packet.TunnelFlapPacket;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BeltTunnelTileEntity extends SmartTileEntity implements IInstanceRendered {

	public Map<Direction, InterpolatedChasingValue> flaps;
	public Set<Direction> sides;

	protected LazyOptional<IItemHandler> cap = LazyOptional.empty();
	protected List<Pair<Direction, Boolean>> flapsToSend;

	public BeltTunnelTileEntity(TileEntityType<? extends BeltTunnelTileEntity> type) {
		super(type);
		flaps = new EnumMap<>(Direction.class);
		sides = new HashSet<>();
		flapsToSend = new LinkedList<>();
	}

	@Override
	public void remove() {
		super.remove();
		cap.invalidate();
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		ListNBT flapsNBT = new ListNBT();
		for (Direction direction : flaps.keySet())
			flapsNBT.add(IntNBT.of(direction.getIndex()));
		compound.put("Flaps", flapsNBT);

		ListNBT sidesNBT = new ListNBT();
		for (Direction direction : sides)
			sidesNBT.add(IntNBT.of(direction.getIndex()));
		compound.put("Sides", sidesNBT);

		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		Set<Direction> newFlaps = new HashSet<>(6);
		ListNBT flapsNBT = compound.getList("Flaps", NBT.TAG_INT);
		for (INBT inbt : flapsNBT)
			if (inbt instanceof IntNBT)
				newFlaps.add(Direction.byIndex(((IntNBT) inbt).getInt()));

		sides.clear();
		ListNBT sidesNBT = compound.getList("Sides", NBT.TAG_INT);
		for (INBT inbt : sidesNBT)
			if (inbt instanceof IntNBT)
				sides.add(Direction.byIndex(((IntNBT) inbt).getInt()));

		for (Direction d : Iterate.directions)
			if (!newFlaps.contains(d))
				flaps.remove(d);
			else if (!flaps.containsKey(d))
				flaps.put(d, new InterpolatedChasingValue().start(.25f)
					.target(0)
					.withSpeed(.05f));

		// Backwards compat
		if (!compound.contains("Sides") && compound.contains("Flaps"))
			sides.addAll(flaps.keySet());
		super.fromTag(state, compound, clientPacket);
		if (clientPacket)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InstancedRenderDispatcher.enqueueUpdate(this));
	}

	public void updateTunnelConnections() {
		flaps.clear();
		sides.clear();
		BlockState tunnelState = getBlockState();
		for (Direction direction : Iterate.horizontalDirections) {
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

			sides.add(direction);

			// Flap might be occluded
			BlockState nextState = world.getBlockState(pos.offset(direction));
			if (nextState.getBlock() instanceof BeltTunnelBlock)
				continue;
			if (nextState.getBlock() instanceof BeltFunnelBlock)
				if (nextState.get(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED
					&& nextState.get(BeltFunnelBlock.HORIZONTAL_FACING) == direction.getOpposite())
					continue;

			flaps.put(direction, new InterpolatedChasingValue().start(.25f)
															   .target(0)
															   .withSpeed(.05f));
		}
		sendData();
	}

	public void flap(Direction side, boolean inward) {
		if (world.isRemote) {
			if (flaps.containsKey(side))
				flaps.get(side)
					.set(inward ^ side.getAxis() == Axis.Z ? -1 : 1);
			return;
		}

		flapsToSend.add(Pair.of(side, inward));
	}

	@Override
	public void initialize() {
		super.initialize();
		updateTunnelConnections();
	}

	@Override
	public void tick() {
		super.tick();
		if (!world.isRemote) {
			if (!flapsToSend.isEmpty())
				sendFlaps();
			return;
		}
		flaps.forEach((d, value) -> value.tick());
	}

	private void sendFlaps() {
		AllPackets.channel.send(packetTarget(), new TunnelFlapPacket(this, flapsToSend));

		flapsToSend.clear();
	}

	@Override
	public boolean shouldRenderNormally() {
		return true;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
		if (capability != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return super.getCapability(capability, side);

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
}
