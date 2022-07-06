package com.simibubi.create.content.contraptions.relays.belt;

import static com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CASING_PROPERTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CasingType;
import com.simibubi.create.foundation.block.render.QuadHelper;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;

public class BeltModel extends BakedModelWrapper<BakedModel> {

	private static final SpriteShiftEntry SPRITE_SHIFT = AllSpriteShifts.ANDESIDE_BELT_CASING;

	public BeltModel(BakedModel template) {
		super(template);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
		List<BakedQuad> quads = super.getQuads(state, side, rand, extraData);
		if (!extraData.hasProperty(CASING_PROPERTY))
			return quads;
		CasingType type = extraData.getData(CASING_PROPERTY);
		if (type == CasingType.NONE || type == CasingType.BRASS)
			return quads;

		quads = new ArrayList<>(quads);

		for (int i = 0; i < quads.size(); i++) {
			BakedQuad quad = quads.get(i);
			TextureAtlasSprite original = quad.getSprite();
			if (original != SPRITE_SHIFT.getOriginal())
				continue;

			BakedQuad newQuad = QuadHelper.clone(quad);
			int[] vertexData = newQuad.getVertices();

			for (int vertex = 0; vertex < 4; vertex++) {
				float u = QuadHelper.getU(vertexData, vertex);
				float v = QuadHelper.getV(vertexData, vertex);
				QuadHelper.setU(vertexData, vertex, SPRITE_SHIFT.getTargetU(u));
				QuadHelper.setV(vertexData, vertex, SPRITE_SHIFT.getTargetV(v));
			}

			quads.set(i, newQuad);
		}

		return quads;
	}

}
