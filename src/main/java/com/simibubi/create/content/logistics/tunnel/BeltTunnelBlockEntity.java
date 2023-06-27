package com.simibubi.create.content.logistics.tunnel;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.tunnel.BeltTunnelBlock.Shape;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.IItemHandler;

public class BeltTunnelBlockEntity extends SmartBlockEntity {

	public Map<Direction, LerpedFloat> flaps;
	public Set<Direction> sides;

	protected LazyOptional<IItemHandler> cap = LazyOptional.empty();
	protected List<Pair<Direction, Boolean>> flapsToSend;

	public BeltTunnelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		flaps = new EnumMap<>(Direction.class);
		sides = new HashSet<>();
		flapsToSend = new LinkedList<>();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		cap.invalidate();
	}

	protected void writeFlapsAndSides(CompoundTag compound) {
		ListTag flapsNBT = new ListTag();
		for (Direction direction : flaps.keySet())
			flapsNBT.add(IntTag.valueOf(direction.get3DDataValue()));
		compound.put("Flaps", flapsNBT);

		ListTag sidesNBT = new ListTag();
		for (Direction direction : sides)
			sidesNBT.add(IntTag.valueOf(direction.get3DDataValue()));
		compound.put("Sides", sidesNBT);
	}

	@Override
	public void writeSafe(CompoundTag tag) {
		writeFlapsAndSides(tag);
		super.writeSafe(tag);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		writeFlapsAndSides(compound);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		Set<Direction> newFlaps = new HashSet<>(6);
		ListTag flapsNBT = compound.getList("Flaps", Tag.TAG_INT);
		for (Tag inbt : flapsNBT)
			if (inbt instanceof IntTag)
				newFlaps.add(Direction.from3DDataValue(((IntTag) inbt).getAsInt()));

		sides.clear();
		ListTag sidesNBT = compound.getList("Sides", Tag.TAG_INT);
		for (Tag inbt : sidesNBT)
			if (inbt instanceof IntTag)
				sides.add(Direction.from3DDataValue(((IntTag) inbt).getAsInt()));

		for (Direction d : Iterate.directions)
			if (!newFlaps.contains(d))
				flaps.remove(d);
			else if (!flaps.containsKey(d))
				flaps.put(d, createChasingFlap());

		// Backwards compat
		if (!compound.contains("Sides") && compound.contains("Flaps"))
			sides.addAll(flaps.keySet());
		super.read(compound, clientPacket);
		if (clientPacket)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InstancedRenderDispatcher.enqueueUpdate(this));
	}

	private LerpedFloat createChasingFlap() {
		return LerpedFloat.linear()
			.startWithValue(.25f)
			.chase(0, .05f, Chaser.EXP);
	}

	public void updateTunnelConnections() {
		flaps.clear();
		sides.clear();
		BlockState tunnelState = getBlockState();
		for (Direction direction : Iterate.horizontalDirections) {
			if (direction.getAxis() != tunnelState.getValue(BlockStateProperties.HORIZONTAL_AXIS)) {
				boolean positive =
					direction.getAxisDirection() == AxisDirection.POSITIVE ^ direction.getAxis() == Axis.Z;
				Shape shape = tunnelState.getValue(BeltTunnelBlock.SHAPE);
				if (BeltTunnelBlock.isStraight(tunnelState))
					continue;
				if (positive && shape == Shape.T_LEFT)
					continue;
				if (!positive && shape == Shape.T_RIGHT)
					continue;
			}

			sides.add(direction);

			// Flap might be occluded
			BlockState nextState = level.getBlockState(worldPosition.relative(direction));
			if (nextState.getBlock() instanceof BeltTunnelBlock)
				continue;
			if (nextState.getBlock() instanceof BeltFunnelBlock)
				if (nextState.getValue(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED
					&& nextState.getValue(BeltFunnelBlock.HORIZONTAL_FACING) == direction.getOpposite())
					continue;

			flaps.put(direction, createChasingFlap());
		}
		sendData();
	}

	public void flap(Direction side, boolean inward) {
		if (level.isClientSide) {
			if (flaps.containsKey(side))
				flaps.get(side)
					.setValue(inward ^ side.getAxis() == Axis.Z ? -1 : 1);
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
		if (!level.isClientSide) {
			if (!flapsToSend.isEmpty())
				sendFlaps();
			return;
		}
		flaps.forEach((d, value) -> value.tickChaser());
	}

	private void sendFlaps() {
		AllPackets.getChannel().send(packetTarget(), new TunnelFlapPacket(this, flapsToSend));

		flapsToSend.clear();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
		if (capability != ForgeCapabilities.ITEM_HANDLER)
			return super.getCapability(capability, side);

		if (!this.cap.isPresent()) {
			if (AllBlocks.BELT.has(level.getBlockState(worldPosition.below()))) {
				BlockEntity teBelow = level.getBlockEntity(worldPosition.below());
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
