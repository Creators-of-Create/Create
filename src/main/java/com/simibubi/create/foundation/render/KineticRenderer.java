package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.backend.core.BasicInstancedTileRenderer;
import com.jozufozu.flywheel.backend.instancing.RenderMaterial;
import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.contraptions.base.RotatingModel;
import com.simibubi.create.content.contraptions.relays.belt.BeltInstancedModel;
import com.simibubi.create.content.logistics.block.FlapModel;

public class KineticRenderer extends BasicInstancedTileRenderer {

	@Override
	public void registerMaterials() {
		super.registerMaterials();

		materials.put(KineticRenderMaterials.BELTS,
				new RenderMaterial<>(this, AllProgramSpecs.BELT, BeltInstancedModel::new));
		materials.put(KineticRenderMaterials.ROTATING,
				new RenderMaterial<>(this, AllProgramSpecs.ROTATING, RotatingModel::new));
		materials.put(KineticRenderMaterials.FLAPS, new RenderMaterial<>(this, AllProgramSpecs.FLAPS, FlapModel::new));
	}
}
