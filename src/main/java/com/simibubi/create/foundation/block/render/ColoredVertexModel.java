package com.simibubi.create.foundation.block.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.simibubi.create.foundation.block.IHaveColoredVertices;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class ColoredVertexModel extends BakedModelWrapper<IBakedModel> {

	private IHaveColoredVertices colorer;
	private static ModelProperty<BlockPos> POSITION_PROPERTY = new ModelProperty<>();

	public ColoredVertexModel(IBakedModel originalModel, IHaveColoredVertices colorer) {
		super(originalModel);
		this.colorer = colorer;
	}

	@Override
	public IModelData getModelData(IEnviromentBlockReader world, BlockPos pos, BlockState state, IModelData tileData) {
		return new ModelDataMap.Builder().withInitial(POSITION_PROPERTY, pos).build();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
		List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, side, rand, extraData));

		if (!extraData.hasProperty(POSITION_PROPERTY))
			return quads;

		for (int i = 0; i < quads.size(); i++) {
			BakedQuad quad = quads.get(i);

			BakedQuad newQuad = new BakedQuad(Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length),
					quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(),
					quad.getFormat());

			VertexFormat format = quad.getFormat();
			int[] vertexData = newQuad.getVertexData();
			BlockPos data = extraData.getData(POSITION_PROPERTY);

//			Direction direction = quad.getFace();
//			if (direction.getAxis().isHorizontal())
//				continue;

			for (int vertex = 0; vertex < vertexData.length; vertex += format.getIntegerSize()) {
				int colorOffset = format.getColorOffset() / 4;
				float x = Float.intBitsToFloat(vertexData[vertex]);
				float y = Float.intBitsToFloat(vertexData[vertex + 1]);
				float z = Float.intBitsToFloat(vertexData[vertex + 2]);
				int color = colorer.getColor(x + data.getX(), y + data.getY(), z + data.getZ());
				vertexData[vertex + colorOffset] = color;
			}

			quads.set(i, newQuad);
		}
		return quads;
	}

}
