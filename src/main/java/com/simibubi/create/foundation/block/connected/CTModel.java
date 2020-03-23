package com.simibubi.create.foundation.block.connected;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour.CTContext;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class CTModel extends BakedModelWrapper<IBakedModel> {

	private static ModelProperty<CTData> CT_PROPERTY = new ModelProperty<>();
	private ConnectedTextureBehaviour behaviour;

	private class CTData {
		int[] indices;

		public CTData() {
			indices = new int[6];
			Arrays.fill(indices, -1);
		}

		void put(Direction face, int texture) {
			indices[face.getIndex()] = texture;
		}

		int get(Direction face) {
			return indices[face.getIndex()];
		}
	}

	public CTModel(IBakedModel originalModel, IHaveConnectedTextures block) {
		super(originalModel);
		behaviour = block.getBehaviour();
	}

	@Override
	public IModelData getModelData(ILightReader world, BlockPos pos, BlockState state, IModelData tileData) {
		if (!(state.getBlock() instanceof IHaveConnectedTextures))
			return EmptyModelData.INSTANCE;
		CTData data = new CTData();

		for (Direction face : Direction.values()) {
			if (!Block.shouldSideBeRendered(state, world, pos, face))
				continue;
			CTSpriteShiftEntry spriteShift = behaviour.get(state, face);
			if (spriteShift == null)
				continue;
			CTContext ctContext = behaviour.buildContext(world, pos, state, face);
			data.put(face, spriteShift.getTextureIndex(ctContext));
		}
		return new ModelDataMap.Builder().withInitial(CT_PROPERTY, data).build();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
		List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, side, rand, extraData));
		if (!extraData.hasProperty(CT_PROPERTY))
			return quads;
		CTData data = extraData.getData(CT_PROPERTY);

		for (int i = 0; i < quads.size(); i++) {
			BakedQuad quad = quads.get(i);

			CTSpriteShiftEntry spriteShift = behaviour.get(state, quad.getFace());
			if (spriteShift == null)
				continue;
			if (quad.getSprite() != spriteShift.getOriginal())
				continue;
			int index = data.get(quad.getFace());
			if (index == -1)
				continue;

			float uShift = spriteShift.getUShift(index);
			float vShift = spriteShift.getVShift(index);

			BakedQuad newQuad =
				new BakedQuad(Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length), quad.getTintIndex(),
						quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting());
			VertexFormat format = DefaultVertexFormats.BLOCK;
			int[] vertexData = newQuad.getVertexData();

			for (int vertex = 0; vertex < vertexData.length; vertex += format.getIntegerSize()) {
				int uvOffset = 20 / 4; // TODO 1.15 is this the right offset?
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
