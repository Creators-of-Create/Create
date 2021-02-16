package com.simibubi.create.foundation.ponder.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.Select;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.SuperByteBufferCache;
import com.simibubi.create.foundation.utility.SuperByteBufferCache.Compartment;
import com.simibubi.create.foundation.utility.TileEntityRenderHelper;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.fluid.IFluidState;
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

	public void queueRedraw(PonderWorld world) {
		redraw = true;
	}

	public void tick(PonderScene scene) {
		if (renderedTileEntities == null)
			return;
		renderedTileEntities.forEach(te -> {
			if (te instanceof ITickableTileEntity)
				((ITickableTileEntity) te).tick();
		});
	}

	@Override
	protected void renderLayer(PonderWorld world, IRenderTypeBuffer buffer, RenderType type, MatrixStack ms,
		float fade) {
		renderStructure(world, ms, buffer, type, fade);
	}

	@Override
	public void renderFirst(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade) {
		int light = -1;
		if (fade != 1)
			light = (int) (MathHelper.lerp(fade, 5, 14));
		if (redraw)
			renderedTileEntities = null;

		world.pushFakeLight(light);
		renderTileEntities(world, ms, buffer);
		world.popLight();
	}

	protected void renderStructure(PonderWorld world, MatrixStack ms, IRenderTypeBuffer buffer, RenderType type,
		float fade) {
		SuperByteBufferCache bufferCache = CreateClient.bufferCache;
		int code = hashCode() ^ world.hashCode();

		Pair<Integer, Integer> key = Pair.of(code, RenderType.getBlockLayers()
			.indexOf(type));
		if (redraw)
			bufferCache.invalidate(DOC_WORLD_SECTION, key);
		SuperByteBuffer contraptionBuffer =
			bufferCache.get(DOC_WORLD_SECTION, key, () -> buildStructureBuffer(world, type));
		if (contraptionBuffer.isEmpty())
			return;

		int light = lightCoordsFromFade(fade);
		contraptionBuffer.light(light)
			.renderInto(ms, buffer.getBuffer(type));
	}
	
	@Override
	protected void renderLast(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade) {
		redraw = false;
	}

	private void renderTileEntities(PonderWorld world, MatrixStack ms, IRenderTypeBuffer buffer) {
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

	private SuperByteBuffer buildStructureBuffer(PonderWorld world, RenderType layer) {
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
				IFluidState ifluidstate = world.getFluidState(pos);

				ms.push();
				ms.translate(pos.getX(), pos.getY(), pos.getZ());

				if (state.getRenderType() != BlockRenderType.ENTITYBLOCK_ANIMATED && state.getBlock() != Blocks.AIR
					&& RenderTypeLookup.canRenderInLayer(state, layer))
					blockRenderer.renderModel(world, dispatcher.getModelForState(state), state, pos, ms, builder, true,
						random, 42, OverlayTexture.DEFAULT_UV, EmptyModelData.INSTANCE);

				if (!ifluidstate.isEmpty() && RenderTypeLookup.canRenderInLayer(ifluidstate, layer))
					dispatcher.renderFluid(pos, world, builder, ifluidstate);

				ms.pop();
			});

		world.clearMask();
		builder.finishDrawing();
		return new SuperByteBuffer(builder);
	}

}
