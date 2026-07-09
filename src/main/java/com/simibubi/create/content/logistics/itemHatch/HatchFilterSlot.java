package com.simibubi.create.content.logistics.itemHatch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.createmod.catnip.api.math.VecHelper;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class HatchFilterSlot extends ValueBoxTransform {

	@Override
	public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
		return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 5.15, 9.5), angle(state), Direction.Axis.Y);
	}

	@Override
	public float getScale() {
		return super.getScale() * 0.965f;
	}

	public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
		return localHit.distanceTo(getLocalOffset(level, pos, state).subtract(0, 0.125, 0)) < scale / 2;
	}

	@Override
	public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
		ms.mulPose(Axis.YP.rotationDegrees(angle(state)));
		ms.mulPose(Axis.XP.rotationDegrees(-45));
	}

	private float angle(BlockState state) {
		return AngleHelper.horizontalAngle(state.getOptionalValue(ItemHatchBlock.FACING)
			.orElse(Direction.NORTH));
	}

}
