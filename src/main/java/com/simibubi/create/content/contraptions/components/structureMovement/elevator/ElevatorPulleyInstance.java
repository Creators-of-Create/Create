package com.simibubi.create.content.contraptions.components.structureMovement.elevator;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.light.TickingLightListener;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;

// TODO
public class ElevatorPulleyInstance extends ShaftInstance implements DynamicInstance, TickingLightListener {

	public ElevatorPulleyInstance(MaterialManager dispatcher, KineticTileEntity tile) {
		super(dispatcher, tile);
	}

	@Override
	public boolean tickLightListener() {
		return false;
	}

	@Override
	public void beginFrame() {
	}

}
