package com.simibubi.create.content.kinetics.steamEngine;

import java.util.Objects;
import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;

public class SteamEngineVisual extends AbstractBlockEntityVisual<SteamEngineBlockEntity> implements SimpleDynamicVisual {

	protected final TransformedInstance piston;
	protected final TransformedInstance linkage;
	protected final TransformedInstance connector;

	private Float lastAngle = Float.NaN;
	private Axis lastAxis = null;

	public SteamEngineVisual(VisualizationContext context, SteamEngineBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);

		piston = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ENGINE_PISTON))
			.createInstance();
		linkage = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ENGINE_LINKAGE))
			.createInstance();
		connector = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ENGINE_CONNECTOR))
			.createInstance();

		animate();
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		animate();
	}

	private void animate() {
		Float angle = blockEntity.getTargetAngle();
		Axis axis = Axis.Y;

		PoweredShaftBlockEntity shaft = blockEntity.getShaft();
		if (shaft != null)
			axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);

		if (Objects.equals(angle, lastAngle) && lastAxis == axis) {
			return;
		}

		lastAngle = angle;
		lastAxis = axis;

		if (angle == null) {
			piston.setVisible(false);
			linkage.setVisible(false);
			connector.setVisible(false);
			return;
		} else {
			piston.setVisible(true);
			linkage.setVisible(true);
			connector.setVisible(true);
		}

		Direction facing = SteamEngineBlock.getFacing(blockState);
		Axis facingAxis = facing.getAxis();

		boolean roll90 = facingAxis.isHorizontal() && axis == Axis.Y || facingAxis.isVertical() && axis == Axis.Z;
		float piston = ((6 / 16f) * Mth.sin(angle) - Mth.sqrt(Mth.square(14 / 16f) - Mth.square(6 / 16f) * Mth.square(Mth.cos(angle))));
		float distance = Mth.sqrt(Mth.square(piston - 6 / 16f * Mth.sin(angle)));
		float angle2 = (float) Math.acos(distance / (14 / 16f)) * (Mth.cos(angle) >= 0 ? 1f : -1f);

		transformed(this.piston, facing, roll90)
			.translate(0, piston + 20 / 16f, 0)
			.setChanged();

		transformed(linkage, facing, roll90)
			.center()
			.translate(0, 1, 0)
			.uncenter()
			.translate(0, piston + 20 / 16f, 0)
			.translate(0, 4 / 16f, 8 / 16f)
			.rotateX(angle2)
			.translate(0, -4 / 16f, -8 / 16f)
			.setChanged();

		transformed(connector, facing, roll90)
			.translate(0, 2, 0)
			.center()
			.rotateX(-(angle + Mth.HALF_PI))
			.uncenter()
			.setChanged();
	}

	protected TransformedInstance transformed(TransformedInstance modelData, Direction facing, boolean roll90) {
		return modelData.setIdentityTransform()
			.translate(getVisualPosition())
			.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(facing))
			.rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
			.rotateYDegrees(roll90 ? -90 : 0)
			.uncenter();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(piston, linkage, connector);
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
