package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.foundation.render.AllMaterialSpecs;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class StabilizedBearingInstance extends ActorInstance {

	final OrientedData topInstance;
	final RotatingData shaft;

	final Direction facing;
	final Vector3f rotationAxis;
	final Quaternion blockOrientation;

	public StabilizedBearingInstance(MaterialManager materialManager, VirtualRenderWorld simulationWorld, MovementContext context) {
		super(materialManager, simulationWorld, context);

		BlockState blockState = context.state;

		facing = blockState.getValue(BlockStateProperties.FACING);
		rotationAxis = Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis()).step();

		blockOrientation = BearingInstance.getBlockStateOrientation(facing);

        topInstance = materialManager.defaultSolid()
                .material(Materials.ORIENTED)
                .getModel(AllPartialModels.BEARING_TOP, blockState)
				.createInstance();

		int blockLight = localBlockLight();
		topInstance.setPosition(context.localPos)
				.setRotation(blockOrientation)
				.setBlockLight(blockLight);

		shaft = materialManager.defaultSolid()
				.material(AllMaterialSpecs.ROTATING)
				.getModel(AllPartialModels.SHAFT_HALF, blockState, blockState.getValue(BlockStateProperties.FACING).getOpposite())
				.createInstance();

		// not rotating so no need to set speed, axis, etc.
		shaft.setPosition(context.localPos)
				.setBlockLight(blockLight);
	}

	@Override
	public void beginFrame() {
		float counterRotationAngle = StabilizedBearingMovementBehaviour.getCounterRotationAngle(context, facing, AnimationTickHolder.getPartialTicks());

		Quaternion rotation = rotationAxis.rotationDegrees(counterRotationAngle);

		rotation.mul(blockOrientation);

		topInstance.setRotation(rotation);
	}
}
