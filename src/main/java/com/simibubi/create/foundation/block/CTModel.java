package com.simibubi.create.foundation.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.simibubi.create.foundation.block.CTModelTextureHandler.TextureEntry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class CTModel extends BakedModelWrapper<IBakedModel> {

	private static ModelProperty<CTData> CT_PROPERTY = new ModelProperty<>();
	private TextureEntry texture;

	private class CTData {
		int[] textures;

		public CTData() {
			textures = new int[6];
			Arrays.fill(textures, -1);
		}

		void put(Direction face, int texture) {
			textures[face.getIndex()] = texture;
		}

		int get(Direction face) {
			return textures[face.getIndex()];
		}
	}

	public CTModel(IBakedModel originalModel, String blockId) {
		super(originalModel);
		texture = CTModelTextureHandler.get(blockId);
	}

	@Override
	public IModelData getModelData(IEnviromentBlockReader world, BlockPos pos, BlockState state, IModelData tileData) {
		if (!(state.getBlock() instanceof IHaveConnectedTextures))
			return EmptyModelData.INSTANCE;
		CTData data = new CTData();
		IHaveConnectedTextures texDef = (IHaveConnectedTextures) state.getBlock();
		for (Direction face : Direction.values()) {
			if (!Block.shouldSideBeRendered(state, world, pos, face))
				continue;
			data.put(face, texDef.getTextureIndex(world, pos, state, face));
		}
		return new ModelDataMap.Builder().withInitial(CT_PROPERTY, data).build();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
		List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, side, rand, extraData));
		if (!extraData.hasProperty(CT_PROPERTY))
			return quads;
		IHaveConnectedTextures texDef = (IHaveConnectedTextures) state.getBlock();
		CTData data = extraData.getData(CT_PROPERTY);

		for (int i = 0; i < quads.size(); i++) {
			BakedQuad quad = quads.get(i);
			if (!texDef.appliesTo(quad))
				continue;
			int index = data.get(quad.getFace());
			if (index == -1)
				return quads;

			float textureSize = 16f / 128f / 8f;
			float uShift = (index % 8) * textureSize;
			float vShift = (index / 8) * textureSize * 2;

			uShift = texture.connectedTextures.getInterpolatedU((index % 8) * 2) - texture.originalTexture.getMinU();
			vShift = texture.connectedTextures.getInterpolatedV((index / 8) * 2) - texture.originalTexture.getMinV();

			BakedQuad newQuad = new BakedQuad(Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length),
					quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(),
					quad.getFormat());
			VertexFormat format = quad.getFormat();
			int[] vertexData = newQuad.getVertexData();
			for (int vertex = 0; vertex < vertexData.length; vertex += format.getIntegerSize()) {
				int uvOffset = format.getUvOffsetById(0) / 4;
				int uIndex = vertex + uvOffset;
				int vIndex = vertex + uvOffset + 1;
				float u = Float.intBitsToFloat(vertexData[uIndex]);
				float v = Float.intBitsToFloat(vertexData[vIndex]);
				u += uShift;
				v += vShift;
				vertexData[uIndex] = Float.floatToIntBits(u);
				vertexData[vIndex] = Float.floatToIntBits(v);
			}
			quads.set(i, newQuad);
		}
		return quads;
	}

}
