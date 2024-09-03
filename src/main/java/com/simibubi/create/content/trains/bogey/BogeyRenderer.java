package com.simibubi.create.content.trains.bogey;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.VirtualRenderHelper;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.Transform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This is a port of the bogey api from Extended Bogeys, If you are looking to implement your own bogeys you can find some helpful resources below:
 * <p>
 * - <a href="https://github.com/Rabbitminers/Extended-Bogeys/tree/1.18/multiloader/dev/common/src/main/java/com/rabbitminers/extendedbogeys/bogeys/styles">Extended Bogeys (Examples)</a>
 * - <a href="https://github.com/Rabbitminers/Extended-Bogeys/blob/1.18/multiloader/dev/API_DOCS.md">Extended Bogeys (API documentation)</a>
 * - <a href="https://github.com/Layers-of-Railways/Railway/tree/93e318d1e922b1e992b89b0aceef85a2d545f370/common/src/main/java/com/railwayteam/railways/content/custom_bogeys">Steam n' Rails (Examples)</a>
 */
public abstract class BogeyRenderer {
	Map<String, BogeyModelData[]> contraptionModelData = new HashMap<>();

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
	public BogeyModelData[] getTransform(PartialModel model, PoseStack ms, boolean inInstancedContraption, int size) {
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
	public BogeyModelData[] getTransform(BlockState state, PoseStack ms, boolean inContraption, int size) {
		return inContraption ? transformContraptionModelData(keyFromModel(state), ms) : createModelData(state, size);
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
	public BogeyModelData getTransform(PartialModel model, PoseStack ms, boolean inInstancedContraption) {
		return inInstancedContraption ? contraptionModelData.get(keyFromModel(model))[0].setTransform(ms)
				: BogeyModelData.from(model);
	}

	/**
	 * A common interface for getting transform data for blockstates, for a single model
	 *
	 * @param state The state of the model to be collected or instantiated
	 * @param ms Posestack to bind the model to if it is within a contraption
	 * @param inContraption Type of model required
	 * @return A generic transform which can be used for both in-world and in-contraption models
	 */
	public BogeyModelData getTransform(BlockState state, PoseStack ms, boolean inContraption) {
		return (inContraption) ? contraptionModelData.get(keyFromModel(state))[0].setTransform(ms)
				: BogeyModelData.from(state);
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
	public abstract void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light,
								VertexConsumer vb, boolean inContraption);

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
	private BogeyModelData[] transformContraptionModelData(String key, PoseStack ms) {
		BogeyModelData[] modelData = contraptionModelData.get(key);
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
	private BogeyModelData[] createModelData(PartialModel model, int size) {
		BogeyModelData[] data = { BogeyModelData.from(model) };
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
	private BogeyModelData[] createModelData(BlockState state, int size) {
		BogeyModelData[] data = { BogeyModelData.from(state) };
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
	private BogeyModelData[] expandArrayToLength(BogeyModelData[] data, int size) {
		return Arrays.stream(Collections.nCopies(size, data).toArray())
				.flatMap(inner -> Arrays.stream((BogeyModelData[]) inner))
				.toArray(BogeyModelData[]::new);
	}

	/**
	 * Provides render implementations a point in setup to instantiate all model data to be needed
	 *
	 * @param context The visualization context
	 * @param carriageBogey The bogey to create data for
	 */
	@OnlyIn(Dist.CLIENT)
	public abstract void initialiseContraptionModelData(VisualizationContext context, CarriageBogey carriageBogey);

	/**
	 * Creates instances of models for in-world rendering to a set length from a provided partial model
	 *
	 * @param context The visualization context
	 * @param model Partial model to be instanced
	 * @param count Amount of models neeeded
	 */
	public void createModelInstance(VisualizationContext context, PartialModel model, int count) {
		var instancer = context.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, Models.partial(model));
		BogeyModelData[] modelData = IntStream.range(0, count)
				.mapToObj(i -> instancer.createInstance())
				.map(BogeyModelData::new)
				.toArray(BogeyModelData[]::new);
		contraptionModelData.put(keyFromModel(model), modelData);
	}

	/**
	 * Creates instances of models for in-contraption rendering to a set length from a provided blockstate
	 *
	 * @param context The visualization context
	 * @param state Blockstate of the model to be created
	 * @param count Amount of models needed
	 */
	public void createModelInstance(VisualizationContext context, BlockState state, int count) {
		var instancer = context.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, VirtualRenderHelper.blockModel(state));
		BogeyModelData[] modelData = IntStream.range(0, count)
				.mapToObj(i -> instancer.createInstance())
				.map(BogeyModelData::new)
				.toArray(BogeyModelData[]::new);
		contraptionModelData.put(keyFromModel(state), modelData);
	}

	/**
	 * Creates a single instance of models for in-contraption rendering from a provided blockstate
	 *
	 * @param context The visualization context
	 * @param states Blockstates of the models to be created
	 */
	public void createModelInstance(VisualizationContext context, BlockState... states) {
		for (BlockState state : states)
			this.createModelInstance(context, state, 1);
	}

	/**
	 * Helper function to create a single model instance for in-contraption rendering
	 *
	 * @param context The visualization context
	 * @param models The type of model to create instances of
	 */
	public void createModelInstance(VisualizationContext context, PartialModel... models) {
		for (PartialModel model : models)
			createModelInstance(context, model, 1);
	}

	/**
	 * This method is deprecated, use BogeyModelData#render instead, left in
	 * to avoid existing usages from crashing
	 *
	 * @param b The model data itself
	 * @param ms Pose stack to render to
	 * @param light light level of the scene
	 * @param vb Vertex Consumber to render to
	 * @param <B> Generic alias for both contraption and in-world model data
	 */

	@Deprecated
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
		for (BogeyModelData[] data : contraptionModelData.values())
			for (BogeyModelData model : data)
				model.setEmptyTransform();
	}

	/**
	 * Automatic handling for updating all model data's light
	 *
	 * @param blockLight the blocklight to be applied
	 * @param skyLight the skylight to be applied
	 */

	public void updateLight(int blockLight, int skyLight) {
		for (BogeyModelData[] data : contraptionModelData.values())
			for (BogeyModelData model : data)
				model.updateLight(blockLight, skyLight);
	}

	/**
	 * Automatic handling for clearing all model data of a contraption
	 *
	 */

	public void remove() {
		for (BogeyModelData[] data : contraptionModelData.values())
			for (BogeyModelData model : data)
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
		return partialModel.modelLocation().toString();
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

	public record BogeyModelData(Transform<?> transform) implements Transform<BogeyModelData> {
		public static BogeyModelData from(PartialModel model) {
			BlockState air = Blocks.AIR.defaultBlockState();
			return new BogeyModelData(CachedBufferer.partial(model, air));
		}
		public static BogeyModelData from(BlockState model) {
			return new BogeyModelData(CachedBufferer.block(model));
		}
		public void render(PoseStack ms, int light, @Nullable VertexConsumer vb) {
			transform.scale(1 - 1/512f);
			if (transform instanceof SuperByteBuffer byteBuf && vb != null)
				byteBuf.light(light).renderInto(ms, vb);
		}

		public BogeyModelData setTransform(PoseStack ms) {
			if (this.transform instanceof TransformedInstance model)
				model.setTransform(ms)
						.setChanged();
			return this;
		}

		public BogeyModelData setEmptyTransform() {
			if (this.transform instanceof TransformedInstance model)
				model.setZeroTransform()
						.setChanged();
			return this;
		}

		public BogeyModelData delete() {
			if (this.transform instanceof TransformedInstance model)
				model.delete();
			return this;
		}

		public BogeyModelData updateLight(int blockLight, int skyLight) {
			if (this.transform instanceof TransformedInstance model)
				model.light(blockLight, skyLight)
						.setChanged();
			return this;
		}

		@Override
		public BogeyModelData mulPose(Matrix4fc pose) {
			this.transform.mulPose(pose);
			return this;
		}

		@Override
		public BogeyModelData mulNormal(Matrix3fc normal) {
			this.transform.mulNormal(normal);
			return this;
		}

		@Override
		public BogeyModelData rotate(Quaternionfc quaternion) {
			this.transform.rotate(quaternion);
			return this;
		}

		@Override
		public BogeyModelData scale(float factorX, float factorY, float factorZ) {
			this.transform.scale(factorX, factorY, factorZ);
			return this;
		}

		@Override
		public BogeyModelData translate(float x, float y, float z) {
			this.transform.translate(x, y, z);
			return this;
		}
	}
}
