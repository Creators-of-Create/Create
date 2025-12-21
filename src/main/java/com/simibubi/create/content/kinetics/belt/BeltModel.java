package com.simibubi.create.content.kinetics.belt;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import org.jetbrains.annotations.NotNull;

public class BeltModel extends BakedModelWrapper<BakedModel> {

	public static final ModelProperty<BeltCasingType> CASING_PROPERTY = new ModelProperty<>();
	public static final ModelProperty<Boolean> COVER_PROPERTY = new ModelProperty<>();

	private static final SpriteShiftEntry SPRITE_SHIFT = AllSpriteShifts.ANDESIDE_BELT_CASING;

	public BeltModel(BakedModel template) {
		super(template);
	}

	@Override
	public @NotNull TextureAtlasSprite getParticleIcon(ModelData data) {
		if (!data.has(CASING_PROPERTY))
			return super.getParticleIcon(data);

		BeltCasingType type = data.get(CASING_PROPERTY);
		if (type == null)
			return super.getParticleIcon(data);

		BeltCasingRenderInfo modelInfo = type.getModelInfo();
		return modelInfo.spriteShift() == null ? super.getParticleIcon(data) :
			modelInfo.spriteShift().getTarget();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
		List<BakedQuad> quads = super.getQuads(state, side, rand, extraData, renderType);
		if (!extraData.has(CASING_PROPERTY))
			return quads;

		boolean cover = extraData.get(COVER_PROPERTY);
		BeltCasingType type = extraData.get(CASING_PROPERTY);

		if (type == null)
			return quads;

		BeltCasingRenderInfo modelInfo = type.getModelInfo();
		boolean noSpriteShift = modelInfo.spriteShift() == null;

		if (noSpriteShift && !cover)
			return quads;

		quads = new ArrayList<>(quads);

		if (cover) {
			boolean alongX = state.getValue(BeltBlock.HORIZONTAL_FACING)
				.getAxis() == Axis.X;
			BakedModel coverModel = (alongX ? modelInfo.coverModelX() : modelInfo.coverModelZ()).get();
			quads.addAll(coverModel.getQuads(state, side, rand, extraData, renderType));
		}

		if (noSpriteShift)
			return quads;

		for (int i = 0; i < quads.size(); i++) {
			BakedQuad quad = quads.get(i);
			TextureAtlasSprite original = quad.getSprite();
			if (original != SPRITE_SHIFT.getOriginal())
				continue;

			BakedQuad newQuad = BakedQuadHelper.clone(quad);
			int[] vertexData = newQuad.getVertices();

			for (int vertex = 0; vertex < 4; vertex++) {
				float u = BakedQuadHelper.getU(vertexData, vertex);
				float v = BakedQuadHelper.getV(vertexData, vertex);
				BakedQuadHelper.setU(vertexData, vertex, SPRITE_SHIFT.getTargetU(u));
				BakedQuadHelper.setV(vertexData, vertex, SPRITE_SHIFT.getTargetV(v));
			}

			quads.set(i, newQuad);
		}

		return quads;
	}

}
