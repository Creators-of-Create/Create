package com.simibubi.create.content.logistics.trains;

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

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * Proof of concept implementation of a common and generic bogey render
 * - Model data is handled as a generic Transform which allows them to be modified in a common manner and then
 * 	"finalised" seperately
 * - Model instances (used for contraptions) are stored in a map keyed by the partial type, this should probably
 * 	be replaced as it has a few drawbacks notably all model data is the same between sizes for the same type
 * - Lighting and removal is automatically handled by collecting values from the map
 * - Renderers are stored by type so they can be easily added or removed for specific sizes without individual
 * 	overriden methods for each size
 */

// Seperate From BogeyInstance So It Can Be Used Inworld

public abstract class BogeyRenderer {
	Map<BogeySize, Renderer> renderers = new EnumMap<>(BogeySize.class);
	Map<String, ModelData[]> contraptionModelData = new HashMap<>();

	public Transform<?>[] getTransformsFromPartial(PartialModel model, PoseStack ms, boolean inContraption, int size) {
		return (inContraption) ? transformContraptionModelData(keyFromModel(model), ms) : createModelData(model, size);
	}

	public Transform<?>[] getTransformsFromBlockState(BlockState state, PoseStack ms, boolean inContraption, int size) {
		return (inContraption) ? transformContraptionModelData(keyFromModel(state), ms) : createModelData(state, size);
	}

	private Transform<?>[] transformContraptionModelData(String key, PoseStack ms) {
		ModelData[] modelData = contraptionModelData.get(key);
		Arrays.stream(modelData).forEach(modelDataElement -> modelDataElement.setTransform(ms));
		return modelData;
	}

	private Transform<?>[] createModelData(PartialModel model, int size) {
		BlockState air = Blocks.AIR.defaultBlockState();
		SuperByteBuffer[] data = { CachedBufferer.partial(model, air) };
		return expandArrayToLength(data, size);
	}

	private Transform<?>[] createModelData(BlockState state, int size) {
		SuperByteBuffer[] data = { CachedBufferer.block(state) };
		return expandArrayToLength(data, size);
	}

	private Transform<?>[] expandArrayToLength(SuperByteBuffer[] data, int size) {
		return Arrays.stream(Collections.nCopies(size, data).toArray())
				.flatMap(inner -> Arrays.stream((SuperByteBuffer[]) inner))
				.toArray(SuperByteBuffer[]::new);
	}

	public Transform<?> getTransformFromPartial(PartialModel model, PoseStack ms, boolean inContraption) {
		BlockState air = Blocks.AIR.defaultBlockState();
		return inContraption ? contraptionModelData.get(keyFromModel(model))[0].setTransform(ms)
				: CachedBufferer.partial(model, air);
	}

	@OnlyIn(Dist.CLIENT)
	public abstract void initialiseContraptionModelData(MaterialManager materialManager, BogeySize size);

	public void createModelInstances(MaterialManager materialManager, PartialModel model, int count) {
		ModelData[] modelData = new ModelData[count];
		materialManager.defaultSolid().material(Materials.TRANSFORMED)
				.getModel(model).createInstances(modelData);
		contraptionModelData.put(keyFromModel(model), modelData);
	}

	public void createModelInstances(MaterialManager materialManager, BlockState state, int count) {
		ModelData[] modelData = new ModelData[count];
		materialManager.defaultSolid().material(Materials.TRANSFORMED)
				.getModel(state).createInstances(modelData);
		contraptionModelData.put(keyFromModel(state), modelData);
	}

	public void createModelInstances(MaterialManager materialManager, PartialModel... models) {
		for (PartialModel model : models)
			createModelInstances(materialManager, model, 1);
	}

	@OnlyIn(Dist.CLIENT)
	public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, @Nullable VertexConsumer vb,
					   BogeySize size) {
		renderCommon(bogeyData, wheelAngle, ms, light, vb);
		renderers.get(size).render(bogeyData, wheelAngle, ms, light, vb);
	}

	@OnlyIn(Dist.CLIENT)
	public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, BogeySize size) {
		this.render(bogeyData, wheelAngle, ms, 0, null, size);
	}

	@OnlyIn(Dist.CLIENT)
	public abstract void renderCommon(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light,
									  @Nullable VertexConsumer vb);

	public static <B extends Transform<?>> void finalize(B b, PoseStack ms, int light, @Nullable VertexConsumer vb) {
		b.scale(1 - 1/512f);
		if (b instanceof SuperByteBuffer byteBuf && vb != null)
			byteBuf.light(light).renderInto(ms, vb);
	}

	public boolean styleImplementsSize(BogeySize size) {
		return this.renderers.containsKey(size);
	}

	public Set<BogeySize> implementedSizes() {
		return renderers.keySet();
	}

	public void emptyTransforms() {
		for (ModelData[] data : contraptionModelData.values())
			for (ModelData model : data)
				model.setEmptyTransform();
	}

	public void updateLight(int blockLight, int skyLight) {
		for (ModelData[] data : contraptionModelData.values())
			for (ModelData model : data)
				model.setBlockLight(blockLight).setSkyLight(skyLight);
	}

	public void remove() {
		for (ModelData[] data : contraptionModelData.values())
			for (ModelData model : data)
				model.delete();
		contraptionModelData.clear();
	}

	private String keyFromModel(PartialModel partialModel) {
		return partialModel.getLocation().toString();
	}

	private String keyFromModel(BlockState state) {
		return state.toString();
	}

	@FunctionalInterface
	interface Renderer {
		void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb);
	}

	// TODO: REPLACE THIS SO THAT IT CAN BE ADDED TO
	public enum BogeySize {
		SMALL, LARGE;
	}
}
