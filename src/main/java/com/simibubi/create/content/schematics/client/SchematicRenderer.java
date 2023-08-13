package com.simibubi.create.content.schematics.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.core.model.ShadeSeparatedBufferedData;
import com.jozufozu.flywheel.core.model.ShadeSeparatingVertexConsumer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simibubi.create.foundation.render.BlockEntityRenderHelper;

import net.createmod.catnip.render.ShadeSpearatingSuperByteBuffer;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.catnip.utility.worldWrappers.SchematicWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SchematicRenderer {

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private final Map<RenderType, SuperByteBuffer> bufferCache = new LinkedHashMap<>(getLayerCount());
	private boolean active;
	private boolean changed;
	protected SchematicWorld schematic;
	private BlockPos anchor;

	public SchematicRenderer() {
		changed = false;
	}

	public void display(SchematicWorld world) {
		this.anchor = world.anchor;
		this.schematic = world;
		this.active = true;
		this.changed = true;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void update() {
		changed = true;
	}

	public void tick() {
		if (!active)
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null || !changed)
			return;

		redraw();
		changed = false;
	}

	public void render(PoseStack ms, SuperRenderTypeBuffer buffers) {
		if (!active)
			return;
		bufferCache.forEach((layer, buffer) -> {
			buffer.renderInto(ms, buffers.getBuffer(layer));
		});
		BlockEntityRenderHelper.renderBlockEntities(schematic, schematic.getRenderedBlockEntities(), ms, buffers);
	}

	protected void redraw() {
		bufferCache.forEach((layer, sbb) -> sbb.delete());
		bufferCache.clear();

		for (RenderType layer : RenderType.chunkBufferLayers()) {
			SuperByteBuffer buffer = drawLayer(layer);
			if (!buffer.isEmpty())
				bufferCache.put(layer, buffer);
			else
				buffer.delete();
		}
	}

	protected SuperByteBuffer drawLayer(RenderType layer) {
		BlockRenderDispatcher dispatcher = ModelUtil.VANILLA_RENDERER;
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		PoseStack poseStack = objects.poseStack;
		Random random = objects.random;
		BlockPos.MutableBlockPos mutableBlockPos = objects.mutableBlockPos;
		SchematicWorld renderWorld = schematic;
		renderWorld.renderMode = true;
		BoundingBox bounds = renderWorld.getBounds();

		ShadeSeparatingVertexConsumer shadeSeparatingWrapper = objects.shadeSeparatingWrapper;
		BufferBuilder shadedBuilder = objects.shadedBuilder;
		BufferBuilder unshadedBuilder = objects.unshadedBuilder;

		shadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		unshadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		shadeSeparatingWrapper.prepare(shadedBuilder, unshadedBuilder);

		ForgeHooksClient.setRenderType(layer);
		ModelBlockRenderer.enableCaching();
		for (BlockPos localPos : BlockPos.betweenClosed(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
			BlockPos pos = mutableBlockPos.setWithOffset(localPos, anchor);
			BlockState state = renderWorld.getBlockState(pos);

			if (state.getRenderShape() == RenderShape.MODEL && ItemBlockRenderTypes.canRenderInLayer(state, layer)) {
				poseStack.pushPose();
				poseStack.translate(localPos.getX(), localPos.getY(), localPos.getZ());

				BlockEntity blockEntity = renderWorld.getBlockEntity(localPos);
				dispatcher.renderBatched(state, pos, renderWorld, poseStack, shadeSeparatingWrapper, true, random,
					blockEntity != null ? blockEntity.getModelData() : EmptyModelData.INSTANCE);

				poseStack.popPose();
			}
		}
		ModelBlockRenderer.clearCache();
		ForgeHooksClient.setRenderType(null);

		shadeSeparatingWrapper.clear();
		ShadeSeparatedBufferedData bufferedData = ModelUtil.endAndCombine(shadedBuilder, unshadedBuilder);

		renderWorld.renderMode = false;

		SuperByteBuffer sbb = new ShadeSpearatingSuperByteBuffer(bufferedData);
		bufferedData.release();
		return sbb;
	}

	private static int getLayerCount() {
		return RenderType.chunkBufferLayers()
			.size();
	}

	private static class ThreadLocalObjects {
		public final PoseStack poseStack = new PoseStack();
		public final Random random = new Random();
		public final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		public final ShadeSeparatingVertexConsumer shadeSeparatingWrapper = new ShadeSeparatingVertexConsumer();
		public final BufferBuilder shadedBuilder = new BufferBuilder(512);
		public final BufferBuilder unshadedBuilder = new BufferBuilder(512);
	}

}
