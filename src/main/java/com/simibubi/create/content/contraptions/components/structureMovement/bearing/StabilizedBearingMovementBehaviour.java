package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StabilizedBearingMovementBehaviour extends MovementBehaviour {

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffer) {
		Direction facing = context.state.get(BlockStateProperties.FACING);
		AllBlockPartials top = AllBlockPartials.BEARING_TOP;
		SuperByteBuffer superBuffer = top.renderOn(context.state);
		float renderPartialTicks = AnimationTickHolder.getPartialTicks();

		// rotate to match blockstate
		Axis axis = facing.getAxis();
		if (axis.isHorizontal())
			superBuffer.rotateCentered(Direction.UP,
				AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())));
		superBuffer.rotateCentered(Direction.EAST, AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)));

		// rotate against parent
		float offset = 0;
		int offsetMultiplier = facing.getAxisDirection().getOffset();
		
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
		if (offset != 0)
			superBuffer.rotateCentered(Direction.UP, AngleHelper.rad(offset * offsetMultiplier));

		// render
		superBuffer.light(msLocal.peek()
			.getModel(), ContraptionRenderDispatcher.getLightOnContraption(context));
		superBuffer.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

}
