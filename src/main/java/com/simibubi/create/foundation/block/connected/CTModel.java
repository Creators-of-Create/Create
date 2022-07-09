package com.simibubi.create.foundation.block.connected;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.simibubi.create.foundation.block.render.QuadHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;
import net.minecraftforge.client.model.data.ModelProperty;

public class CTModel extends BakedModelWrapperWithData {

	private static final ModelProperty<CTData> CT_PROPERTY = new ModelProperty<>();

	private final ConnectedTextureBehaviour behaviour;

	public CTModel(BakedModel originalModel, ConnectedTextureBehaviour behaviour) {
		super(originalModel);
		this.behaviour = behaviour;
	}

	@Override
	protected Builder gatherModelData(Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state) {
		return builder.withInitial(CT_PROPERTY, createCTData(world, pos, state));
	}

	protected CTData createCTData(BlockAndTintGetter world, BlockPos pos, BlockState state) {
		CTData data = new CTData();
		MutableBlockPos mutablePos = new MutableBlockPos();
		for (Direction face : Iterate.directions) {
			if (!behaviour.buildContextForOccludedDirections()
				&& !Block.shouldRenderFace(state, world, pos, face, mutablePos.setWithOffset(pos, face)))
				continue;
			CTType dataType = behaviour.getDataType(state, face);
			if (dataType == null)
				continue;
			CTContext context = behaviour.buildContext(world, pos, state, face, dataType.getContextRequirement());
			data.put(face, dataType.getTextureIndex(context));
		}
		return data;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
		List<BakedQuad> quads = super.getQuads(state, side, rand, extraData);
		if (!extraData.hasProperty(CT_PROPERTY))
			return quads;

		CTData data = extraData.getData(CT_PROPERTY);
		quads = new ArrayList<>(quads);

		for (int i = 0; i < quads.size(); i++) {
			BakedQuad quad = quads.get(i);

			int index = data.get(quad.getDirection());
			if (index == -1)
				continue;

			CTSpriteShiftEntry spriteShift = behaviour.getShift(state, quad.getDirection(), quad.getSprite());
			if (spriteShift == null)
				continue;
			if (quad.getSprite() != spriteShift.getOriginal())
				continue;

			BakedQuad newQuad = QuadHelper.clone(quad);
			int[] vertexData = newQuad.getVertices();

			for (int vertex = 0; vertex < 4; vertex++) {
				float u = QuadHelper.getU(vertexData, vertex);
				float v = QuadHelper.getV(vertexData, vertex);
				QuadHelper.setU(vertexData, vertex, spriteShift.getTargetU(u, index));
				QuadHelper.setV(vertexData, vertex, spriteShift.getTargetV(v, index));
			}

			quads.set(i, newQuad);
		}

		return quads;
	}

	private static class CTData {
		private final int[] indices;

		public CTData() {
			indices = new int[6];
			Arrays.fill(indices, -1);
		}

		public void put(Direction face, int texture) {
			indices[face.get3DDataValue()] = texture;
		}

		public int get(Direction face) {
			return indices[face.get3DDataValue()];
		}
	}

}
