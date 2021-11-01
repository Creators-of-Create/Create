package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HarvesterTileEntity extends SyncedTileEntity {

	private static final AABB RENDER_BOX = new AABB(0, 0, 0, 1, 1, 1);

	// For simulations such as Ponder
	private float manuallyAnimatedSpeed;

	public HarvesterTileEntity(BlockEntityType<? extends HarvesterTileEntity> type) {
		super(type);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AABB getRenderBoundingBox() {
		return RENDER_BOX.move(worldPosition);
	}

	public float getAnimatedSpeed() {
		return manuallyAnimatedSpeed;
	}

	public void setAnimatedSpeed(float speed) {
		manuallyAnimatedSpeed = speed;
	}

}
