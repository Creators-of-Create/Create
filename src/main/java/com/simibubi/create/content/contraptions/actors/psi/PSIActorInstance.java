package com.simibubi.create.content.contraptions.actors.psi;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

public class PSIActorInstance extends ActorInstance {

	private final PIInstance instance;

	public PSIActorInstance(VisualizationContext materialManager, VirtualRenderWorld world, MovementContext context) {
		super(materialManager, world, context);

		instance = new PIInstance(materialManager.instancerProvider(), context.state, context.localPos);

		instance.init(false);
		instance.middle.setBlockLight(localBlockLight());
		instance.top.setBlockLight(localBlockLight());
	}

	@Override
	public void beginFrame() {
		LerpedFloat lf = PortableStorageInterfaceMovement.getAnimation(context);
		instance.tick(lf.settled());
		instance.beginFrame(lf.getValue(AnimationTickHolder.getPartialTicks()));
	}

}
