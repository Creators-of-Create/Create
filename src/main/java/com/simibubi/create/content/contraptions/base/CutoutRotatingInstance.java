package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;
import com.simibubi.create.foundation.render.AllMaterialSpecs;

public class CutoutRotatingInstance extends SingleRotatingInstance {
	public CutoutRotatingInstance(MaterialManager materialManager, KineticBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	protected Material<RotatingData> getRotatingMaterial() {
		return materialManager.defaultCutout()
				.material(AllMaterialSpecs.ROTATING);
	}
}
