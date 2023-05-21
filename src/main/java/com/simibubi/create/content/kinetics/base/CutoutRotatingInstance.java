package com.simibubi.create.content.kinetics.base;

import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData;
import com.simibubi.create.foundation.render.AllMaterialSpecs;

public class CutoutRotatingInstance<T extends KineticBlockEntity> extends SingleRotatingInstance<T> {
	public CutoutRotatingInstance(MaterialManager materialManager, T blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	protected Material<RotatingData> getRotatingMaterial() {
		return materialManager.defaultCutout()
				.material(AllMaterialSpecs.ROTATING);
	}
}
