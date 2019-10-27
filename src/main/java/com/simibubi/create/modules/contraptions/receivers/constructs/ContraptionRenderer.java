package com.simibubi.create.modules.contraptions.receivers.constructs;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.foundation.utility.BufferManipulator;
import com.simibubi.create.foundation.utility.PlacementSimulationWorld;
import com.simibubi.create.modules.contraptions.receivers.constructs.IHaveMovementBehavior.MovementContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.client.model.data.EmptyModelData;

public class ContraptionRenderer {

	protected static Cache<Contraption, ContraptionVertexBuffer> cachedConstructs;
	protected static PlacementSimulationWorld renderWorld;

	public static <T extends BufferManipulator> void cacheContraptionIfMissing(Contraption c) {
		if (cachedConstructs == null)
			cachedConstructs = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.SECONDS).build();
		if (cachedConstructs.getIfPresent(c) != null)
			return;
		if (renderWorld == null || renderWorld.getWorld() != Minecraft.getInstance().world)
			renderWorld = new PlacementSimulationWorld(Minecraft.getInstance().world);

		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
		Random random = new Random();
		BufferBuilder builder = new BufferBuilder(0);
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		builder.setTranslation(0, 0, 0);

		for (BlockInfo info : c.blocks.values()) {
			renderWorld.setBlockState(info.pos, info.state);
		}

		for (BlockInfo info : c.blocks.values()) {
			IBakedModel originalModel = dispatcher.getModelForState(info.state);
			blockRenderer.renderModel(renderWorld, originalModel, info.state, info.pos, builder, true, random, 42,
					EmptyModelData.INSTANCE);
		}

		builder.finishDrawing();
		renderWorld.clear();
		cachedConstructs.put(c, new ContraptionVertexBuffer(builder.getByteBuffer()));
	}

	public static ContraptionVertexBuffer get(Contraption c) {
		return cachedConstructs.getIfPresent(c);
	}

	public static void renderActors(World world, Contraption c, float xIn, float yIn, float zIn, float yaw, float pitch,
			BufferBuilder buffer) {
		for (Pair<BlockInfo, MovementContext> actor : c.getActors()) {
			MovementContext context = actor.getRight();
			if (context == null)
				continue;
			if (context.world == null)
				context.world = world;

			BlockInfo blockInfo = actor.getLeft();
			IHaveMovementBehavior block = (IHaveMovementBehavior) blockInfo.state.getBlock();
			ByteBuffer renderInConstruct = block.renderInConstruct(context);
			if (renderInConstruct == null)
				continue;

			int posX = blockInfo.pos.getX();
			int posY = blockInfo.pos.getY();
			int posZ = blockInfo.pos.getZ();

			float x = xIn + posX;
			float y = yIn + posY;
			float z = zIn + posZ;

			float xOrigin = -posX + c.anchor.getX() + .5f;
			float yOrigin = -posY + c.anchor.getY() + .5f;
			float zOrigin = -posZ + c.anchor.getZ() + .5f;

			buffer.putBulkData(BufferManipulator.remanipulateBuffer(renderInConstruct, x, y, z, xOrigin, yOrigin,
					zOrigin, yaw, pitch));
		}
	}

	public static void invalidateCache() {
		if (cachedConstructs != null)
			cachedConstructs.invalidateAll();
	}

}
