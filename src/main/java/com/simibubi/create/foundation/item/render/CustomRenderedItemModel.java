package com.simibubi.create.foundation.item.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.Create;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.BakedModelWrapper;

public abstract class CustomRenderedItemModel extends BakedModelWrapper<IBakedModel> {

	protected String namespace;
	protected String basePath;
	protected Map<String, IBakedModel> partials = new HashMap<>();
	protected ItemStackTileEntityRenderer renderer;

	public CustomRenderedItemModel(IBakedModel template, String namespace, String basePath) {
		super(template);
		this.namespace = namespace;
		this.basePath = basePath;
		this.renderer = createRenderer();
	}

	@Override
	public boolean isCustomRenderer() {
		return true;
	}

	@Override
	public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
		// Super call returns originalModel, but we want to return this, else ISTER won't be used.
		super.handlePerspective(cameraTransformType, mat);
		return this;
	}

	public final IBakedModel getOriginalModel() {
		return originalModel;
	}

	public ItemStackTileEntityRenderer getRenderer() {
		return renderer;
	}

	public abstract ItemStackTileEntityRenderer createRenderer();

	public final List<ResourceLocation> getModelLocations() {
		return partials.keySet().stream().map(this::getPartialModelLocation).collect(Collectors.toList());
	}

	protected void addPartials(String... partials) {
		this.partials.clear();
		for (String name : partials)
			this.partials.put(name, null);
	}

	public CustomRenderedItemModel loadPartials(ModelBakeEvent event) {
		for (String name : partials.keySet())
			partials.put(name, loadModel(event, name));
		return this;
	}

	@SuppressWarnings("deprecation")
	private IBakedModel loadModel(ModelBakeEvent event, String name) {
		return event.getModelLoader().bake(getPartialModelLocation(name), ModelRotation.X0_Y0);
	}

	private ResourceLocation getPartialModelLocation(String name) {
		return new ResourceLocation(namespace, "item/" + basePath + "/" + name);
	}

	public IBakedModel getPartial(String name) {
		return partials.get(name);
	}

}
