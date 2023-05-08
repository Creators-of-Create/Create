package com.simibubi.create.content.logistics.trains.track;

import com.simibubi.create.content.logistics.trains.IBogeyBlock;
import com.simibubi.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class StandardBogeyBlockEntity extends CachedRenderBBBlockEntity {

	public StandardBogeyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return super.createRenderBoundingBox().inflate(2);
	}

	// Ponder
	
	LerpedFloat virtualAnimation = LerpedFloat.angular();
	
	public float getVirtualAngle(float partialTicks) {
		return virtualAnimation.getValue(partialTicks);
	}
	
	public void animate(float distanceMoved) {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof IBogeyBlock type))
			return;
		double angleDiff = 360 * distanceMoved / (Math.PI * 2 * type.getWheelRadius());
		double newWheelAngle = (virtualAnimation.getValue() - angleDiff) % 360;
		virtualAnimation.setValue(newWheelAngle);
	}

}
