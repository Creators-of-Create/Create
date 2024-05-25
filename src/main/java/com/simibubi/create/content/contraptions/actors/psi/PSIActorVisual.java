package com.simibubi.create.content.contraptions.actors.psi;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

public class PSIActorVisual extends ActorVisual {

	private final PIInstance instance;

	public PSIActorVisual(VisualizationContext context, VirtualRenderWorld world, MovementContext movementContext) {
		super(context, world, movementContext);

		instance = new PIInstance(context.instancerProvider(), movementContext.state, movementContext.localPos);

		instance.init(false);
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

	@Override
	public void init(float partialTick) {

	}
}
