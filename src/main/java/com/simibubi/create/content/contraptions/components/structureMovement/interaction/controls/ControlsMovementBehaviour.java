package com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls;

import java.util.Collection;

import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.logistics.trains.entity.Carriage;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ControlsMovementBehaviour extends MovementBehaviour {

	// TODO: this is specific to Carriage Contraptions - need to move this behaviour
	// there
	LerpedFloat steering = LerpedFloat.linear();
	LerpedFloat speed = LerpedFloat.linear();
	LerpedFloat equipAnimation = LerpedFloat.linear();

	@Override
	public void tick(MovementContext context) {
		steering.tickChaser();
		speed.tickChaser();
		equipAnimation.tickChaser();
		super.tick(context);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
		if (ControlsHandler.entityRef.get() == context.contraption.entity && ControlsHandler.controlsPos != null
			&& ControlsHandler.controlsPos.equals(context.localPos)) {
			Collection<Integer> pressed = ControlsHandler.currentlyPressed;
			equipAnimation.chase(1, .2f, Chaser.EXP);
			steering.chase((pressed.contains(3) ? 1 : 0) + (pressed.contains(2) ? -1 : 0), 0.2f, Chaser.EXP);
			speed.chase(0, 0.2f, Chaser.EXP); // TODO
		} else
			equipAnimation.chase(0, .2f, Chaser.EXP);
		float pt = AnimationTickHolder.getPartialTicks(context.world);
		ControlsRenderer.render(context, renderWorld, matrices, buffer, equipAnimation.getValue(pt), speed.getValue(pt),
			steering.getValue(pt));
	}

}
