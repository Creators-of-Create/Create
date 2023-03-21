package com.simibubi.create.content.logistics.trains;

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

public abstract class BogeyRenderer {
	Map<BogeySize, Renderer> renderers = new EnumMap<>(BogeySize.class);
	Map<PartialModel, ModelData[]> contraptionModelData = new HashMap<>();

	public Transform<?>[] getTransformsFromPartial(PartialModel model, boolean inContraption, int size) {
		return inContraption ? contraptionModelData.get(model) : createModelData(model, size);
	}

	private Transform<?>[] createModelData(PartialModel model, int size) {
		BlockState air = Blocks.AIR.defaultBlockState();
		SuperByteBuffer[] data = { CachedBufferer.partial(model, air) };
		return Arrays.stream(Collections.nCopies(size, data).toArray())
				.flatMap(inner -> Arrays.stream((SuperByteBuffer[]) inner))
				.toArray(SuperByteBuffer[]::new);
	}

	public Transform<?> getTransformFromPartial(PartialModel model, boolean inContraption) {
		BlockState air = Blocks.AIR.defaultBlockState();
		return inContraption ? contraptionModelData.get(model)[0]
				: CachedBufferer.partial(model, air);
	}

	@OnlyIn(Dist.CLIENT)
	public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, @Nullable VertexConsumer vb,
					   BogeySize size) {
		renderers.get(size).render(bogeyData, wheelAngle, ms, light, vb);
	}

	@OnlyIn(Dist.CLIENT)
	public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, BogeySize size) {
		this.render(bogeyData, wheelAngle, ms, 0, null, size);
	}

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

	@FunctionalInterface
	interface Renderer {
		void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb);
	}

	// TODO: REPLACE THIS SO THAT IT CAN BE ADDED TO
	public enum BogeySize {
		SMALL, LARGE;
	}
}
