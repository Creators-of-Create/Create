package com.simibubi.create.modules.symmetry.client;

import com.simibubi.create.foundation.block.CustomRenderItemBakedModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.client.event.ModelBakeEvent;

public class SymmetryWandModel extends CustomRenderItemBakedModel {

	public IBakedModel core;
	public IBakedModel bits;
	
	public SymmetryWandModel(IBakedModel template) {
		super(template);
	}

	@Override
	public CustomRenderItemBakedModel loadPartials(ModelBakeEvent event) {
		this.core = loadCustomModel(event, "symmetry_wand_core");
		this.bits = loadCustomModel(event, "symmetry_wand_bits");
		return this;
	}

}
