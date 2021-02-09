package com.simibubi.create.foundation.metadoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.SuperByteBufferCache;
import com.simibubi.create.foundation.utility.SuperByteBufferCache.Compartment;
import com.simibubi.create.foundation.utility.TileEntityRenderHelper;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public abstract class WorldSectionElement extends AnimatedSceneElement implements Predicate<BlockPos> {

	public static final Compartment<Pair<Integer, Integer>> DOC_WORLD_SECTION = new Compartment<>();

	List<TileEntity> renderedTileEntities;

	@Override
	public void render(MetaDocWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade) {
		renderTileEntities(world, ms, buffer);
		if (buffer instanceof IRenderTypeBuffer.Impl)
			((IRenderTypeBuffer.Impl) buffer).draw();
		renderStructure(world, ms, buffer, fade);
	}

	@Override
	public abstract int hashCode();

	public abstract Stream<BlockPos> all();

	public static class Cuboid extends WorldSectionElement {

		MutableBoundingBox bb;
		Vec3i origin;
		Vec3i size;

		public Cuboid(BlockPos origin, Vec3i size) {
			bb = new MutableBoundingBox(origin, origin.add(size));
			this.origin = origin;
			this.size = size;
		}

		@Override
		public boolean test(BlockPos t) {
			return bb.isVecInside(t);
		}

		@Override
		public Stream<BlockPos> all() {
			return BlockPos.func_229383_a_(bb);
		}

		@Override
		public int hashCode() {
			return origin.hashCode() ^ size.hashCode();
		}

	}

	protected void renderStructure(MetaDocWorld world, MatrixStack ms, IRenderTypeBuffer buffer, float fade) {
		SuperByteBufferCache bufferCache = CreateClient.bufferCache;
		List<RenderType> blockLayers = RenderType.getBlockLayers();
		int code = hashCode();

		buffer.getBuffer(RenderType.getSolid());
		for (int i = 0; i < blockLayers.size(); i++) {
			RenderType layer = blockLayers.get(i);
			Pair<Integer, Integer> key = Pair.of(code, i);
			SuperByteBuffer contraptionBuffer =
				bufferCache.get(DOC_WORLD_SECTION, key, () -> buildStructureBuffer(world, layer));
			if (contraptionBuffer.isEmpty())
				continue;
			
			int light = 0xF000F0;
			if (fade != 1) {
				light = (int) (0xF * fade);
				light = light << 4 | light << 20;
			}
			
			contraptionBuffer.light(light)
				.renderInto(ms, buffer.getBuffer(layer));
		}
	}

	private void renderTileEntities(MetaDocWorld world, MatrixStack ms, IRenderTypeBuffer buffer) {
		if (renderedTileEntities == null) {
			renderedTileEntities = new ArrayList<>();
			all().map(world::getTileEntity)
				.filter(Objects::nonNull)
				.forEach(renderedTileEntities::add);
		}

		TileEntityRenderHelper.renderTileEntities(world, renderedTileEntities, ms, new MatrixStack(), buffer);
	}

	private SuperByteBuffer buildStructureBuffer(MetaDocWorld world, RenderType layer) {
		ForgeHooksClient.setRenderLayer(layer);
		MatrixStack ms = new MatrixStack();
		BlockRendererDispatcher dispatcher = Minecraft.getInstance()
			.getBlockRendererDispatcher();
		BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
		Random random = new Random();
		BufferBuilder builder = new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize());
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		all().forEach(pos -> {
			BlockState state = world.getBlockState(pos);
			if (state.getRenderType() == BlockRenderType.ENTITYBLOCK_ANIMATED)
				return;
			if (!RenderTypeLookup.canRenderInLayer(state, layer))
				return;

			IBakedModel originalModel = dispatcher.getModelForState(state);
			ms.push();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());
			blockRenderer.renderModel(world, originalModel, state, pos, ms, builder, true, random, 42,
				OverlayTexture.DEFAULT_UV, EmptyModelData.INSTANCE);
			ms.pop();
		});

		builder.finishDrawing();
		return new SuperByteBuffer(builder);
	}

}
