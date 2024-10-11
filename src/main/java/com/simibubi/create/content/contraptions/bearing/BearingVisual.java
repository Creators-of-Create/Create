package com.simibubi.create.content.contraptions.bearing;

import java.util.function.Consumer;

import org.joml.Quaternionf;

import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.BackHalfShaftVisual;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.AngleHelper;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BearingVisual<B extends KineticBlockEntity & IBearingBlockEntity> extends BackHalfShaftVisual<B> implements SimpleDynamicVisual {
	final OrientedInstance topInstance;

	final Axis rotationAxis;
	final Quaternionf blockOrientation;

	public BearingVisual(VisualizationContext context, B blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);

		Direction facing = blockState.getValue(BlockStateProperties.FACING);
		rotationAxis = Axis.of(Direction.get(Direction.AxisDirection.POSITIVE, axis).step());

		blockOrientation = getBlockStateOrientation(facing);

		PartialModel top =
				blockEntity.isWoodenTop() ? AllPartialModels.BEARING_TOP_WOODEN : AllPartialModels.BEARING_TOP;

		topInstance = instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(top))
				.createInstance();

		topInstance.position(getVisualPosition())
				.rotation(blockOrientation)
				.setChanged();
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		float interpolatedAngle = blockEntity.getInterpolatedAngle(ctx.partialTick() - 1);
		Quaternionf rot = rotationAxis.rotationDegrees(interpolatedAngle);

		rot.mul(blockOrientation);

		topInstance.rotation(rot)
				.setChanged();
	}

	@Override
	public void updateLight(float partialTick) {
		super.updateLight(partialTick);
		relight(topInstance);
	}

	@Override
	protected void _delete() {
		super._delete();
		topInstance.delete();
	}

	static Quaternionf getBlockStateOrientation(Direction facing) {
		Quaternionf orientation;

		if (facing.getAxis().isHorizontal()) {
			orientation = Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing.getOpposite()));
		} else {
			orientation = new Quaternionf();
		}

		orientation.mul(Axis.XP.rotationDegrees(-90 - AngleHelper.verticalAngle(facing)));
		return orientation;
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
		consumer.accept(topInstance);
	}
}
