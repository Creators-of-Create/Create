package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;
import com.simibubi.create.lib.block.CustomRenderBoundingBox;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class HarvesterTileEntity extends SyncedTileEntity implements CustomRenderBoundingBox {

	private static final AABB RENDER_BOX = new AABB(0, 0, 0, 1, 1, 1);

	// For simulations such as Ponder
	private float manuallyAnimatedSpeed;

	public HarvesterTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	@Environment(EnvType.CLIENT)
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
