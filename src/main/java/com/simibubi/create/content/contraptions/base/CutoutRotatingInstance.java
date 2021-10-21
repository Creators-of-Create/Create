package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.material.InstanceMaterial;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.simibubi.create.foundation.render.AllMaterialSpecs;

public class CutoutRotatingInstance extends SingleRotatingInstance {
	public CutoutRotatingInstance(MaterialManager<?> modelManager, KineticTileEntity tile) {
		super(modelManager, tile);
	}

	protected InstanceMaterial<RotatingData> getRotatingMaterial() {
		return materialManager.defaultCutout()
				.material(AllMaterialSpecs.ROTATING);
	}
}
