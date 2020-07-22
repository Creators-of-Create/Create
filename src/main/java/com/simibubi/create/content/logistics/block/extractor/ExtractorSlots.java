package com.simibubi.create.content.logistics.block.extractor;

import static net.minecraft.block.HorizontalBlock.HORIZONTAL_FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.logistics.block.AttachedLogisticalBlock;
import com.simibubi.create.content.logistics.block.transposer.TransposerBlock;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3d;

public class ExtractorSlots {

	static class Filter extends ValueBoxTransform {

		Vector3d offsetForHorizontal = VecHelper.voxelSpace(8f, 10.5f, 14f);
		Vector3d offsetForUpward = VecHelper.voxelSpace(8f, 14.15f, 3.5f);
		Vector3d offsetForDownward = VecHelper.voxelSpace(8f, 1.85f, 3.5f);

		@Override
		protected Vector3d getLocalOffset(BlockState state) {
			Vector3d location = offsetForHorizontal;
			if (state.getBlock() instanceof TransposerBlock)
				location = location.add(0, 2 / 16f, 0);
			if (AttachedLogisticalBlock.isVertical(state))
				location = state.get(AttachedLogisticalBlock.UPWARD) ? offsetForUpward : offsetForDownward;
			return rotateHorizontally(state, location);
		}

		@Override
		protected void rotate(BlockState state, MatrixStack ms) {
			float yRot = AngleHelper.horizontalAngle(state.get(HORIZONTAL_FACING));
			float xRot = (AttachedLogisticalBlock.isVertical(state)) ? 0 : 90;
			MatrixStacker.of(ms)
				.rotateY(yRot)
				.rotateX(xRot);
		}

	}

	public static class Link extends ValueBoxTransform.Dual {

		public Link(boolean first) {
			super(first);
		}

		Vector3d offsetForHorizontal = VecHelper.voxelSpace(11.5f, 4f, 14f);
		Vector3d offsetForUpward = VecHelper.voxelSpace(10f, 14f, 11.5f);
		Vector3d offsetForDownward = VecHelper.voxelSpace(10f, 2f, 11.5f);

		@Override
		protected Vector3d getLocalOffset(BlockState state) {
			Vector3d location = offsetForHorizontal;
			if (state.getBlock() instanceof TransposerBlock)
				location = location.add(0, 2 / 16f, 0);
			if (!isFirst())
				location = location.add(0, 4 / 16f, 0);

			if (AttachedLogisticalBlock.isVertical(state)) {
				location = state.get(AttachedLogisticalBlock.UPWARD) ? offsetForUpward : offsetForDownward;
				if (!isFirst())
					location = location.add(-4 / 16f, 0, 0);
			}

			float yRot = AngleHelper.horizontalAngle(state.get(HORIZONTAL_FACING));
			location = VecHelper.rotateCentered(location, yRot, Axis.Y);
			return location;
		}

		@Override
		protected void rotate(BlockState state, MatrixStack ms) {
			float horizontalAngle = AngleHelper.horizontalAngle(state.get(HORIZONTAL_FACING));
			boolean vertical = AttachedLogisticalBlock.isVertical(state);
			float yRot = vertical ? horizontalAngle + 180 : horizontalAngle + 270;
			float zRot = vertical ? (state.get(AttachedLogisticalBlock.UPWARD) ? 90 : 270) : 0;
			MatrixStacker.of(ms)
				.rotateY(yRot)
				.rotateZ(zRot);
		}

	}

}
