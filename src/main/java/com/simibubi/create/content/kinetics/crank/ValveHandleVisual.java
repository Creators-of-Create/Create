package com.simibubi.create.content.kinetics.crank;

import java.util.function.Consumer;

import net.createmod.catnip.math.AngleHelper;

import org.joml.Quaternionf;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ValveHandleVisual extends KineticBlockEntityVisual<HandCrankBlockEntity> implements SimpleDynamicVisual {
	private final TransformedInstance crank;

	public ValveHandleVisual(VisualizationContext modelManager, HandCrankBlockEntity blockEntity, float partialTick) {
		super(modelManager, blockEntity, partialTick);

		BlockState state = blockEntity.getBlockState();
		DyeColor color = null;
		if (state != null && state.getBlock() instanceof ValveHandleBlock vhb)
			color = vhb.color;

		crank = instancerProvider()
			.instancer(InstanceTypes.TRANSFORMED,
				Models.partial(
					color == null ? AllPartialModels.VALVE_HANDLE : AllPartialModels.DYED_VALVE_HANDLES.get(color)))
			.createInstance();

		rotateCrank(partialTick);
	}

	@Override
	public void beginFrame(Context ctx) {
		rotateCrank(ctx.partialTick());
	}

	private void rotateCrank(float pt) {
		var facing = blockState.getValue(BlockStateProperties.FACING);
		float angle = AngleHelper.rad(blockEntity.getIndependentAngle(pt));

		crank.setIdentityTransform()
			.translate(getVisualPosition())
			.center()
			.rotate(angle, Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis()))
			.rotate(new Quaternionf().rotateTo(0, 1, 0, facing.getStepX(), facing.getStepY(), facing.getStepZ()))
			.uncenter()
			.setChanged();
	}

	@Override
	protected void _delete() {
		crank.delete();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(crank);
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(crank);
	}
}
