package com.simibubi.create.foundation.item.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.ModelLoader;

public abstract class CustomRenderedItemModel extends BakedModelWrapper<IBakedModel> {

	protected String namespace;
	protected String basePath;
	protected Map<String, IBakedModel> partials = new HashMap<>();

	public CustomRenderedItemModel(IBakedModel template, String namespace, String basePath) {
		super(template);
		this.namespace = namespace;
		this.basePath = basePath;
	}

	@Override
	public boolean isCustomRenderer() {
		return true;
	}

	@Override
	public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
		// Super call returns originalModel, but we want to return this, else ISTER
		// won't be used.
		super.handlePerspective(cameraTransformType, mat);
		return this;
	}

	public final IBakedModel getOriginalModel() {
		return originalModel;
	}

	public IBakedModel getPartial(String name) {
		return partials.get(name);
	}

	public final List<ResourceLocation> getModelLocations() {
		return partials.keySet().stream().map(this::getPartialModelLocation).collect(Collectors.toList());
	}

	protected void addPartials(String... partials) {
		for (String name : partials)
			this.partials.put(name, null);
	}

	public void loadPartials(ModelBakeEvent event) {
		ModelLoader modelLoader = event.getModelLoader();
		for (String name : partials.keySet())
			partials.put(name, loadPartial(modelLoader, name));
	}

	@SuppressWarnings("deprecation")
	protected IBakedModel loadPartial(ModelLoader modelLoader, String name) {
		return modelLoader.bake(getPartialModelLocation(name), ModelRotation.X0_Y0);
	}

	protected ResourceLocation getPartialModelLocation(String name) {
		return new ResourceLocation(namespace, "item/" + basePath + "/" + name);
	}

}
