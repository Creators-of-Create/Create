package com.simibubi.create.foundation.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;

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

	public static class Compartment<T> {
	}

	public static final Compartment<BlockState> GENERIC_TILE = new Compartment<>();
	public static final Compartment<AllBlockPartials> PARTIAL = new Compartment<>();
	public static final Compartment<Pair<Direction, AllBlockPartials>> DIRECTIONAL_PARTIAL = new Compartment<>();

	Map<Compartment<?>, Cache<Object, SuperByteBuffer>> cache;

	public SuperByteBufferCache() {
		cache = new HashMap<>();
		registerCompartment(GENERIC_TILE);
		registerCompartment(PARTIAL);
		registerCompartment(DIRECTIONAL_PARTIAL);
	}

	public SuperByteBuffer renderBlock(BlockState toRender) {
		return getGeneric(toRender, () -> standardBlockRender(toRender));
	}

	public SuperByteBuffer renderPartial(AllBlockPartials partial, BlockState referenceState) {
		return get(PARTIAL, partial, () -> standardModelRender(partial.get(), referenceState));
	}

	public SuperByteBuffer renderPartial(AllBlockPartials partial, BlockState referenceState,
		MatrixStack modelTransform) {
		return get(PARTIAL, partial, () -> standardModelRender(partial.get(), referenceState, modelTransform));
	}

	public SuperByteBuffer renderDirectionalPartial(AllBlockPartials partial, BlockState referenceState,
		Direction dir) {
		return get(DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
			() -> standardModelRender(partial.get(), referenceState));
	}

	public SuperByteBuffer renderDirectionalPartial(AllBlockPartials partial, BlockState referenceState, Direction dir,
		MatrixStack modelTransform) {
		return get(DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
			() -> standardModelRender(partial.get(), referenceState, modelTransform));
	}

	public SuperByteBuffer renderBlockIn(Compartment<BlockState> compartment, BlockState toRender) {
		return get(compartment, toRender, () -> standardBlockRender(toRender));
	}

	SuperByteBuffer getGeneric(BlockState key, Supplier<SuperByteBuffer> supplier) {
		return get(GENERIC_TILE, key, supplier);
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
		Minecraft mc = Minecraft.getInstance();
		BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
		BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
		BufferBuilder builder = new BufferBuilder(512);

		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		blockRenderer.renderModel(mc.world, model, referenceState, BlockPos.ZERO.up(255), ms, builder, true,
			mc.world.rand, 42, OverlayTexture.DEFAULT_UV, VirtualEmptyModelData.INSTANCE);
		builder.finishDrawing();

		return new SuperByteBuffer(builder);
	}

	public void invalidate() {
		cache.forEach((comp, cache) -> cache.invalidateAll());
	}

}
