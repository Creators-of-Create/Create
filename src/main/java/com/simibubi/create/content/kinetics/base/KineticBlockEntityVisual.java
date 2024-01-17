package com.simibubi.create.content.kinetics.base;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;

public abstract class KineticBlockEntityVisual<T extends KineticBlockEntity> extends AbstractBlockEntityVisual<T> {

	protected final Direction.Axis axis;

	public KineticBlockEntityVisual(VisualizationContext context, T blockEntity) {
		super(context, blockEntity);
		axis = (blockState.getBlock() instanceof IRotate irotate) ? irotate.getRotationAxis(blockState) : Axis.Y;
	}

	protected final void updateRotation(RotatingInstance instance) {
		updateRotation(instance, getRotationAxis(), getBlockEntitySpeed());
	}

	protected final void updateRotation(RotatingInstance instance, Direction.Axis axis) {
		updateRotation(instance, axis, getBlockEntitySpeed());
	}

	protected final void updateRotation(RotatingInstance instance, float speed) {
		updateRotation(instance, getRotationAxis(), speed);
	}

	protected final void updateRotation(RotatingInstance instance, Direction.Axis axis, float speed) {
		instance.setRotationAxis(axis)
			.setRotationOffset(getRotationOffset(axis))
			.setRotationalSpeed(speed)
			.setColor(blockEntity);
	}

	protected final RotatingInstance setup(RotatingInstance key) {
		return setup(key, getRotationAxis(), getBlockEntitySpeed());
	}

	protected final RotatingInstance setup(RotatingInstance key, Direction.Axis axis) {
		return setup(key, axis, getBlockEntitySpeed());
	}

	protected final RotatingInstance setup(RotatingInstance key, float speed) {
		return setup(key, getRotationAxis(), speed);
	}

	protected final RotatingInstance setup(RotatingInstance key, Direction.Axis axis, float speed) {
		key.setRotationAxis(axis)
			.setRotationalSpeed(speed)
			.setRotationOffset(getRotationOffset(axis))
			.setColor(blockEntity)
			.setPosition(getVisualPosition());

		return key;
	}

	protected float getRotationOffset(final Direction.Axis axis) {
		float offset = ICogWheel.isLargeCog(blockState) ? 11.25f : 0;
		double d = (((axis == Direction.Axis.X) ? 0 : pos.getX()) + ((axis == Direction.Axis.Y) ? 0 : pos.getY())
			+ ((axis == Direction.Axis.Z) ? 0 : pos.getZ())) % 2;
		if (d == 0) {
			offset = 22.5f;
		}
		return offset;
	}

	protected Direction.Axis getRotationAxis() {
		return axis;
	}

	protected float getBlockEntitySpeed() {
		return blockEntity.getSpeed();
	}

	protected BlockState shaft() {
		return shaft(getRotationAxis());
	}

	public static BlockState shaft(Direction.Axis axis) {
		return AllBlocks.SHAFT.getDefaultState()
			.setValue(ShaftBlock.AXIS, axis);
	}
}
