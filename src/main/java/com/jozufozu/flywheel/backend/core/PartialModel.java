package com.jozufozu.flywheel.backend.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;

/**
 * A helper class for loading and accessing json models.
 * <p>
 * Creating a PartialModel will make the associated modelLocation automatically load.
 * As such, PartialModels must be initialized at or before {@link ModelRegistryEvent}.
 * Once {@link ModelBakeEvent} finishes, all PartialModels (with valid modelLocations)
 * will have their bakedModel fields populated.
 * <p>
 * Attempting to create a PartialModel after ModelRegistryEvent will cause an error.
 */
public class PartialModel {

	private static boolean tooLate = false;
	private static final List<PartialModel> all = new ArrayList<>();

	protected final ResourceLocation modelLocation;
	protected IBakedModel bakedModel;

	public PartialModel(ResourceLocation modelLocation) {

		if (tooLate) throw new RuntimeException("PartialModel '" + modelLocation + "' loaded after ModelRegistryEvent");

		this.modelLocation = modelLocation;
		all.add(this);
	}

	public static void onModelRegistry(ModelRegistryEvent event) {
		for (PartialModel partial : all)
			ModelLoader.addSpecialModel(partial.modelLocation);

		tooLate = true;
	}

	public static void onModelBake(ModelBakeEvent event) {
		Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
		for (PartialModel partial : all)
			partial.bakedModel = modelRegistry.get(partial.modelLocation);
	}

	public IBakedModel get() {
		return bakedModel;
	}

}
