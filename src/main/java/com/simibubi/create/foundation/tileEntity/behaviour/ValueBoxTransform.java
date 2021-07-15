package com.simibubi.create.foundation.tileEntity.behaviour;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3d;

public abstract class ValueBoxTransform {

	protected float scale = getScale();

	protected abstract Vector3d getLocalOffset(BlockState state);

	protected abstract void rotate(BlockState state, MatrixStack ms);

	public boolean testHit(BlockState state, Vector3d localHit) {
		Vector3d offset = getLocalOffset(state);
		if (offset == null)
			return false;
		return localHit.distanceTo(offset) < scale / 2;
	}

	public void transform(BlockState state, MatrixStack ms) {
		Vector3d position = getLocalOffset(state);
		if (position == null)
			return;
		ms.translate(position.x, position.y, position.z);
		rotate(state, ms);
		ms.scale(scale, scale, scale);
	}

	public boolean shouldRender(BlockState state) {
		return state.getMaterial() != Material.AIR && getLocalOffset(state) != null;
	}

	protected Vector3d rotateHorizontally(BlockState state, Vector3d vec) {
		float yRot = 0;
		if (state.hasProperty(BlockStateProperties.FACING))
			yRot = AngleHelper.horizontalAngle(state.getValue(BlockStateProperties.FACING));
		if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
			yRot = AngleHelper.horizontalAngle(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
		return VecHelper.rotateCentered(vec, yRot, Axis.Y);
	}

	protected float getScale() {
		return .4f;
	}

	protected float getFontScale() {
		return 1 / 64f;
	}

	public static abstract class Dual extends ValueBoxTransform {

		protected boolean first;

		public Dual(boolean first) {
			this.first = first;
		}

		public boolean isFirst() {
			return first;
		}

		public static Pair<ValueBoxTransform, ValueBoxTransform> makeSlots(Function<Boolean, ? extends Dual> factory) {
			return Pair.of(factory.apply(true), factory.apply(false));
		}

		public boolean testHit(BlockState state, Vector3d localHit) {
			Vector3d offset = getLocalOffset(state);
			if (offset == null)
				return false;
			return localHit.distanceTo(offset) < scale / 3.5f;
		}

	}

	public static abstract class Sided extends ValueBoxTransform {

		protected Direction direction = Direction.UP;

		public Sided fromSide(Direction direction) {
			this.direction = direction;
			return this;
		}

		@Override
		protected Vector3d getLocalOffset(BlockState state) {
			Vector3d location = getSouthLocation();
			location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Axis.Y);
			location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Axis.Z);
			return location;
		}

		protected abstract Vector3d getSouthLocation();

		@Override
		protected void rotate(BlockState state, MatrixStack ms) {
			float yRot = AngleHelper.horizontalAngle(getSide()) + 180;
			float xRot = getSide() == Direction.UP ? 90 : getSide() == Direction.DOWN ? 270 : 0;
			MatrixStacker.of(ms)
				.rotateY(yRot)
				.rotateX(xRot);
		}

		@Override
		public boolean shouldRender(BlockState state) {
			return super.shouldRender(state) && isSideActive(state, getSide());
		}

		@Override
		public boolean testHit(BlockState state, Vector3d localHit) {
			return isSideActive(state, getSide()) && super.testHit(state, localHit);
		}

		protected boolean isSideActive(BlockState state, Direction direction) {
			return true;
		}

		public Direction getSide() {
			return direction;
		}

	}

}
