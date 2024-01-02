package com.simibubi.create.content.kinetics.simpleRelays;

import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;

public class BracketedKineticBlockEntityInstance extends SingleRotatingInstance<BracketedKineticBlockEntity> {

	protected RotatingInstance additionalShaft;

	public BracketedKineticBlockEntityInstance(VisualizationContext materialManager, BracketedKineticBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	public void init(float pt) {
		super.init(pt);
		if (!ICogWheel.isLargeCog(blockEntity.getBlockState()))
			return;

		// Large cogs sometimes have to offset their teeth by 11.25 degrees in order to
		// mesh properly

		float speed = blockEntity.getSpeed();
		Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
		BlockPos pos = blockEntity.getBlockPos();
		float offset = BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos);
		Direction facing = Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE);
		Instancer<RotatingInstance> half = materialManager.defaultSolid()
				.material(AllInstanceTypes.ROTATING)
				.getModel(AllPartialModels.COGWHEEL_SHAFT, blockState,
			facing, () -> this.rotateToAxis(axis));

		additionalShaft = setup(half.createInstance(), speed);
		additionalShaft.setRotationOffset(offset);
	}

	@Override
	protected Model model() {
		if (!ICogWheel.isLargeCog(blockEntity.getBlockState()))
			return super.model();

		Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
		Direction facing = Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE);
		return model(AllPartialModels.SHAFTLESS_LARGE_COGWHEEL, blockState, facing,
				() -> this.rotateToAxis(axis));
	}

	private PoseStack rotateToAxis(Direction.Axis axis) {
		Direction facing = Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE);
		PoseStack poseStack = new PoseStack();
		TransformStack.of(poseStack)
				.center()
				.rotateToFace(facing)
				.multiply(Axis.XN.rotationDegrees(-90))
				.uncenter();
		return poseStack;
	}

	@Override
	public void update(float pt) {
		super.update(pt);
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
    protected void _delete() {
		super._delete();
		if (additionalShaft != null)
			additionalShaft.delete();
	}

}
