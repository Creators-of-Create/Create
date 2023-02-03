package com.simibubi.create.foundation.item.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;

public abstract class CustomRenderedItemModel extends BakedModelWrapper<BakedModel> {

	protected String namespace;
	protected String basePath;
	protected Map<String, BakedModel> partials = new HashMap<>();

	public CustomRenderedItemModel(BakedModel template, String namespace, String basePath) {
		super(template);
		this.namespace = namespace;
		this.basePath = basePath;
	}

	@Override
	public boolean isCustomRenderer() {
		return true;
	}

	@Override
	public BakedModel applyTransform(ItemTransforms.TransformType cameraTransformType, PoseStack mat, boolean leftHand) {
		// Super call returns originalModel, but we want to return this, else ISTER
		// won't be used.
		super.applyTransform(cameraTransformType, mat, leftHand);
		return this;
	}

	public final BakedModel getOriginalModel() {
		return originalModel;
	}

	public BakedModel getPartial(String name) {
		return partials.get(name);
	}

	public final List<ResourceLocation> getModelLocations() {
		return partials.keySet().stream().map(this::getPartialModelLocation).collect(Collectors.toList());
	}

	protected void addPartials(String... partials) {
		for (String name : partials)
			this.partials.put(name, null);
	}

	public void loadPartials(ModelEvent.ModifyBakingResult event) {
		Map<ResourceLocation, BakedModel> models = event.getModels();
		for (String name : partials.keySet())
			partials.put(name, loadPartial(models, name));
	}

	protected BakedModel loadPartial(Map<ResourceLocation, BakedModel> models, String name) {
		return models.get(getPartialModelLocation(name));
	}

	protected ResourceLocation getPartialModelLocation(String name) {
		return new ResourceLocation(namespace, "item/" + basePath + "/" + name);
	}

}
