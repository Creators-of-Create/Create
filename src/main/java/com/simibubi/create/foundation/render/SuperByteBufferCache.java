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
import com.jozufozu.flywheel.backend.core.PartialModel;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.VirtualEmptyModelData;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class SuperByteBufferCache {

	Map<Compartment<?>, Cache<Object, SuperByteBuffer>> cache;

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
										 Supplier<MatrixStack> modelTransform) {
		return get(Compartment.PARTIAL, partial,
				() -> standardModelRender(partial.get(), referenceState, modelTransform.get()));
	}

	public SuperByteBuffer renderDirectionalPartial(PartialModel partial, BlockState referenceState,
													Direction dir) {
		return get(Compartment.DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
			() -> standardModelRender(partial.get(), referenceState));
	}

	public SuperByteBuffer renderDirectionalPartial(PartialModel partial, BlockState referenceState, Direction dir,
													Supplier<MatrixStack> modelTransform) {
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
		cache.put(instance, CacheBuilder.newBuilder()
			.build());
	}

	public void registerCompartment(Compartment<?> instance, long ticksUntilExpired) {
		cache.put(instance, CacheBuilder.newBuilder()
			.expireAfterAccess(ticksUntilExpired * 50, TimeUnit.MILLISECONDS)
			.build());
	}

	private SuperByteBuffer standardBlockRender(BlockState renderedState) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance()
			.getBlockRendererDispatcher();
		return standardModelRender(dispatcher.getModelForState(renderedState), renderedState);
	}

	private SuperByteBuffer standardModelRender(IBakedModel model, BlockState referenceState) {
		return standardModelRender(model, referenceState, new MatrixStack());
	}

	private SuperByteBuffer standardModelRender(IBakedModel model, BlockState referenceState, MatrixStack ms) {
		BufferBuilder builder = getBufferBuilder(model, referenceState, ms);

		return new SuperByteBuffer(builder);
	}

	public static BufferBuilder getBufferBuilder(IBakedModel model, BlockState referenceState, MatrixStack ms) {
		Minecraft mc = Minecraft.getInstance();
		BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
		BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
		BufferBuilder builder = new BufferBuilder(512);

		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		blockRenderer.renderModel(mc.world, model, referenceState, BlockPos.ZERO.up(255), ms, builder, true,
			mc.world.rand, 42, OverlayTexture.DEFAULT_UV, VirtualEmptyModelData.INSTANCE);
		builder.finishDrawing();
		return builder;
	}

	public void invalidate() {
		cache.forEach((comp, cache) -> cache.invalidateAll());
	}

}
