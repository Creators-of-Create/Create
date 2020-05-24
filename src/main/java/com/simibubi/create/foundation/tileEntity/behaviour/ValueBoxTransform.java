package com.simibubi.create.foundation.tileEntity.behaviour;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public abstract class ValueBoxTransform {

	protected float scale = getScale();

	protected abstract Vec3d getLocalOffset(BlockState state);

	protected abstract void rotate(BlockState state, MatrixStack ms);

	public boolean testHit(BlockState state, Vec3d localHit) {
		Vec3d offset = getLocalOffset(state);
		if (offset == null)
			return false;
		return localHit.distanceTo(offset) < scale / 2;
	}

	public void transform(BlockState state, MatrixStack ms) {
		Vec3d position = getLocalOffset(state);
		if (position == null)
			return;
		ms.translate(position.x, position.y, position.z);
		rotate(state, ms);
		ms.scale(scale, scale, scale);
	}

	public boolean shouldRender(BlockState state) {
		return state.getMaterial() != Material.AIR && getLocalOffset(state) != null;
	}

	protected Vec3d rotateHorizontally(BlockState state, Vec3d vec) {
		float yRot = 0;
		if (state.has(BlockStateProperties.FACING))
			yRot = AngleHelper.horizontalAngle(state.get(BlockStateProperties.FACING));
		if (state.has(BlockStateProperties.HORIZONTAL_FACING))
			yRot = AngleHelper.horizontalAngle(state.get(BlockStateProperties.HORIZONTAL_FACING));
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

		public boolean testHit(BlockState state, Vec3d localHit) {
			Vec3d offset = getLocalOffset(state);
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
		protected Vec3d getLocalOffset(BlockState state) {
			Vec3d location = getSouthLocation();
			location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(direction), Axis.Y);
			location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(direction), Axis.Z);
			return location;
		}

		protected abstract Vec3d getSouthLocation();

		@Override
		protected void rotate(BlockState state, MatrixStack ms) {
			float yRot = AngleHelper.horizontalAngle(direction) + 180;
			float xRot = direction == Direction.UP ? 90 : direction == Direction.DOWN ? 270 : 0;
			ms.multiply(VecHelper.rotateY(yRot));
			ms.multiply(VecHelper.rotateX(xRot));
		}

		@Override
		public boolean shouldRender(BlockState state) {
			return super.shouldRender(state) && isSideActive(state, direction);
		}

		@Override
		public boolean testHit(BlockState state, Vec3d localHit) {
			return isSideActive(state, direction) && super.testHit(state, localHit);
		}

		protected boolean isSideActive(BlockState state, Direction direction) {
			return true;
		}

	}

}
