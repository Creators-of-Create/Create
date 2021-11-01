package com.simibubi.create.content.logistics.block.redstone;

import java.util.List;

import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.contraptions.fluids.PipeConnection.Flow;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.CapManipulationBehaviourBase.InterfaceProvider;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.TankManipulationBehaviour;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class ContentObserverTileEntity extends SmartTileEntity {

	private static final int DEFAULT_DELAY = 6;
	private FilteringBehaviour filtering;
	private InvManipulationBehaviour observedInventory;
	private TankManipulationBehaviour observedTank;
	public int turnOffTicks = 0;

	public ContentObserverTileEntity(BlockEntityType<? extends ContentObserverTileEntity> type) {
		super(type);
		setLazyTickRate(20);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		filtering = new FilteringBehaviour(this, new FilteredDetectorFilterSlot()).moveText(new Vec3(0, 5, 0));
		behaviours.add(filtering);

		InterfaceProvider towardBlockFacing = InterfaceProvider.towardBlockFacing();
		behaviours.add(observedInventory = new InvManipulationBehaviour(this, towardBlockFacing).bypassSidedness());
		behaviours.add(observedTank = new TankManipulationBehaviour(this, towardBlockFacing).bypassSidedness());
	}

	@Override
	public void tick() {
		super.tick();
		BlockState state = getBlockState();
		if (turnOffTicks > 0) {
			turnOffTicks--;
			if (turnOffTicks == 0)
				level.getBlockTicks()
					.scheduleTick(worldPosition, state.getBlock(), 1);
		}

		if (!isActive())
			return;

		Direction facing = state.getValue(ContentObserverBlock.FACING);
		BlockPos targetPos = worldPosition.relative(facing);

		// Detect items on belt
		TransportedItemStackHandlerBehaviour behaviour =
			TileEntityBehaviour.get(level, targetPos, TransportedItemStackHandlerBehaviour.TYPE);
		if (behaviour != null) {
			behaviour.handleCenteredProcessingOnAllItems(.45f, stack -> {
				if (!filtering.test(stack.stack) || turnOffTicks == 6)
					return TransportedResult.doNothing();
				activate();
				return TransportedResult.doNothing();
			});
			return;
		}

		// Detect fluids in pipe
		FluidTransportBehaviour fluidBehaviour =
			TileEntityBehaviour.get(level, targetPos, FluidTransportBehaviour.TYPE);
		if (fluidBehaviour != null) {
			for (Direction side : Iterate.directions) {
				Flow flow = fluidBehaviour.getFlow(side);
				if (flow == null || !flow.inbound || !flow.complete)
					continue;
				if (!filtering.test(flow.fluid))
					continue;
				activate();
				return;
			}
			return;
		}

		if (!observedInventory.simulate()
			.extract()
			.isEmpty()) {
			activate();
			return;
		}

		if (!observedTank.simulate()
			.extractAny()
			.isEmpty()) {
			activate();
			return;
		}
	}

	public void activate() {
		activate(DEFAULT_DELAY);
	}

	public void activate(int ticks) {
		BlockState state = getBlockState();
		turnOffTicks = ticks;
		if (state.getValue(ContentObserverBlock.POWERED))
			return;
		level.setBlockAndUpdate(worldPosition, state.setValue(ContentObserverBlock.POWERED, true));
		level.updateNeighborsAt(worldPosition, state.getBlock());
	}

	private boolean isActive() {
		return true;
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("TurnOff", turnOffTicks);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		turnOffTicks = compound.getInt("TurnOff");
	}

}
