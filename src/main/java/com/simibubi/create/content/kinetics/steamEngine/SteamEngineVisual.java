package com.simibubi.create.content.kinetics.steamEngine;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.jozufozu.flywheel.lib.visual.SimpleDynamicVisual;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;

public class SteamEngineVisual extends AbstractBlockEntityVisual<SteamEngineBlockEntity> implements SimpleDynamicVisual {

	protected final TransformedInstance piston;
	protected final TransformedInstance linkage;
	protected final TransformedInstance connector;

	public SteamEngineVisual(VisualizationContext context, SteamEngineBlockEntity blockEntity) {
		super(context, blockEntity);

		piston = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ENGINE_PISTON))
				.createInstance();
		linkage = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ENGINE_LINKAGE))
				.createInstance();
		connector = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ENGINE_CONNECTOR))
				.createInstance();
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {
		Float angle = blockEntity.getTargetAngle();
		if (angle == null) {
			piston.setEmptyTransform().setChanged();
			linkage.setEmptyTransform().setChanged();
			connector.setEmptyTransform().setChanged();
			return;
		}

		Direction facing = SteamEngineBlock.getFacing(blockState);
		Axis facingAxis = facing.getAxis();
		Axis axis = Axis.Y;

		PoweredShaftBlockEntity shaft = blockEntity.getShaft();
		if (shaft != null)
			axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);

		boolean roll90 = facingAxis.isHorizontal() && axis == Axis.Y || facingAxis.isVertical() && axis == Axis.Z;
		float sine = Mth.sin(angle);
		float sine2 = Mth.sin(angle - Mth.HALF_PI);
		float piston = ((1 - sine) / 4) * 24 / 16f;

		transformed(this.piston, facing, roll90)
			.translate(0, piston, 0)
			.setChanged();

		transformed(linkage, facing, roll90)
			.center()
			.translate(0, 1, 0)
			.uncenter()
			.translate(0, piston, 0)
			.translate(0, 4 / 16f, 8 / 16f)
			.rotateXDegrees(sine2 * 23f)
			.translate(0, -4 / 16f, -8 / 16f)
			.setChanged();

		transformed(connector, facing, roll90)
			.translate(0, 2, 0)
			.center()
			.rotateX(-angle + Mth.HALF_PI)
			.uncenter()
			.setChanged();
	}

	protected TransformedInstance transformed(TransformedInstance modelData, Direction facing, boolean roll90) {
		return modelData.loadIdentity()
			.translate(getVisualPosition())
			.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(facing))
			.rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
			.rotateYDegrees(roll90 ? -90 : 0)
			.uncenter();
	}

	@Override
	public void updateLight() {
		relight(pos, piston, linkage, connector);
	}

	@Override
	protected void _delete() {
		piston.delete();
		linkage.delete();
		connector.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(piston);
		consumer.accept(linkage);
		consumer.accept(connector);
	}
}
