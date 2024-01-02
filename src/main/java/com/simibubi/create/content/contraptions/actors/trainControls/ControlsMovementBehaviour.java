package com.simibubi.create.content.contraptions.actors.trainControls;

import java.util.Collection;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.render.VirtualRenderWorld;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ControlsMovementBehaviour implements MovementBehaviour {

	// TODO: rendering the levers should be specific to Carriage Contraptions -
	static class LeverAngles {
		LerpedFloat steering = LerpedFloat.linear();
		LerpedFloat speed = LerpedFloat.linear();
		LerpedFloat equipAnimation = LerpedFloat.linear();
	}

	@Override
	public ItemStack canBeDisabledVia(MovementContext context) {
		return null;
	}

	@Override
	public void stopMoving(MovementContext context) {
		context.contraption.entity.stopControlling(context.localPos);
		MovementBehaviour.super.stopMoving(context);
	}

	@Override
	public void tick(MovementContext context) {
		MovementBehaviour.super.tick(context);
		if (!context.world.isClientSide)
			return;
		if (!(context.temporaryData instanceof LeverAngles))
			context.temporaryData = new LeverAngles();
		LeverAngles angles = (LeverAngles) context.temporaryData;
		angles.steering.tickChaser();
		angles.speed.tickChaser();
		angles.equipAnimation.tickChaser();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
		if (!(context.temporaryData instanceof LeverAngles angles))
			return;

		AbstractContraptionEntity entity = context.contraption.entity;
		if (!(entity instanceof CarriageContraptionEntity cce))
			return;

		StructureBlockInfo info = context.contraption.getBlocks()
			.get(context.localPos);
		Direction initialOrientation = cce.getInitialOrientation()
			.getCounterClockWise();
		boolean inverted = false;
		if (info != null && info.state().hasProperty(ControlsBlock.FACING))
			inverted = !info.state().getValue(ControlsBlock.FACING)
				.equals(initialOrientation);

		if (ControlsHandler.getContraption() == entity && ControlsHandler.getControlsPos() != null
			&& ControlsHandler.getControlsPos().equals(context.localPos)) {
			Collection<Integer> pressed = ControlsHandler.currentlyPressed;
			angles.equipAnimation.chase(1, .2f, Chaser.EXP);
			angles.steering.chase((pressed.contains(3) ? 1 : 0) + (pressed.contains(2) ? -1 : 0), 0.2f, Chaser.EXP);
			float f = cce.movingBackwards ^ inverted ? -1 : 1;
			angles.speed.chase(Math.min(context.motion.length(), 0.5f) * f, 0.2f, Chaser.EXP);

		} else {
			angles.equipAnimation.chase(0, .2f, Chaser.EXP);
			angles.steering.chase(0, 0, Chaser.EXP);
			angles.speed.chase(0, 0, Chaser.EXP);
		}

		float pt = AnimationTickHolder.getPartialTicks(context.world);
		ControlsRenderer.render(context, renderWorld, matrices, buffer, angles.equipAnimation.getValue(pt),
			angles.speed.getValue(pt), angles.steering.getValue(pt));
	}

}
