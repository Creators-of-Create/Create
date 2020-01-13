package com.simibubi.create.modules.curiosities.symmetry.client;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.foundation.block.render.CustomRenderItemBakedModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.client.event.ModelBakeEvent;

public class SymmetryWandModel extends CustomRenderItemBakedModel {

	public IBakedModel core;
	public IBakedModel bits;
	
	public SymmetryWandModel(IBakedModel template) {
		super(template);
	}

	public static List<String> getCustomModelLocations() {
		return Arrays.asList("symmetry_wand_core", "symmetry_wand_bits");
	}
	
	@Override
	public CustomRenderItemBakedModel loadPartials(ModelBakeEvent event) {
		this.core = loadCustomModel(event, "symmetry_wand_core");
		this.bits = loadCustomModel(event, "symmetry_wand_bits");
		return this;
	}

}
