package com.simibubi.create.foundation.metadoc.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.metadoc.MetaDocWorld;
import com.simibubi.create.foundation.metadoc.Select;
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
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class WorldSectionElement extends AnimatedSceneElement {

	public static final Compartment<Pair<Integer, Integer>> DOC_WORLD_SECTION = new Compartment<>();

	List<TileEntity> renderedTileEntities;
	Select section;
	boolean redraw;

	public WorldSectionElement(Select section) {
		this.section = section;
	}

	public void queueRedraw(MetaDocWorld world) {
		redraw = true;
	}

	public void tick() {
		if (renderedTileEntities == null)
			return;
		renderedTileEntities.forEach(te -> {
			if (te instanceof ITickableTileEntity)
				((ITickableTileEntity) te).tick();
		});
	}

	@Override
	public void render(MetaDocWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade) {
		int light = -1;
		if (fade != 1)
			light = (int) (MathHelper.lerp(fade, 5, 14));
		if (redraw)
			renderedTileEntities = null;

		world.pushFakeLight(light);
		renderTileEntities(world, ms, buffer);
		world.popLight();

		if (buffer instanceof IRenderTypeBuffer.Impl)
			((IRenderTypeBuffer.Impl) buffer).draw();
		renderStructure(world, ms, buffer, fade);
		redraw = false;
	}

	protected void renderStructure(MetaDocWorld world, MatrixStack ms, IRenderTypeBuffer buffer, float fade) {
		SuperByteBufferCache bufferCache = CreateClient.bufferCache;
		List<RenderType> blockLayers = RenderType.getBlockLayers();
		int code = hashCode() ^ world.hashCode();

		buffer.getBuffer(RenderType.getSolid());
		for (int i = 0; i < blockLayers.size(); i++) {
			RenderType layer = blockLayers.get(i);
			Pair<Integer, Integer> key = Pair.of(code, i);
			if (redraw)
				bufferCache.invalidate(DOC_WORLD_SECTION, key);
			SuperByteBuffer contraptionBuffer =
				bufferCache.get(DOC_WORLD_SECTION, key, () -> buildStructureBuffer(world, layer));
			if (contraptionBuffer.isEmpty())
				continue;

			int light = lightCoordsFromFade(fade);
			contraptionBuffer.light(light)
				.renderInto(ms, buffer.getBuffer(layer));
		}
	}

	private void renderTileEntities(MetaDocWorld world, MatrixStack ms, IRenderTypeBuffer buffer) {
		if (renderedTileEntities == null) {
			renderedTileEntities = new ArrayList<>();
			section.all()
				.map(world::getTileEntity)
				.filter(Objects::nonNull)
				.forEach(renderedTileEntities::add);
		} else
			renderedTileEntities.removeIf(te -> world.getTileEntity(te.getPos()) != te);

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
		world.setMask(this.section);

		section.all()
			.forEach(pos -> {
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

		world.clearMask();
		builder.finishDrawing();
		return new SuperByteBuffer(builder);
	}

}
