package com.simibubi.create.modules.curiosities.deforester;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.foundation.block.render.CustomRenderItemBakedModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.client.event.ModelBakeEvent;

public class DeforesterModel extends CustomRenderItemBakedModel {

	public IBakedModel gear;
	public IBakedModel light;
	public IBakedModel blade;
	
	public DeforesterModel(IBakedModel template) {
		super(template);
	}

	public static List<String> getCustomModelLocations() {
		return Arrays.asList("deforester/gear", "deforester/light", "deforester/blade");
	}
	
	@Override
	public CustomRenderItemBakedModel loadPartials(ModelBakeEvent event) {
		this.gear = loadCustomModel(event, "deforester/gear");
		this.light = loadCustomModel(event, "deforester/light");
		this.blade = loadCustomModel(event, "deforester/blade");
		return this;
	}

}
