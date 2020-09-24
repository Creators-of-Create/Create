package com.simibubi.create.content.contraptions.components.actors;

import java.util.List;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class PortableStorageInterfaceTileEntity extends SmartTileEntity {

	protected int transferTimeout;
	protected LazyOptional<IItemHandlerModifiable> capability;

	public PortableStorageInterfaceTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		transferTimeout = 0;
		capability = LazyOptional.empty();
	}

	public void startTransferringTo(Contraption contraption) {
		CombinedInvWrapper inventory = contraption.inventory;
		capability.invalidate();
		capability = LazyOptional.of(() -> inventory);

	}

	@Override
	public void tick() {
		super.tick();
	}
	
	public boolean isTransferring() {
		return transferTimeout != 0;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return super.getCapability(cap, side);
	}

	public void resetTimer() {

	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

}
