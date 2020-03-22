package com.simibubi.create.modules.contraptions.components.contraptions;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.utility.PlacementSimulationWorld;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.SuperByteBufferCache.Compartment;
import com.simibubi.create.foundation.utility.WrappedWorld;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.ReportedException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.client.model.data.EmptyModelData;

public class ContraptionRenderer {

	public static final Compartment<Contraption> CONTRAPTION = new Compartment<>();
	protected static PlacementSimulationWorld renderWorld;
	protected static LightingWorld lightingWorld;

	public static void render(World world, Contraption c, Consumer<SuperByteBuffer> transform, BufferBuilder buffer) {
		SuperByteBuffer contraptionBuffer = CreateClient.bufferCache.get(CONTRAPTION, c, () -> renderContraption(c));
		transform.accept(contraptionBuffer);
		contraptionBuffer.light((lx, ly, lz) -> getLight(world, lx, ly, lz)).renderInto(buffer);
		renderActors(world, c, transform, buffer);
	}

	public static void renderTEsWithGL(World world, Contraption c, Vec3d position, Vec3d rotation) {
		TileEntityRendererDispatcher dispatcher = TileEntityRendererDispatcher.instance;
		float pt = Minecraft.getInstance().getRenderPartialTicks();
		World prevDispatcherWorld = dispatcher.world;

		if (lightingWorld == null)
			lightingWorld = new LightingWorld(world);
		lightingWorld.setWorld(world);
		lightingWorld.setTransform(position, rotation);
		dispatcher.setWorld(lightingWorld);

		for (Iterator<TileEntity> iterator = c.customRenderTEs.iterator(); iterator.hasNext();) {
			TileEntity tileEntity = iterator.next();
			if (dispatcher.getRenderer(tileEntity) == null) {
				iterator.remove();
				continue;
			}

			try {

				BlockPos pos = tileEntity.getPos();
				if (!tileEntity.hasFastRenderer()) {
					RenderHelper.enableStandardItemLighting();
					int i = lightingWorld.getCombinedLight(pos, 0);
					int j = i % 65536;
					int k = i / 65536;
					GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float) j, (float) k);
					GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				}

				World prevTileWorld = tileEntity.getWorld();
				tileEntity.setWorld(lightingWorld);
				dispatcher.render(tileEntity, pos.getX(), pos.getY(), pos.getZ(), pt, -1, true);
				tileEntity.setWorld(prevTileWorld);

			} catch (ReportedException e) {
				if (AllConfigs.CLIENT.explainRenderErrors.get()) {
					Create.logger.error("TileEntity " + tileEntity.getType().getRegistryName().toString()
							+ " didn't want to render while moved.\n", e);
				} else {
					Create.logger.error("TileEntity " + tileEntity.getType().getRegistryName().toString()
							+ " didn't want to render while moved.\n");
				}
				iterator.remove();
				continue;
			}
		}

		dispatcher.setWorld(prevDispatcherWorld);
	}

	private static SuperByteBuffer renderContraption(Contraption c) {
		if (renderWorld == null || renderWorld.getWorld() != Minecraft.getInstance().world)
			renderWorld = new PlacementSimulationWorld(Minecraft.getInstance().world);

		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
		Random random = new Random();
		BufferBuilder builder = new BufferBuilder(0);
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		for (BlockInfo info : c.blocks.values())
			renderWorld.setBlockState(info.pos, info.state);
		for (BlockPos pos : c.renderOrder) {
			BlockInfo info = c.blocks.get(pos);
			BlockState state = info.state;

			if (state.getRenderType() == BlockRenderType.ENTITYBLOCK_ANIMATED)
				continue;

			IBakedModel originalModel = dispatcher.getModelForState(state);
			blockRenderer.renderModel(renderWorld, originalModel, state, info.pos, builder, true, random, 42,
					EmptyModelData.INSTANCE);
		}

		builder.finishDrawing();
		renderWorld.clear();
		return new SuperByteBuffer(builder);
	}

	private static void renderActors(World world, Contraption c, Consumer<SuperByteBuffer> transform,
			BufferBuilder buffer) {
		for (Pair<BlockInfo, MovementContext> actor : c.getActors()) {
			MovementContext context = actor.getRight();
			if (context == null)
				continue;
			if (context.world == null)
				context.world = world;

			BlockInfo blockInfo = actor.getLeft();
			for (SuperByteBuffer render : Contraption.getMovement(blockInfo.state).renderListInContraption(context)) {
				if (render == null)
					continue;

				int posX = blockInfo.pos.getX();
				int posY = blockInfo.pos.getY();
				int posZ = blockInfo.pos.getZ();

				render.translate(posX, posY, posZ);
				transform.accept(render);
				render.light((lx, ly, lz) -> getLight(world, lx, ly, lz)).renderInto(buffer);
			}
		}
	}

	public static int getLight(World world, float lx, float ly, float lz) {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		float sky = 0, block = 0;
		float offset = 1 / 8f;

		for (float zOffset = offset; zOffset >= -offset; zOffset -= 2 * offset)
			for (float yOffset = offset; yOffset >= -offset; yOffset -= 2 * offset)
				for (float xOffset = offset; xOffset >= -offset; xOffset -= 2 * offset) {
					pos.setPos(lx + xOffset, ly + yOffset, lz + zOffset);
					sky += world.getLightLevel(LightType.SKY, pos) / 8f;
					block += world.getLightLevel(LightType.BLOCK, pos) / 8f;
				}

		return ((int) sky) << 20 | ((int) block) << 4;
	}

	private static class LightingWorld extends WrappedWorld {

		private Vec3d offset;
		private Vec3d rotation;

		public LightingWorld(World world) {
			super(world);
		}

		void setWorld(World world) {
			this.world = world;
		}

		void setTransform(Vec3d offset, Vec3d rotation) {
			this.offset = offset;
			this.rotation = rotation;
		}

		@Override
		public int getCombinedLight(BlockPos pos, int minLight) {
			return super.getCombinedLight(transformPos(pos), minLight);
		}

		private BlockPos transformPos(BlockPos pos) {
			Vec3d vec = VecHelper.getCenterOf(pos);
			vec = VecHelper.rotate(vec, rotation.x, rotation.y, rotation.z);
			vec = vec.add(offset).subtract(VecHelper.getCenterOf(BlockPos.ZERO));
			return new BlockPos(vec);
		}

	}

}
