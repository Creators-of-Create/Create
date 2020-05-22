package com.simibubi.create.modules.contraptions.relays.elementary;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.util.math.AxisAlignedBB;

public class SimpleKineticTileEntity extends KineticTileEntity {

	public SimpleKineticTileEntity() {
		super(AllTileEntities.SIMPLE_KINETIC.type);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos).grow(1);
	}
	
}
