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

	private static final SuperRenderTypeBuffer INSTANCE = new SuperRenderTypeBuffer();

	public static SuperRenderTypeBuffer getInstance() {
		return INSTANCE;
	}

	private SuperRenderTypeBufferPhase earlyBuffer;
	private SuperRenderTypeBufferPhase defaultBuffer;
	private SuperRenderTypeBufferPhase lateBuffer;

	public SuperRenderTypeBuffer() {
		earlyBuffer = new SuperRenderTypeBufferPhase();
		defaultBuffer = new SuperRenderTypeBufferPhase();
		lateBuffer = new SuperRenderTypeBufferPhase();
	}

	public VertexConsumer getEarlyBuffer(RenderType type) {
		return earlyBuffer.bufferSource.getBuffer(type);
	}

	@Override
	public VertexConsumer getBuffer(RenderType type) {
		return defaultBuffer.bufferSource.getBuffer(type);
	}

	public VertexConsumer getLateBuffer(RenderType type) {
		return lateBuffer.bufferSource.getBuffer(type);
	}

	public void draw() {
		earlyBuffer.bufferSource.endBatch();
		defaultBuffer.bufferSource.endBatch();
		lateBuffer.bufferSource.endBatch();
	}

	public void draw(RenderType type) {
		earlyBuffer.bufferSource.endBatch(type);
		defaultBuffer.bufferSource.endBatch(type);
		lateBuffer.bufferSource.endBatch(type);
	}

	private static class SuperRenderTypeBufferPhase {

		// Visible clones from RenderBuffers
		private final ChunkBufferBuilderPack fixedBufferPack = new ChunkBufferBuilderPack();
		private final SortedMap<RenderType, BufferBuilder> fixedBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), map -> {
				map.put(Sheets.solidBlockSheet(), fixedBufferPack.builder(RenderType.solid()));
				map.put(Sheets.cutoutBlockSheet(), fixedBufferPack.builder(RenderType.cutout()));
				map.put(Sheets.bannerSheet(), fixedBufferPack.builder(RenderType.cutoutMipped()));
				map.put(Sheets.translucentCullBlockSheet(), fixedBufferPack.builder(RenderType.translucent()));
				put(map, Sheets.shieldSheet());
				put(map, Sheets.bedSheet());
				put(map, Sheets.shulkerBoxSheet());
				put(map, Sheets.signSheet());
				put(map, Sheets.chestSheet());
				put(map, RenderType.translucentNoCrumbling());
				put(map, RenderType.armorGlint());
				put(map, RenderType.armorEntityGlint());
				put(map, RenderType.glint());
				put(map, RenderType.glintDirect());
				put(map, RenderType.glintTranslucent());
				put(map, RenderType.entityGlint());
				put(map, RenderType.entityGlintDirect());
				put(map, RenderType.waterMask());
				ModelBakery.DESTROY_TYPES.forEach((p_173062_) -> {
					put(map, p_173062_);
				});

				// extras
				put(map, RenderTypes.outlineSolid());
			});
		private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediateWithBuffers(fixedBuffers, new BufferBuilder(256));

		private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> map, RenderType type) {
			map.put(type, new BufferBuilder(type.bufferSize()));
		}

	}

}
