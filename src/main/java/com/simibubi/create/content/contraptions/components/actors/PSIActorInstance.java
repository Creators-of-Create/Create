package com.simibubi.create.content.contraptions.components.actors;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

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
		PortableStorageInterfaceTileEntity psi = PortableStorageInterfaceRenderer.getTargetPSI(context);
		instance.tick(psi != null && psi.isConnected());
		instance.beginFrame(psi == null ? 0f : psi.getExtensionDistance(AnimationTickHolder.getPartialTicks()));
	}

}
