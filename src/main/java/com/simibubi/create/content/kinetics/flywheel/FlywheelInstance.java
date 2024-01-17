package com.simibubi.create.content.kinetics.flywheel;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityInstance;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.render.VirtualRenderHelper;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;

public class FlywheelInstance extends KineticBlockEntityInstance<FlywheelBlockEntity> implements DynamicVisual {

	protected final RotatingInstance shaft;
	protected final TransformedInstance wheel;
	protected float lastAngle = Float.NaN;

	public FlywheelInstance(VisualizationContext materialManager, FlywheelBlockEntity blockEntity) {
		super(materialManager, blockEntity);

		shaft = setup(instancerProvider.instancer(AllInstanceTypes.ROTATING, VirtualRenderHelper.blockModel(shaft()), RenderStage.AFTER_BLOCK_ENTITIES)
			.createInstance());
		wheel = instancerProvider.instancer(InstanceTypes.TRANSFORMED, VirtualRenderHelper.blockModel(blockState), RenderStage.AFTER_BLOCK_ENTITIES)
			.createInstance();

		animate(blockEntity.angle);
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {

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
		var msr = TransformStack.of(ms);

		msr.translate(getVisualPosition());
		msr.center()
			.rotate(AngleHelper.rad(angle), Direction.get(Direction.AxisDirection.POSITIVE, axis))
			.uncenter();

		wheel.setTransform(ms);
	}

	@Override
	public void update(float pt) {
		updateRotation(shaft);
	}

	@Override
	public void updateLight() {
		relight(pos, shaft, wheel);
	}

	@Override
	protected void _delete() {
		shaft.delete();
		wheel.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(shaft);
		consumer.accept(wheel);
	}
}
