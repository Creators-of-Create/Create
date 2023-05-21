package com.simibubi.create.content.trains.bogey;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.Transform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class BogeyRenderer {
	Map<String, ModelData[]> contraptionModelData = new HashMap<>();

	/**
	 * A common interface for getting transform data for both in-world and in-contraption model data safely from a
	 * partial model
	 *
	 * @param model The key for the model data to instantiate or retrieve
	 * @param ms The posestack used for contraption model data
	 * @param inInstancedContraption The type of model needed
	 * @param size The amount of models needed
	 * @return A generic transform which can be used for both in-world and in-contraption models
	 */
	public Transform<?>[] getTransformsFromPartial(PartialModel model, PoseStack ms, boolean inInstancedContraption, int size) {
		return (inInstancedContraption) ? transformContraptionModelData(keyFromModel(model), ms) : createModelData(model, size);
	}

	/**
	 * A common interface for getting transform data for both in-world and in-contraption model data safely from a
	 * blockstate
	 *
	 * @param state The key for the model data to instantiate or retrieve
	 * @param ms The posestack used for contraption model data
	 * @param inContraption The type of model needed
	 * @param size The amount of models needed
	 * @return A generic transform which can be used for both in-world and in-contraption models
	 */
	public Transform<?>[] getTransformsFromBlockState(BlockState state, PoseStack ms, boolean inContraption, int size) {
		return inContraption ? transformContraptionModelData(keyFromModel(state), ms) : createModelData(state, size);
	}

	/**
	 * Used for calling both in-world and in-contraption rendering
	 *
	 * @param bogeyData Custom data stored on the bogey able to be used for rendering
	 * @param wheelAngle The angle of the wheel
	 * @param ms The posestack to render to
	 * @param light (Optional) Light used for in-world rendering
	 * @param vb (Optional) Vertex Consumer used for in-world rendering
	 */
	@OnlyIn(Dist.CLIENT)
	public abstract void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb, boolean inContraption);

	/**
	 * Used for calling in-contraption rendering ensuring that falsey data is handled correctly
	 *
	 * @param bogeyData Custom data stored on the bogey able to be used for rendering
	 * @param wheelAngle The angle of the wheel
	 * @param ms The posestack to render to
	 */
	@OnlyIn(Dist.CLIENT)
	public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms) {
		this.render(bogeyData, wheelAngle, ms, 0, null, true);
	}

	public abstract BogeySizes.BogeySize getSize();

	/**
	 * Used to collect Contraption Model Data for in-contraption rendering, should not be utilised directly when
	 * rendering to prevent render type mismatch
	 *
	 * @param key The key used to access the model
	 * @param ms Posestack of the contraption to bind the model data to
	 * @return A generic transform which can be used for both in-world and in-contraption models
	 */
	private Transform<?>[] transformContraptionModelData(String key, PoseStack ms) {
		ModelData[] modelData = contraptionModelData.get(key);
		Arrays.stream(modelData).forEach(modelDataElement -> modelDataElement.setTransform(ms));
		return modelData;
	}


	/**
	 * Used for in world rendering, creates a set count of model data to be rendered, allowing for a generic response
	 * when rendering multiple models both in-world and in-contraption for example, with wheels
	 *
	 * @param model The partial model of the model data ot be made
	 * @param size The Amount of models needed
	 * @return A generic transform which can be used for both in-world and in-contraption models
	 */
	private Transform<?>[] createModelData(PartialModel model, int size) {
		BlockState air = Blocks.AIR.defaultBlockState();
		SuperByteBuffer[] data = { CachedBufferer.partial(model, air) };
		return expandArrayToLength(data, size);
	}

	/**
	 * Used for in world rendering, creates a set count of model data to be rendered, allowing for a generic response
	 * when rendering multiple models both in-world and in-contraption for example, with wheels
	 *
	 * @param state The state of the model data to be made
	 * @param size Amount of models needed
	 * @return A generic transform which can be used for both in-world and in-contraption models
	 */
	private Transform<?>[] createModelData(BlockState state, int size) {
		SuperByteBuffer[] data = { CachedBufferer.block(state) };
		return expandArrayToLength(data, size);
	}

	/**
	 * Utility function to clone in-world models to a set size to allow for common handling of rendering with multiple
	 * instances of the same model for example with wheels
	 *
	 * @param data An in-world model to be replicated
	 * @param size Amount of models needed
	 * @return A generic transform which can be used for both in-world and in-contraption models
	 */
	private Transform<?>[] expandArrayToLength(SuperByteBuffer[] data, int size) {
		return Arrays.stream(Collections.nCopies(size, data).toArray())
				.flatMap(inner -> Arrays.stream((SuperByteBuffer[]) inner))
				.toArray(SuperByteBuffer[]::new);
	}

	/**
	 * Helper function to collect or create a single model from a partial model used for both in-world and
	 * in-contraption rendering
	 *
 	 * @param model The key of the model to be collected or instantiated
	 * @param ms Posestack to bind the model to if it is within a contraption
	 * @param inInstancedContraption Type of rendering required
	 * @return A generic transform which can be used for both in-world and in-contraption models
	 */
	public Transform<?> getTransformFromPartial(PartialModel model, PoseStack ms, boolean inInstancedContraption) {
		BlockState air = Blocks.AIR.defaultBlockState();
		return inInstancedContraption ? contraptionModelData.get(keyFromModel(model))[0].setTransform(ms)
				: CachedBufferer.partial(model, air);
	}

	/**
	 * A common interface for getting transform data for blockstates, for a single model
	 *
	 * @param state The state of the model to be collected or instantiated
	 * @param ms Posestack to bind the model to if it is within a contraption
	 * @param inContraption Type of model required
	 * @return A generic transform which can be used for both in-world and in-contraption models
	 */
	public Transform<?> getTransformFromBlockState(BlockState state, PoseStack ms, boolean inContraption) {
		return (inContraption) ? contraptionModelData.get(keyFromModel(state))[0].setTransform(ms)
				: CachedBufferer.block(state);
	}

	/**
	 * Provides render implementations a point in setup to instantiate all model data to be needed
	 *
	 * @param materialManager The material manager
	 */
	@OnlyIn(Dist.CLIENT)
	public abstract void initialiseContraptionModelData(MaterialManager materialManager);

	/**
	 * Creates instances of models for in-world rendering to a set length from a provided partial model
	 *
	 * @param materialManager The material manager
	 * @param model Partial model to be instanced
	 * @param count Amount of models neeeded
	 */
	public void createModelInstances(MaterialManager materialManager, PartialModel model, int count) {
		ModelData[] modelData = new ModelData[count];
		materialManager.defaultSolid().material(Materials.TRANSFORMED)
				.getModel(model).createInstances(modelData);
		contraptionModelData.put(keyFromModel(model), modelData);
	}

	/**
	 * Creates instances of models for in-contraption rendering to a set length from a provided blockstate
	 *
	 * @param materialManager The material manager
	 * @param state Blockstate of the model to be created
	 * @param count Amount of models needed
	 */
	public void createModelInstances(MaterialManager materialManager, BlockState state, int count) {
		ModelData[] modelData = new ModelData[count];
		materialManager.defaultSolid().material(Materials.TRANSFORMED)
				.getModel(state).createInstances(modelData);
		contraptionModelData.put(keyFromModel(state), modelData);
	}

	/**
	 * Creates a single instance of models for in-contraption rendering from a provided blockstate
	 *
	 * @param materialManager The material manager
	 * @param state Blockstate of the model to be created
	 */
	public void createModelInstance(MaterialManager materialManager, BlockState state) {
		this.createModelInstances(materialManager, state, 1);
	}

	/**
	 * Helper function to create a single model instance for in-contraption rendering
	 *
	 * @param materialManager The material manager
	 * @param models The type of model to create instances of
	 */
	public void createModelInstances(MaterialManager materialManager, PartialModel... models) {
		for (PartialModel model : models)
			createModelInstances(materialManager, model, 1);
	}

	/**
	 * Handles scale for all model data and renders non contraption model data
	 *
	 * @param b The model data itself
	 * @param ms Pose stack to render to
	 * @param light light level of the scene
	 * @param vb Vertex Consumber to render to
	 * @param <B> Generic alias for both contraption and in-world model data
	 */

	public static <B extends Transform<?>> void finalize(B b, PoseStack ms, int light, @Nullable VertexConsumer vb) {
		b.scale(1 - 1/512f);
		if (b instanceof SuperByteBuffer byteBuf && vb != null)
			byteBuf.light(light).renderInto(ms, vb);
	}

	/**
	 * Automatic handling for setting empty transforms for all model data
	 *
	 */

	public void emptyTransforms() {
		for (ModelData[] data : contraptionModelData.values())
			for (ModelData model : data)
				model.setEmptyTransform();
	}

	/**
	 * Automatic handling for updating all model data's light
	 *
	 * @param blockLight the blocklight to be applied
	 * @param skyLight the skylight to be applied
	 */

	public void updateLight(int blockLight, int skyLight) {
		for (ModelData[] data : contraptionModelData.values())
			for (ModelData model : data)
				model.setBlockLight(blockLight).setSkyLight(skyLight);
	}

	/**
	 * Automatic handling for clearing all model data of a contraption
	 *
	 */

	public void remove() {
		for (ModelData[] data : contraptionModelData.values())
			for (ModelData model : data)
				model.delete();
		contraptionModelData.clear();
	}

	/**
	 * Create a model key from a partial model, so it can be easily accessed
	 *
	 * @param partialModel the model we want a unique key for
	 * @return Key of the model
	 */

	private String keyFromModel(PartialModel partialModel) {
		return partialModel.getLocation().toString();
	}

	/**
	 * Create a model key from a blockstate, so it can be easily accessed
	 *
	 * @param state Blockstate of the model
	 * @return Key of the model
	 */

	private String keyFromModel(BlockState state) {
		return state.toString();
	}

	public static abstract class CommonRenderer extends BogeyRenderer {
		@Override
		public BogeySizes.BogeySize getSize() {
			return null;
		}
	}
}
