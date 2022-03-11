package com.simibubi.create.content.logistics.block.depot;

import java.util.List;

import com.simibubi.create.content.contraptions.debrisShield.DebrisShieldHandler;
import com.simibubi.create.content.contraptions.debrisShield.IDebrisShielded;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class DepotTileEntity extends SmartTileEntity implements IDebrisShielded {

	DepotBehaviour depotBehaviour;
	private DebrisShieldHandler<DepotTileEntity> acceptDroppedItems;

	public DepotTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(depotBehaviour = new DepotBehaviour(this));
		depotBehaviour.addSubBehaviours(behaviours);

		acceptDroppedItems = new DebrisShieldHandler<>(this);
		acceptDroppedItems.addBehaviours(behaviours);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return depotBehaviour.getItemCapability(cap, side);
		return super.getCapability(cap, side);
	}

	@Override
	public DebrisShieldHandler.SelectionMode toggleShielded() {
		return acceptDroppedItems.toggle();
	}

	@Override
	public void setShielded(DebrisShieldHandler.SelectionMode lockingState) {
		acceptDroppedItems.setShielded(lockingState);
	}

	@Override
	public boolean isShielded() {
		return acceptDroppedItems.isShielded();
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		acceptDroppedItems.write(compound);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		acceptDroppedItems.read(compound);
	}
}
