package com.simibubi.create.foundation.render;

import java.util.SortedMap;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.ModelBakery;

public class SuperRenderTypeBuffer implements MultiBufferSource {

	static SuperRenderTypeBuffer instance;

	public static SuperRenderTypeBuffer getInstance() {
		if (instance == null)
			instance = new SuperRenderTypeBuffer();
		return instance;
	}

	SuperRenderTypeBufferPhase earlyBuffer;
	SuperRenderTypeBufferPhase defaultBuffer;
	SuperRenderTypeBufferPhase lateBuffer;

	public SuperRenderTypeBuffer() {
		earlyBuffer = new SuperRenderTypeBufferPhase();
		defaultBuffer = new SuperRenderTypeBufferPhase();
		lateBuffer = new SuperRenderTypeBufferPhase();
	}

	public VertexConsumer getEarlyBuffer(RenderType type) {
		return earlyBuffer.getBuffer(type);
	}

	@Override
	public VertexConsumer getBuffer(RenderType type) {
		return defaultBuffer.getBuffer(type);
	}

	public VertexConsumer getLateBuffer(RenderType type) {
		return lateBuffer.getBuffer(type);
	}

	public void draw() {
		earlyBuffer.endBatch();
		defaultBuffer.endBatch();
		lateBuffer.endBatch();
	}

	public void draw(RenderType type) {
		earlyBuffer.endBatch(type);
		defaultBuffer.endBatch(type);
		lateBuffer.endBatch(type);
	}

	private static class SuperRenderTypeBufferPhase extends MultiBufferSource.BufferSource {

		// Visible clones from net.minecraft.client.renderer.RenderTypeBuffers
		static final ChunkBufferBuilderPack blockBuilders = new ChunkBufferBuilderPack();

		static final SortedMap<RenderType, BufferBuilder> createEntityBuilders() {
			return Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) -> {
				map.put(Sheets.solidBlockSheet(), blockBuilders.builder(RenderType.solid()));
				assign(map, RenderTypes.getOutlineSolid());
				map.put(Sheets.cutoutBlockSheet(), blockBuilders.builder(RenderType.cutout()));
				map.put(Sheets.bannerSheet(), blockBuilders.builder(RenderType.cutoutMipped()));
				map.put(Sheets.translucentCullBlockSheet(), blockBuilders.builder(RenderType.translucent())); // FIXME new equivalent of getEntityTranslucent() ?
				assign(map, Sheets.shieldSheet());
				assign(map, Sheets.bedSheet());
				assign(map, Sheets.shulkerBoxSheet());
				assign(map, Sheets.signSheet());
				assign(map, Sheets.chestSheet());
				assign(map, RenderType.translucentNoCrumbling());
				assign(map, RenderType.glint());
				assign(map, RenderType.entityGlint());
				assign(map, RenderType.waterMask());
				ModelBakery.DESTROY_TYPES.forEach((p_228488_1_) -> {
					assign(map, p_228488_1_);
				});
			});
		}

		private static void assign(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> map, RenderType type) {
			map.put(type, new BufferBuilder(type.bufferSize()));
		}

		protected SuperRenderTypeBufferPhase() {
			super(new BufferBuilder(256), createEntityBuilders());
		}

	}

}
