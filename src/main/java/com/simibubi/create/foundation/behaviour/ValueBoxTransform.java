package com.simibubi.create.foundation.behaviour;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.GlHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public abstract class ValueBoxTransform {

	protected float scale = getScale();

	protected abstract Vec3d getLocation(BlockState state);

	protected abstract Vec3d getOrientation(BlockState state);

	public boolean testHit(BlockState state, Vec3d localHit) {
		Vec3d offset = getLocation(state);
		if (offset == null)
			return false;
		return localHit.distanceTo(offset) < scale / 2;
	}

	public void renderTransformed(BlockState state, Runnable render) {
		Vec3d position = getLocation(state);
		Vec3d rotation = getOrientation(state);
		GlHelper.renderTransformed(position, rotation, scale, render);
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
			Vec3d offset = getLocation(state);
			if (offset == null)
				return false;
			return localHit.distanceTo(offset) < scale / 3.5f;
		}

	}
	
	public static abstract class Sided extends ValueBoxTransform {
		
		Direction direction = Direction.UP;
		
		public Sided fromSide(Direction direction) {
			this.direction = direction;
			return this;
		}
		
		@Override
		protected Vec3d getLocation(BlockState state) {
			Vec3d location = getSouthLocation();
			location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(direction), Axis.Y);
			location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(direction), Axis.Z);
			return location;
		}
		
		protected abstract Vec3d getSouthLocation();

		@Override
		protected Vec3d getOrientation(BlockState state) {
			float yRot = AngleHelper.horizontalAngle(direction) + 180;
			float zRot = direction == Direction.UP ? 90 : direction == Direction.DOWN ? 270 : 0;
			return new Vec3d(0, yRot, zRot);
		}
		
		@Override
		public void renderTransformed(BlockState state, Runnable render) {
			if (!isSideActive(state, direction))
				return;
			super.renderTransformed(state, render);
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
