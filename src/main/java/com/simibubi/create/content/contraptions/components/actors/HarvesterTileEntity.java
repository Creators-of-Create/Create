package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;

public class HarvesterTileEntity extends SyncedTileEntity {

	private static final AxisAlignedBB RENDER_BOX = new AxisAlignedBB(0, 0, 0, 1, 1, 1);

	// For simulations such as Ponder
	private float manuallyAnimatedSpeed;

	public HarvesterTileEntity(TileEntityType<? extends HarvesterTileEntity> type) {
		super(type);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return RENDER_BOX.offset(pos);
	}

	public float getAnimatedSpeed() {
		return manuallyAnimatedSpeed;
	}

	public void setAnimatedSpeed(float speed) {
		manuallyAnimatedSpeed = speed;
	}

}
