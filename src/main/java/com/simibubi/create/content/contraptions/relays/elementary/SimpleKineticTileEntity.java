package com.simibubi.create.content.contraptions.relays.elementary;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;

public class SimpleKineticTileEntity extends KineticTileEntity {

	public SimpleKineticTileEntity(TileEntityType<? extends SimpleKineticTileEntity> type) {
		super(type);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos).grow(1);
	}
	
}
