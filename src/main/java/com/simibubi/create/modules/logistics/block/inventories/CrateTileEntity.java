package com.simibubi.create.modules.logistics.block.inventories;

import java.util.List;

import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;

public abstract class CrateTileEntity extends SmartTileEntity {

	public CrateTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	public boolean isDoubleCrate() {
		return getBlockState().get(FlexcrateBlock.DOUBLE);
	}

	public boolean isSecondaryCrate() {
		if (!hasWorld())
			return false;
		if (!(getBlockState().getBlock() instanceof CrateBlock))
			return false;
		return isDoubleCrate() && getFacing().getAxisDirection() == AxisDirection.NEGATIVE;
	}
	
	public Direction getFacing() {
		return getBlockState().get(FlexcrateBlock.FACING);
	}

}
