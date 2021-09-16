package com.simibubi.create.foundation.render;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.VirtualEmptyModelData;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class SuperByteBufferCache {

	private Map<Compartment<?>, Cache<Object, SuperByteBuffer>> cache;

	public SuperByteBufferCache() {
		cache = new HashMap<>();
		registerCompartment(Compartment.GENERIC_TILE);
		registerCompartment(Compartment.PARTIAL);
		registerCompartment(Compartment.DIRECTIONAL_PARTIAL);
	}

	public SuperByteBuffer renderBlock(BlockState toRender) {
		return getGeneric(toRender, () -> standardBlockRender(toRender));
	}

	public SuperByteBuffer renderPartial(PartialModel partial, BlockState referenceState) {
		return get(Compartment.PARTIAL, partial, () -> standardModelRender(partial.get(), referenceState));
	}

	public SuperByteBuffer renderPartial(PartialModel partial, BlockState referenceState,
										 Supplier<PoseStack> modelTransform) {
		return get(Compartment.PARTIAL, partial,
				() -> standardModelRender(partial.get(), referenceState, modelTransform.get()));
	}

	public SuperByteBuffer renderDirectionalPartial(PartialModel partial, BlockState referenceState,
													Direction dir) {
		return get(Compartment.DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
			() -> standardModelRender(partial.get(), referenceState));
	}

	public SuperByteBuffer renderDirectionalPartial(PartialModel partial, BlockState referenceState, Direction dir,
													Supplier<PoseStack> modelTransform) {
		return get(Compartment.DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
				() -> standardModelRender(partial.get(), referenceState, modelTransform.get()));
	}

	public SuperByteBuffer renderBlockIn(Compartment<BlockState> compartment, BlockState toRender) {
		return get(compartment, toRender, () -> standardBlockRender(toRender));
	}

	SuperByteBuffer getGeneric(BlockState key, Supplier<SuperByteBuffer> supplier) {
		return get(Compartment.GENERIC_TILE, key, supplier);
	}

	public <T> SuperByteBuffer get(Compartment<T> compartment, T key, Supplier<SuperByteBuffer> supplier) {
		Cache<Object, SuperByteBuffer> compartmentCache = this.cache.get(compartment);
		try {
			return compartmentCache.get(key, supplier::get);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public <T> void invalidate(Compartment<T> compartment, T key) {
		Cache<Object, SuperByteBuffer> compartmentCache = this.cache.get(compartment);
		compartmentCache.invalidate(key);
	}

	public void registerCompartment(Compartment<?> instance) {
		synchronized (cache) {
			cache.put(instance, CacheBuilder.newBuilder()
				.build());
		}
	}

	public void registerCompartment(Compartment<?> instance, long ticksUntilExpired) {
		synchronized (cache) {
			cache.put(instance, CacheBuilder.newBuilder()
				.expireAfterAccess(ticksUntilExpired * 50, TimeUnit.MILLISECONDS)
				.build());
		}
	}

	private SuperByteBuffer standardBlockRender(BlockState renderedState) {
		BlockRenderDispatcher dispatcher = Minecraft.getInstance()
			.getBlockRenderer();
		return standardModelRender(dispatcher.getBlockModel(renderedState), renderedState);
	}

	private SuperByteBuffer standardModelRender(BakedModel model, BlockState referenceState) {
		return standardModelRender(model, referenceState, new PoseStack());
	}

	private SuperByteBuffer standardModelRender(BakedModel model, BlockState referenceState, PoseStack ms) {
		BufferBuilder builder = getBufferBuilder(model, referenceState, ms);

		return new SuperByteBuffer(builder);
	}

	public static BufferBuilder getBufferBuilder(BakedModel model, BlockState referenceState, PoseStack ms) {
		Minecraft mc = Minecraft.getInstance();
		BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
		ModelBlockRenderer blockRenderer = dispatcher.getModelRenderer();
		BufferBuilder builder = new BufferBuilder(512);

		builder.begin(GL11.GL_QUADS, DefaultVertexFormat.BLOCK);
		blockRenderer.renderModel(mc.level, model, referenceState, BlockPos.ZERO.above(255), ms, builder, true,
			mc.level.random, 42, OverlayTexture.NO_OVERLAY, VirtualEmptyModelData.INSTANCE);
		builder.end();
		return builder;
	}

	public void invalidate() {
		cache.forEach((comp, cache) -> cache.invalidateAll());
	}

}
