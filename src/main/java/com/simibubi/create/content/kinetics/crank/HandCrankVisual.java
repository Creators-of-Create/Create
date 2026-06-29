package com.simibubi.create.content.kinetics.crank;

import java.util.function.Consumer;

import net.createmod.catnip.math.AngleHelper;

import org.joml.Quaternionf;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HandCrankVisual extends KineticBlockEntityVisual<HandCrankBlockEntity> implements SimpleDynamicVisual {
	private final RotatingInstance rotatingModel;
	private final TransformedInstance crank;

	public HandCrankVisual(VisualizationContext modelManager, HandCrankBlockEntity blockEntity, float partialTick) {
		super(modelManager, blockEntity, partialTick);

		crank = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.HAND_CRANK_HANDLE))
				.createInstance();

		rotateCrank(partialTick);

		rotatingModel = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.HAND_CRANK_BASE))
			.createInstance();

		rotatingModel.setup(HandCrankVisual.this.blockEntity)
			.setPosition(getVisualPosition())
			.rotateToFace(blockState.getValue(BlockStateProperties.FACING))
			.setChanged();
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		rotateCrank(ctx.partialTick());
	}

	private void rotateCrank(float pt) {
		var facing = blockState.getValue(BlockStateProperties.FACING);
		float angle = AngleHelper.rad(blockEntity.getIndependentAngle(pt));

		crank.setIdentityTransform()
			.translate(getVisualPosition())
			.center()
			.rotate(angle, Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis()))
			.rotate(new Quaternionf().rotateTo(0, 0, -1, facing.getStepX(), facing.getStepY(), facing.getStepZ()))
			.uncenter()
			.setChanged();
	}

	@Override
	protected void _delete() {
		crank.delete();
		rotatingModel.delete();
	}

	@Override
	public void update(float pt) {
		rotatingModel.setup(blockEntity)
			.setChanged();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(crank, rotatingModel);
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(crank);
		consumer.accept(rotatingModel);
	}
}
