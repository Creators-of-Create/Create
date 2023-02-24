package com.simibubi.create.content.contraptions.relays.elementary;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;

public class BracketedKineticBlockEntityInstance extends SingleRotatingInstance<BracketedKineticBlockEntity> {

	protected RotatingData additionalShaft;

	public BracketedKineticBlockEntityInstance(MaterialManager materialManager, BracketedKineticBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	public void init() {
		super.init();
		if (!ICogWheel.isLargeCog(blockEntity.getBlockState()))
			return;

		// Large cogs sometimes have to offset their teeth by 11.25 degrees in order to
		// mesh properly

		float speed = blockEntity.getSpeed();
		Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
		BlockPos pos = blockEntity.getBlockPos();
		float offset = BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos);
		Direction facing = Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE);
		Instancer<RotatingData> half = getRotatingMaterial().getModel(AllPartialModels.COGWHEEL_SHAFT, blockState,
			facing, () -> this.rotateToAxis(axis));

		additionalShaft = setup(half.createInstance(), speed);
		additionalShaft.setRotationOffset(offset);
	}

	@Override
	protected Instancer<RotatingData> getModel() {
		if (!ICogWheel.isLargeCog(blockEntity.getBlockState()))
			return super.getModel();

		Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
		Direction facing = Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE);
		return getRotatingMaterial().getModel(AllPartialModels.SHAFTLESS_LARGE_COGWHEEL, blockState, facing,
			() -> this.rotateToAxis(axis));
	}

	private PoseStack rotateToAxis(Axis axis) {
		Direction facing = Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE);
		PoseStack poseStack = new PoseStack();
		TransformStack.cast(poseStack)
				.centre()
				.rotateToFace(facing)
				.multiply(Vector3f.XN.rotationDegrees(-90))
				.unCentre();
		return poseStack;
	}

	@Override
	public void update() {
		super.update();
		if (additionalShaft != null) {
			updateRotation(additionalShaft);
			additionalShaft.setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos));
		}
	}

	@Override
	public void updateLight() {
		super.updateLight();
		if (additionalShaft != null)
			relight(pos, additionalShaft);
	}

	@Override
	public void remove() {
		super.remove();
		if (additionalShaft != null)
			additionalShaft.delete();
	}

}
