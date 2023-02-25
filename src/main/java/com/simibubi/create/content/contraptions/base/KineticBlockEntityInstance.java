package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.foundation.render.AllMaterialSpecs;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;

public abstract class KineticBlockEntityInstance<T extends KineticBlockEntity> extends BlockEntityInstance<T> {

	protected final Direction.Axis axis;

	public KineticBlockEntityInstance(MaterialManager materialManager, T blockEntity) {
		super(materialManager, blockEntity);
		axis = (blockState.getBlock() instanceof IRotate irotate) ? irotate.getRotationAxis(blockState) : Axis.Y;
	}

	protected final void updateRotation(RotatingData instance) {
		updateRotation(instance, getRotationAxis(), getBlockEntitySpeed());
	}

	protected final void updateRotation(RotatingData instance, Direction.Axis axis) {
		updateRotation(instance, axis, getBlockEntitySpeed());
	}

	protected final void updateRotation(RotatingData instance, float speed) {
		updateRotation(instance, getRotationAxis(), speed);
	}

	protected final void updateRotation(RotatingData instance, Direction.Axis axis, float speed) {
		instance.setRotationAxis(axis)
			.setRotationOffset(getRotationOffset(axis))
			.setRotationalSpeed(speed)
			.setColor(blockEntity);
	}

	protected final RotatingData setup(RotatingData key) {
		return setup(key, getRotationAxis(), getBlockEntitySpeed());
	}

	protected final RotatingData setup(RotatingData key, Direction.Axis axis) {
		return setup(key, axis, getBlockEntitySpeed());
	}

	protected final RotatingData setup(RotatingData key, float speed) {
		return setup(key, getRotationAxis(), speed);
	}

	protected final RotatingData setup(RotatingData key, Direction.Axis axis, float speed) {
		key.setRotationAxis(axis)
			.setRotationalSpeed(speed)
			.setRotationOffset(getRotationOffset(axis))
			.setColor(blockEntity)
			.setPosition(getInstancePosition());

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

	protected Material<RotatingData> getRotatingMaterial() {
		return materialManager.defaultSolid()
			.material(AllMaterialSpecs.ROTATING);
	}

	public static BlockState shaft(Direction.Axis axis) {
		return AllBlocks.SHAFT.getDefaultState()
			.setValue(ShaftBlock.AXIS, axis);
	}
}
