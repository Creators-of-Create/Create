package com.simibubi.create.content.contraptions.gantry;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftInstance;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class GantryCarriageInstance extends ShaftInstance<GantryCarriageBlockEntity> implements DynamicInstance {

	private final ModelData gantryCogs;

	final Direction facing;
	final Boolean alongFirst;
	final Direction.Axis rotationAxis;
	final float rotationMult;
	final BlockPos visualPos;

	private float lastAngle = Float.NaN;

	public GantryCarriageInstance(MaterialManager materialManager, GantryCarriageBlockEntity blockEntity) {
		super(materialManager, blockEntity);

		gantryCogs = getTransformMaterial()
								 .getModel(AllPartialModels.GANTRY_COGS, blockState)
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
	public void beginFrame() {
		float cogAngle = getCogAngle();

		if (Mth.equal(cogAngle, lastAngle)) return;

		animateCogs(cogAngle);
	}

	private float getCogAngle() {
		return GantryCarriageRenderer.getAngleForBE(blockEntity, visualPos, rotationAxis) * rotationMult;
	}

	private void animateCogs(float cogAngle) {
		gantryCogs.loadIdentity()
				.translate(getInstancePosition())
				.centre()
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
				.rotateY(alongFirst ^ facing.getAxis() == Direction.Axis.X ? 0 : 90)
				.translate(0, -9 / 16f, 0)
				.rotateX(-cogAngle)
				.translate(0, 9 / 16f, 0)
				.unCentre();
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
	public void remove() {
		super.remove();
		gantryCogs.delete();
	}
}
