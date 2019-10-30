package com.simibubi.create.modules.contraptions;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.foundation.block.CustomRenderItemBakedModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.client.event.ModelBakeEvent;

public class WrenchModel extends CustomRenderItemBakedModel {

	public IBakedModel gear;
	
	public WrenchModel(IBakedModel template) {
		super(template);
	}

	public static List<String> getCustomModelLocations() {
		return Arrays.asList("gear");
	}
	
	@Override
	public CustomRenderItemBakedModel loadPartials(ModelBakeEvent event) {
		this.gear = loadCustomModel(event, "wrench/gear");
		return this;
	}

}
