package com.simibubi.create.content.kinetics.crank;

import java.util.function.Consumer;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.render.VirtualRenderHelper;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HandCrankVisual extends KineticBlockEntityVisual<HandCrankBlockEntity> implements SimpleDynamicVisual {
	protected RotatingInstance rotatingModel;
	private final TransformedInstance crank;
	private final Direction facing;

	public HandCrankVisual(VisualizationContext modelManager, HandCrankBlockEntity blockEntity, float partialTick) {
		super(modelManager, blockEntity, partialTick);
		facing = blockState.getValue(BlockStateProperties.FACING);
		Model model = blockEntity.getRenderedHandleInstance();

		crank = instancerProvider.instancer(InstanceTypes.TRANSFORMED, model)
				.createInstance();

		rotateCrank(partialTick);

		if (blockEntity.shouldRenderShaft()) {
			rotatingModel = instancerProvider.instancer(AllInstanceTypes.ROTATING, VirtualRenderHelper.blockModel(blockState))
					.createInstance();
			setup(rotatingModel);
		}
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		rotateCrank(ctx.partialTick());
	}

	private void rotateCrank(float pt) {
		Direction.Axis axis = facing.getAxis();
		float angle = blockEntity.getIndependentAngle(pt);

		crank.loadIdentity()
			.translate(getVisualPosition())
			.center()
			.rotate(angle, Direction.get(Direction.AxisDirection.POSITIVE, axis))
			.uncenter()
			.setChanged();
	}

	@Override
	protected void _delete() {
		crank.delete();
	}

	@Override
	public void update(float pt) {
		if (rotatingModel != null)
			updateRotation(rotatingModel);
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
