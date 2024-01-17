package com.simibubi.create.content.contraptions.gantry;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class GantryCarriageVisual extends ShaftVisual<GantryCarriageBlockEntity> implements DynamicVisual {

	private final TransformedInstance gantryCogs;

	final Direction facing;
	final Boolean alongFirst;
	final Direction.Axis rotationAxis;
	final float rotationMult;
	final BlockPos visualPos;

	private float lastAngle = Float.NaN;

	public GantryCarriageVisual(VisualizationContext context, GantryCarriageBlockEntity blockEntity) {
		super(context, blockEntity);

		gantryCogs = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.GANTRY_COGS), RenderStage.AFTER_BLOCK_ENTITIES)
								 .createInstance();

		facing = blockState.getValue(GantryCarriageBlock.FACING);
		alongFirst = blockState.getValue(GantryCarriageBlock.AXIS_ALONG_FIRST_COORDINATE);
		rotationAxis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);

		rotationMult = getRotationMultiplier(getGantryAxis(), facing);

		visualPos = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? blockEntity.getBlockPos()
				: blockEntity.getBlockPos()
					  .relative(facing.getOpposite());

		animateCogs(getCogAngle());
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {
		float cogAngle = getCogAngle();

		if (Mth.equal(cogAngle, lastAngle)) return;

		animateCogs(cogAngle);
	}

	private float getCogAngle() {
		return GantryCarriageRenderer.getAngleForBE(blockEntity, visualPos, rotationAxis) * rotationMult;
	}

	private void animateCogs(float cogAngle) {
		gantryCogs.loadIdentity()
				.translate(getVisualPosition())
				.center()
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
				.rotateY(alongFirst ^ facing.getAxis() == Direction.Axis.X ? 0 : 90)
				.translate(0, -9 / 16f, 0)
				.rotateX(-cogAngle)
				.translate(0, 9 / 16f, 0)
				.uncenter();
	}

	static float getRotationMultiplier(Direction.Axis gantryAxis, Direction facing) {
		float multiplier = 1;
		if (gantryAxis == Direction.Axis.X)
			if (facing == Direction.UP)
				multiplier *= -1;
		if (gantryAxis == Direction.Axis.Y)
			if (facing == Direction.NORTH || facing == Direction.EAST)
				multiplier *= -1;

		return multiplier;
	}

	private Direction.Axis getGantryAxis() {
		Direction.Axis gantryAxis = Direction.Axis.X;
		for (Direction.Axis axis : Iterate.axes)
			if (axis != rotationAxis && axis != facing.getAxis())
				gantryAxis = axis;
		return gantryAxis;
	}

	@Override
	public void updateLight() {
		relight(pos, gantryCogs, rotatingModel);
	}

	@Override
    protected void _delete() {
		super._delete();
		gantryCogs.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
		consumer.accept(gantryCogs);
	}
}
