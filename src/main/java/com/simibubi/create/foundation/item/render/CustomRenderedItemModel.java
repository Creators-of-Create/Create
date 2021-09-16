package com.simibubi.create.foundation.item.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.BakedModelWrapper;

public abstract class CustomRenderedItemModel extends BakedModelWrapper<BakedModel> {

	protected String namespace;
	protected String basePath;
	protected Map<String, BakedModel> partials = new HashMap<>();
	protected BlockEntityWithoutLevelRenderer renderer;

	public CustomRenderedItemModel(BakedModel template, String namespace, String basePath) {
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
	public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack mat) {
		// Super call returns originalModel, but we want to return this, else ISTER won't be used.
		super.handlePerspective(cameraTransformType, mat);
		return this;
	}

	public final BakedModel getOriginalModel() {
		return originalModel;
	}

	public BlockEntityWithoutLevelRenderer getRenderer() {
		return renderer;
	}

	public abstract BlockEntityWithoutLevelRenderer createRenderer();

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
	private BakedModel loadModel(ModelBakeEvent event, String name) {
		return event.getModelLoader().bake(getPartialModelLocation(name), BlockModelRotation.X0_Y0);
	}

	private ResourceLocation getPartialModelLocation(String name) {
		return new ResourceLocation(namespace, "item/" + basePath + "/" + name);
	}

	public BakedModel getPartial(String name) {
		return partials.get(name);
	}

}
