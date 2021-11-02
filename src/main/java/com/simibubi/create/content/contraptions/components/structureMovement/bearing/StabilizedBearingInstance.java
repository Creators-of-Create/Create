package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class StabilizedBearingInstance extends ActorInstance {

	final OrientedData topInstance;

	final Direction facing;
	final Vector3f rotationAxis;
	final Quaternion blockOrientation;

	public StabilizedBearingInstance(MaterialManager materialManager, PlacementSimulationWorld simulationWorld, MovementContext context) {
		super(materialManager, simulationWorld, context);

		BlockState blockState = context.state;

		facing = blockState.getValue(BlockStateProperties.FACING);
		rotationAxis = Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis()).step();

		blockOrientation = BearingInstance.getBlockStateOrientation(facing);

        topInstance = materialManager.defaultSolid()
                .material(Materials.ORIENTED)
                .getModel(AllBlockPartials.BEARING_TOP, blockState).createInstance();

		topInstance.setPosition(context.localPos)
				.setRotation(blockOrientation)
				.setBlockLight(localBlockLight());
	}

	@Override
	public void beginFrame() {
		float counterRotationAngle = StabilizedBearingMovementBehaviour.getCounterRotationAngle(context, facing, AnimationTickHolder.getPartialTicks());

		Quaternion rotation = rotationAxis.rotationDegrees(counterRotationAngle);

		rotation.mul(blockOrientation);

		topInstance.setRotation(rotation);
	}
}
