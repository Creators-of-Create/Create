package com.simibubi.create.content.contraptions.components.flywheel;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.base.KineticBlockEntityInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;

public class FlywheelInstance extends KineticBlockEntityInstance<FlywheelBlockEntity> implements DynamicInstance {

	protected final RotatingData shaft;
	protected final ModelData wheel;
	protected float lastAngle = Float.NaN;

	public FlywheelInstance(MaterialManager materialManager, FlywheelBlockEntity blockEntity) {
		super(materialManager, blockEntity);

		shaft = setup(getRotatingMaterial().getModel(shaft())
			.createInstance());
		wheel = getTransformMaterial().getModel(blockState)
			.createInstance();

		animate(blockEntity.angle);
	}

	@Override
	public void beginFrame() {

		float partialTicks = AnimationTickHolder.getPartialTicks();

		float speed = blockEntity.visualSpeed.getValue(partialTicks) * 3 / 10f;
		float angle = blockEntity.angle + speed * partialTicks;

		if (Math.abs(angle - lastAngle) < 0.001)
			return;

		animate(angle);

		lastAngle = angle;
	}

	private void animate(float angle) {
		PoseStack ms = new PoseStack();
		TransformStack msr = TransformStack.cast(ms);

		msr.translate(getInstancePosition());
		msr.centre()
			.rotate(Direction.get(Direction.AxisDirection.POSITIVE, axis), AngleHelper.rad(angle))
			.unCentre();

		wheel.setTransform(ms);
	}

	@Override
	public void update() {
		updateRotation(shaft);
	}

	@Override
	public void updateLight() {
		relight(pos, shaft, wheel);
	}

	@Override
	public void remove() {
		shaft.delete();
		wheel.delete();
	}

}
