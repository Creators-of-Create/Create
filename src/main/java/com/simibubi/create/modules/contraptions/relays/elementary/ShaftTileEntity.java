package com.simibubi.create.modules.contraptions.relays.elementary;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.util.math.AxisAlignedBB;

public class ShaftTileEntity extends KineticTileEntity {

	public ShaftTileEntity() {
		super(AllTileEntities.SHAFT.type);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos).grow(1);
	}
	
}
