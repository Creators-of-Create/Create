package com.simibubi.create.content.contraptions.relays.belt;


import java.util.Random;
import java.util.function.Supplier;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CasingType;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;

//todo: review this?
public class BeltModel extends ForwardingBakedModel {

	private static final ThreadLocal<SpriteFinder> SPRITE_FINDER = ThreadLocal.withInitial(() -> SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS)));

	public BeltModel(BakedModel template) {
		wrapped = template;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		Object attachment = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
		boolean applyTransform = false;
		if (attachment instanceof CasingType) {
			CasingType type = (CasingType) attachment;
			applyTransform = !(type == CasingType.NONE || type == CasingType.BRASS);
		}
		boolean pushed = false;
		if (applyTransform) {
			SpriteShiftEntry spriteShift = AllSpriteShifts.ANDESIDE_BELT_CASING;
			TextureAtlasSprite target = spriteShift.getTarget();
			if (target != null) {
				pushed = true;
				context.pushTransform(quad -> {
					TextureAtlasSprite original = SPRITE_FINDER.get().find(quad, 0);
					for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
						float u = quad.spriteU(vertexIndex, 0);
						float v = quad.spriteV(vertexIndex, 0);
						u = target.getU(SuperByteBuffer.getUnInterpolatedU(original, u));
						v = target.getV(SuperByteBuffer.getUnInterpolatedV(original, v));
						quad.sprite(vertexIndex, 0, u, v);
					}
					return true;
				});
			}

			//quads.set(i, newQuad);
		}
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		if (applyTransform && pushed) {
			context.popTransform();
		}
	}

//	@Override
//	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
//		List<BakedQuad> quads = super.getQuads(state, side, rand, extraData);
//		if (!extraData.hasProperty(CASING_PROPERTY))
//			return quads;
//		CasingType type = extraData.getData(CASING_PROPERTY);
//		if (type == CasingType.NONE || type == CasingType.BRASS)
//			return quads;
//		quads = new ArrayList<>(quads);
//
//		SpriteShiftEntry spriteShift = AllSpriteShifts.ANDESIDE_BELT_CASING;
//		VertexFormat format = DefaultVertexFormat.BLOCK;
//
//		for (int i = 0; i < quads.size(); i++) {
//			BakedQuad quad = quads.get(i);
//			if (spriteShift == null)
//				continue;
//			if (quad.getSprite() != spriteShift.getOriginal())
//				continue;
//
//			TextureAtlasSprite original = quad.getSprite();
//			TextureAtlasSprite target = spriteShift.getTarget();
//			BakedQuad newQuad = QuadHelper.clone(quad);
//			int[] vertexData = newQuad.getVertices();
//
//			for (int vertex = 0; vertex < vertexData.length; vertex += format.getIntegerSize()) {
//				int uvOffset = 16 / 4;
//				int uIndex = vertex + uvOffset;
//				int vIndex = vertex + uvOffset + 1;
//				float u = Float.intBitsToFloat(vertexData[uIndex]);
//				float v = Float.intBitsToFloat(vertexData[vIndex]);
//				vertexData[uIndex] =
//					Float.floatToRawIntBits(target.getU(SuperByteBuffer.getUnInterpolatedU(original, u)));
//				vertexData[vIndex] =
//					Float.floatToRawIntBits(target.getV(SuperByteBuffer.getUnInterpolatedV(original, v)));
//			}
//
//			quads.set(i, newQuad);
//		}
//		return quads;
//	}

}
