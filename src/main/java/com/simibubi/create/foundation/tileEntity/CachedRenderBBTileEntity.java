package com.simibubi.create.foundation.tileEntity;

import io.github.fabricators_of_create.porting_lib.block.CustomRenderBoundingBoxBlockEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class CachedRenderBBTileEntity extends SyncedTileEntity implements CustomRenderBoundingBoxBlockEntity {

	private AABB renderBoundingBox;

	public CachedRenderBBTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public AABB getRenderBoundingBox() {
		if (renderBoundingBox == null) {
			renderBoundingBox = createRenderBoundingBox();
		}
		return renderBoundingBox;
	}

	protected void invalidateRenderBoundingBox() {
		renderBoundingBox = null;
	}

	protected AABB createRenderBoundingBox() {
		return CustomRenderBoundingBoxBlockEntity.super.getRenderBoundingBox();
	}

}
