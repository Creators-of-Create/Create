package com.simibubi.create.modules.contraptions.components.constructs.bearing;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.PlacementSimulationWorld;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.client.model.data.EmptyModelData;

public class MechanicalBearingTileEntityRenderer extends KineticTileEntityRenderer {

	protected static Cache<RotationConstruct, RotationConstructVertexBuffer> cachedConstructs;
	protected static PlacementSimulationWorld renderWorld;

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, buffer);

		MechanicalBearingTileEntity bearingTe = (MechanicalBearingTileEntity) te;
		final Direction facing = te.getBlockState().get(BlockStateProperties.FACING);
		BlockState capState = AllBlocks.MECHANICAL_BEARING_TOP.get().getDefaultState().with(BlockStateProperties.FACING,
				facing);

		SuperByteBuffer superBuffer = CreateClient.bufferCache.renderBlockState(KINETIC_TILE, capState);
		float interpolatedAngle = bearingTe.getInterpolatedAngle(partialTicks);
		kineticRotationTransform(superBuffer, bearingTe, facing.getAxis(), interpolatedAngle, getWorld());
		superBuffer.translate(x, y, z).renderInto(buffer);

		if (!bearingTe.running)
			return;

		cacheConstructIfMissing(bearingTe.movingConstruct);
		renderConstructFromCache(bearingTe.movingConstruct, bearingTe, x, y, z, partialTicks, buffer);
	}

	protected void cacheConstructIfMissing(RotationConstruct c) {
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
		cachedConstructs.put(c, new RotationConstructVertexBuffer(builder.getByteBuffer()));
	}

	protected void renderConstructFromCache(RotationConstruct c, MechanicalBearingTileEntity te, double x, double y,
			double z, float partialTicks, BufferBuilder buffer) {
		float zfightBonus = 1 / 128f;
		Direction direction = te.getBlockState().get(BlockStateProperties.FACING);
		Vec3i vec = direction.getDirectionVec();
		buffer.putBulkData(cachedConstructs.getIfPresent(c).getTransformed(te, (float) (x) + vec.getX() * zfightBonus,
				(float) (y) + vec.getY() * zfightBonus, (float) (z) + vec.getZ() * zfightBonus,
				te.getInterpolatedAngle(partialTicks), direction.getAxis()));
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.SHAFT_HALF.get().getDefaultState().with(BlockStateProperties.FACING,
				te.getBlockState().get(BlockStateProperties.FACING).getOpposite());
	}

	public static void invalidateCache() {
		if (cachedConstructs != null)
			cachedConstructs.invalidateAll();
	}

}
