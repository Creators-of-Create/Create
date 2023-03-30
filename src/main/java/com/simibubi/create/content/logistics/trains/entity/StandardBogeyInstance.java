package com.simibubi.create.content.logistics.trains.entity;

import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.logistics.trains.BogeyRenderer;
import com.simibubi.create.content.logistics.trains.StandardBogeyRenderer;

public class StandardBogeyInstance extends BogeyInstance {
	public StandardBogeyInstance(CarriageBogey bogey, BogeyRenderer.BogeySize bogeySize, MaterialManager materialManager) {
		super(bogey, new StandardBogeyRenderer(), bogeySize, materialManager);
	}

	public static StandardBogeyInstance drive(CarriageBogey bogey, MaterialManager materialManager) {
		return new StandardBogeyInstance(bogey, BogeyRenderer.BogeySize.LARGE, materialManager);
	}


	public static StandardBogeyInstance frame(CarriageBogey bogey, MaterialManager materialManager) {
		return new StandardBogeyInstance(bogey, BogeyRenderer.BogeySize.SMALL, materialManager);
	}

	@Override
	public BogeyInstanceFactory getInstanceFactory() {
		return StandardBogeyInstance::new;
	}
}
