package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionKineticRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StabilizedBearingMovementBehaviour extends MovementBehaviour {

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext context, PlacementSimulationWorld renderWorld,
		ContraptionMatrices matrices, IRenderTypeBuffer buffer) {
		if (Backend.canUseInstancing()) return;

		Direction facing = context.state.get(BlockStateProperties.FACING);
		PartialModel top = AllBlockPartials.BEARING_TOP;
		SuperByteBuffer superBuffer = PartialBufferer.get(top, context.state);
		float renderPartialTicks = AnimationTickHolder.getPartialTicks();

		// rotate to match blockstate
		Quaternion orientation = BearingInstance.getBlockStateOrientation(facing);

		// rotate against parent
		float angle = getCounterRotationAngle(context, facing, renderPartialTicks) * facing.getAxisDirection().getOffset();

		Quaternion rotation = facing.getUnitVector().getDegreesQuaternion(angle);

		rotation.multiply(orientation);

		orientation = rotation;

		superBuffer.transform(matrices.contraptionStack);
		superBuffer.rotateCentered(orientation);

		// render
		superBuffer
			.light(matrices.entityMatrix,
				ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
			.renderInto(matrices.entityStack, buffer.getBuffer(RenderType.getSolid()));
	}

	@Override
	public boolean hasSpecialInstancedRendering() {
		return true;
	}

	@Nullable
	@Override
	public ActorInstance createInstance(ContraptionKineticRenderer kr, MovementContext context) {
		return new StabilizedBearingInstance(kr, context);
	}

	static float getCounterRotationAngle(MovementContext context, Direction facing, float renderPartialTicks) {
		float offset = 0;

		Axis axis = facing.getAxis();

		AbstractContraptionEntity entity = context.contraption.entity;
		if (entity instanceof ControlledContraptionEntity) {
			ControlledContraptionEntity controlledCE = (ControlledContraptionEntity) entity;
			if (context.contraption.canBeStabilized(facing, context.localPos))
				offset = -controlledCE.getAngle(renderPartialTicks);

		} else if (entity instanceof OrientedContraptionEntity) {
			OrientedContraptionEntity orientedCE = (OrientedContraptionEntity) entity;
			if (axis.isVertical())
				offset = -orientedCE.getYaw(renderPartialTicks);
			else {
				if (orientedCE.isInitialOrientationPresent() && orientedCE.getInitialOrientation()
						.getAxis() == axis)
					offset = -orientedCE.getPitch(renderPartialTicks);
			}
		}
		return offset;
	}


}
