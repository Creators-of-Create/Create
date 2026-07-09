package com.simibubi.create.content.contraptions.actors.psi;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.animation.LerpedFloat;

public class PSIActorVisual extends ActorVisual {

	private final PIInstance instance;

	public PSIActorVisual(VisualizationContext context, VirtualRenderWorld world, MovementContext movementContext) {
		super(context, movementContext.world, movementContext);

		instance = new PIInstance(context.instancerProvider(), movementContext.state, movementContext.localPos, false);

		instance.middle.light(localBlockLight(), 0);
		instance.top.light(localBlockLight(), 0);
	}

	@Override
	public void beginFrame() {
		LerpedFloat lf = PortableStorageInterfaceMovement.getAnimation(context);
		instance.tick(lf.settled());
		instance.beginFrame(lf.getValue(AnimationTickHolder.getPartialTicks()));
	}

	@Override
	protected void _delete() {
		instance.remove();
	}
}
