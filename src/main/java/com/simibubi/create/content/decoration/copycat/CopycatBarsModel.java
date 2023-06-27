package com.simibubi.create.content.decoration.copycat;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class CopycatBarsModel extends CopycatModel {

	public CopycatBarsModel(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return false;
	}
	
	@Override
	protected List<BakedQuad> getCroppedQuads(BlockState state, Direction side, RandomSource rand, BlockState material,
		ModelData wrappedData, RenderType renderType) {
		BakedModel model = getModelOf(material);
		List<BakedQuad> templateQuads = model.getQuads(material, null, rand, wrappedData, renderType);
		List<BakedQuad> superQuads = originalModel.getQuads(state, side, rand, wrappedData, renderType);
		List<BakedQuad> quads = new ArrayList<>();
		TextureAtlasSprite targetSprite = model.getParticleIcon(wrappedData);

		boolean vertical = state.getValue(CopycatPanelBlock.FACING)
			.getAxis() == Axis.Y;

		if (side != null && (vertical || side.getAxis() == Axis.Y))
			for (int i = 0; i < templateQuads.size(); i++) {
				BakedQuad quad = templateQuads.get(i);
				if (quad.getDirection() != Direction.UP)
					continue;
				targetSprite = quad.getSprite();
				break;
			}

		if (targetSprite == null)
			return superQuads;

		for (int i = 0; i < superQuads.size(); i++) {
			BakedQuad quad = superQuads.get(i);
			TextureAtlasSprite original = quad.getSprite();
			BakedQuad newQuad = BakedQuadHelper.clone(quad);
			int[] vertexData = newQuad.getVertices();
			for (int vertex = 0; vertex < 4; vertex++) {
				BakedQuadHelper.setU(vertexData, vertex, targetSprite
					.getU(SpriteShiftEntry.getUnInterpolatedU(original, BakedQuadHelper.getU(vertexData, vertex))));
				BakedQuadHelper.setV(vertexData, vertex, targetSprite
					.getV(SpriteShiftEntry.getUnInterpolatedV(original, BakedQuadHelper.getV(vertexData, vertex))));
			}
			quads.add(newQuad);
		}

		return quads;
	}

}
