package com.simibubi.create.content.contraptions.bearing;

import org.joml.Quaternionf;

import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class StabilizedBearingVisual extends ActorVisual {

	final OrientedInstance topInstance;
	final RotatingInstance shaft;

	final Direction facing;
	final Axis rotationAxis;
	final Quaternionf blockOrientation;

	public StabilizedBearingVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
		super(visualizationContext, movementContext.world, movementContext);

		BlockState blockState = movementContext.state;

		facing = blockState.getValue(BlockStateProperties.FACING);
		rotationAxis = Axis.of(Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis()).step());

		blockOrientation = BearingVisual.getBlockStateOrientation(facing);

        topInstance = instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.BEARING_TOP))
				.createInstance();

		int blockLight = localBlockLight();
		topInstance.position(movementContext.localPos)
				.rotation(blockOrientation)
				.light(blockLight, 0)
				.setChanged();

		shaft = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF))
				.createInstance();

		// not rotating so no need to set speed.
		var axis = KineticBlockEntityVisual.rotationAxis(blockState);
		shaft.setRotationAxis(axis)
			.setRotationOffset(KineticBlockEntityVisual.rotationOffset(blockState, axis, movementContext.localPos))
			.setPosition(movementContext.localPos)
			.rotateToFace(Direction.SOUTH, blockState.getValue(BlockStateProperties.FACING).getOpposite())
			.light(blockLight, 0)
			.setChanged();
	}

	@Override
	public void beginFrame() {
		float counterRotationAngle = StabilizedBearingMovementBehaviour.getCounterRotationAngle(context, facing, AnimationTickHolder.getPartialTicks());

		Quaternionf rotation = rotationAxis.rotationDegrees(counterRotationAngle);

		rotation.mul(blockOrientation);

		topInstance.rotation(rotation)
			.setChanged();
	}

	@Override
	protected void _delete() {
		topInstance.delete();
		shaft.delete();
	}
}
