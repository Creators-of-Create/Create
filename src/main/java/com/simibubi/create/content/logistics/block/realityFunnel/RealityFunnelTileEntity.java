package com.simibubi.create.content.logistics.block.realityFunnel;

import java.util.List;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InsertingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InventoryManagementBehaviour.Attachments;

import net.minecraft.tileentity.TileEntityType;

public class RealityFunnelTileEntity extends SmartTileEntity {

	private FilteringBehaviour filtering;
	private InsertingBehaviour inserting;

	public RealityFunnelTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		filtering = new FilteringBehaviour(this, new FunnelFilterSlotPositioning());
		behaviours.add(filtering);
		inserting = 
			new InsertingBehaviour(this, Attachments.toward(() -> RealityFunnelBlock.getFunnelFacing(getBlockState())));
		behaviours.add(inserting);
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return 64;
	}
	
}
