package com.simibubi.create.modules.curiosities.partialWindows;

import static com.simibubi.create.modules.curiosities.partialWindows.WindowInABlockTileEntity.PARTIAL_BLOCK;
import static com.simibubi.create.modules.curiosities.partialWindows.WindowInABlockTileEntity.POSITION;
import static com.simibubi.create.modules.curiosities.partialWindows.WindowInABlockTileEntity.WINDOW_BLOCK;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.simibubi.create.foundation.block.render.WrappedBakedModel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

public class WindowInABlockModel extends WrappedBakedModel {

	public WindowInABlockModel(IBakedModel template) {
		super(template);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData data) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState partialState = data.getData(PARTIAL_BLOCK);
		BlockState windowState = data.getData(WINDOW_BLOCK);
		BlockPos position = data.getData(POSITION);
		ClientWorld world = Minecraft.getInstance().world;
		List<BakedQuad> quads = new ArrayList<>();

		if (partialState == null || windowState == null)
			return dispatcher.getModelForState(Blocks.DIRT.getDefaultState()).getQuads(state, side, rand, data);

		RenderType renderLayer = MinecraftForgeClient.getRenderLayer();
		if (RenderTypeLookup.canRenderInLayer(partialState, renderLayer) && partialState != null) {
			IBakedModel partialModel = dispatcher.getModelForState(partialState);
			IModelData modelData = partialModel.getModelData(Minecraft.getInstance().world, position, partialState,
					EmptyModelData.INSTANCE);
			quads.addAll(partialModel.getQuads(partialState, side, rand, modelData));
		}
		if (RenderTypeLookup.canRenderInLayer(windowState, renderLayer) && windowState != null) {
			IBakedModel windowModel = dispatcher.getModelForState(windowState);
			IModelData modelData =
				windowModel.getModelData(Minecraft.getInstance().world, position, windowState, EmptyModelData.INSTANCE);
			quads.addAll(dispatcher.getModelForState(windowState).getQuads(windowState, side, rand, modelData).stream()
					.filter(q -> {
						Direction face = q.getFace();
						if (face != null
								&& world.getBlockState(position.offset(face)).isSideInvisible(windowState, face))
							return false;
						if (face != null && Block.hasSolidSide(partialState, world, position, face))
							return false;

						fightZfighting(q);
						return true;
					}).collect(Collectors.toList()));
		}

		return quads;
	}

	protected void fightZfighting(BakedQuad q) {
		VertexFormat format = DefaultVertexFormats.BLOCK;
		int[] data = q.getVertexData();
		Vec3i vec = q.getFace().getDirectionVec();
		int dirX = vec.getX();
		int dirY = vec.getY();
		int dirZ = vec.getZ();

		for (int i = 0; i < 4; ++i) {
			int j = format.getIntegerSize() * i;
			float x = Float.intBitsToFloat(data[j + 0]);
			float y = Float.intBitsToFloat(data[j + 1]);
			float z = Float.intBitsToFloat(data[j + 2]);
			double offset = q.getFace().getAxis().getCoordinate(x, y, z);

			if (offset < 1 / 1024d || offset > 1023 / 1024d) {
				data[j + 0] = Float.floatToIntBits(x - 1 / 512f * dirX);
				data[j + 1] = Float.floatToIntBits(y - 1 / 512f * dirY);
				data[j + 2] = Float.floatToIntBits(z - 1 / 512f * dirZ);
			}

		}
	}

	@Override
	public TextureAtlasSprite getParticleTexture(IModelData data) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState partialState = data.getData(PARTIAL_BLOCK);
		if (partialState == null)
			return super.getParticleTexture(data);
		return dispatcher.getModelForState(partialState).getParticleTexture(data);
	}

	@Override
	public boolean isAmbientOcclusion() {
		return MinecraftForgeClient.getRenderLayer() == RenderType.getSolid();
	}

}
