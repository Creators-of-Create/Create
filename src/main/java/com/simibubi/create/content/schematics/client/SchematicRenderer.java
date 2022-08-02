package com.simibubi.create.content.schematics.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.core.model.ShadeSeparatedBufferBuilder;
import com.jozufozu.flywheel.core.model.ShadeSeparatingVertexConsumer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.render.TileEntityRenderHelper;

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
		TileEntityRenderHelper.renderTileEntities(schematic, schematic.getRenderedTileEntities(), ms, buffers);
	}

	protected void redraw() {
		bufferCache.clear();
		for (RenderType layer : RenderType.chunkBufferLayers()) {
			SuperByteBuffer buffer = drawLayer(layer);
			if (!buffer.isEmpty())
				bufferCache.put(layer, buffer);
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
		ShadeSeparatedBufferBuilder builder = new ShadeSeparatedBufferBuilder(512);
		BufferBuilder unshadedBuilder = objects.unshadedBuilder;

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		unshadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		shadeSeparatingWrapper.prepare(builder, unshadedBuilder);

		ForgeHooksClient.setRenderType(layer);
		ModelBlockRenderer.enableCaching();
		for (BlockPos localPos : BlockPos.betweenClosed(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
			BlockPos pos = mutableBlockPos.setWithOffset(localPos, anchor);
			BlockState state = renderWorld.getBlockState(pos);

			if (state.getRenderShape() == RenderShape.MODEL && ItemBlockRenderTypes.canRenderInLayer(state, layer)) {
				poseStack.pushPose();
				poseStack.translate(localPos.getX(), localPos.getY(), localPos.getZ());

				BlockEntity tileEntity = renderWorld.getBlockEntity(localPos);
				dispatcher.renderBatched(state, pos, renderWorld, poseStack, shadeSeparatingWrapper, true, random,
					tileEntity != null ? tileEntity.getModelData() : EmptyModelData.INSTANCE);

				poseStack.popPose();
			}
		}
		ModelBlockRenderer.clearCache();
		ForgeHooksClient.setRenderType(null);

		shadeSeparatingWrapper.clear();
		unshadedBuilder.end();
		builder.appendUnshadedVertices(unshadedBuilder);
		builder.end();

		renderWorld.renderMode = false;

		return new SuperByteBuffer(builder);
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
		public final BufferBuilder unshadedBuilder = new BufferBuilder(512);
	}

}
