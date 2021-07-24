package com.simibubi.create.content.logistics.block.redstone;

import java.util.List;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour.InterfaceProvider;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class ContentObserverTileEntity extends SmartTileEntity {

	private static final int DEFAULT_DELAY = 6;
	private FilteringBehaviour filtering;
	private InvManipulationBehaviour observedInventory;
	public int turnOffTicks = 0;

	public ContentObserverTileEntity(TileEntityType<? extends ContentObserverTileEntity> type) {
		super(type);
		setLazyTickRate(20);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		filtering = new FilteringBehaviour(this, new FilteredDetectorFilterSlot()).moveText(new Vector3d(0, 5, 0));
		behaviours.add(filtering);

		observedInventory = new InvManipulationBehaviour(this, InterfaceProvider.towardBlockFacing()).bypassSidedness();
		behaviours.add(observedInventory);
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
		
		if (!observedInventory.simulate()
			.extract()
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
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putInt("TurnOff", turnOffTicks);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		turnOffTicks = compound.getInt("TurnOff");
	}

}
