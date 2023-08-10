package com.simibubi.create.content.contraptions.actors.psi;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorInstance;

import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.animation.LerpedFloat;

public class PSIActorInstance extends ActorInstance {

	private final PIInstance instance;

	public PSIActorInstance(MaterialManager materialManager, VirtualRenderWorld world, MovementContext context) {
		super(materialManager, world, context);

		instance = new PIInstance(materialManager, context.state, context.localPos);

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
