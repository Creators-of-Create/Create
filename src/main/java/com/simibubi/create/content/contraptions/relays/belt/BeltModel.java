package com.simibubi.create.content.contraptions.relays.belt;

import static com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CASING_PROPERTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CasingType;
import com.simibubi.create.foundation.block.render.QuadHelper;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;

public class BeltModel extends BakedModelWrapper<BakedModel> {

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

		SpriteShiftEntry spriteShift = AllSpriteShifts.ANDESIDE_BELT_CASING;
		VertexFormat format = DefaultVertexFormat.BLOCK;

		for (int i = 0; i < quads.size(); i++) {
			BakedQuad quad = quads.get(i);
			if (spriteShift == null)
				continue;
			if (quad.getSprite() != spriteShift.getOriginal())
				continue;

			TextureAtlasSprite original = quad.getSprite();
			TextureAtlasSprite target = spriteShift.getTarget();
			BakedQuad newQuad = QuadHelper.clone(quad);
			int[] vertexData = newQuad.getVertices();

			for (int vertex = 0; vertex < vertexData.length; vertex += format.getIntegerSize()) {
				int uvOffset = 16 / 4;
				int uIndex = vertex + uvOffset;
				int vIndex = vertex + uvOffset + 1;
				float u = Float.intBitsToFloat(vertexData[uIndex]);
				float v = Float.intBitsToFloat(vertexData[vIndex]);
				vertexData[uIndex] =
					Float.floatToRawIntBits(target.getU(SuperByteBuffer.getUnInterpolatedU(original, u)));
				vertexData[vIndex] =
					Float.floatToRawIntBits(target.getV(SuperByteBuffer.getUnInterpolatedV(original, v)));
			}

			quads.set(i, newQuad);
		}
		return quads;
	}

}
