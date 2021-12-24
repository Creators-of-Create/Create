package com.simibubi.create.foundation.block.connected;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;

import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.simibubi.create.foundation.utility.Iterate;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CTModel extends ForwardingBakedModel {

	private static final ThreadLocal<SpriteFinder> SPRITE_FINDER = ThreadLocal.withInitial(() -> SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS)));
	private ConnectedTextureBehaviour behaviour;

	private class CTData {
		int[] indices;

		public CTData() {
			indices = new int[6];
			Arrays.fill(indices, -1);
		}

		void put(Direction face, int texture) {
			indices[face.get3DDataValue()] = texture;
		}

		int get(Direction face) {
			return indices[face.get3DDataValue()];
		}
	}

	public CTModel(BakedModel originalModel, ConnectedTextureBehaviour behaviour) {
		wrapped = originalModel;
		this.behaviour = behaviour;
	}

	protected CTData createCTData(BlockAndTintGetter world, BlockPos pos, BlockState state) {
		CTData data = new CTData();
		for (Direction face : Iterate.directions) {
			if (!Block.shouldRenderFace(state, world, pos, face, pos.relative(face))
				&& !behaviour.buildContextForOccludedDirections())
				continue;
			CTSpriteShiftEntry spriteShift = behaviour.get(state, face);
			if (spriteShift == null)
				continue;
			CTContext ctContext = behaviour.buildContext(world, pos, state, face);
			data.put(face, spriteShift.getTextureIndex(ctContext));
		}
		return data;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

//	@Override
//	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
//		List<BakedQuad> quads = super.getQuads(state, side, rand, extraData);
//		if (!extraData.hasProperty(CT_PROPERTY))
//			return quads;
//		CTData data = extraData.getData(CT_PROPERTY);
//		quads = new ArrayList<>(quads);
//
//		VertexFormat format = DefaultVertexFormat.BLOCK;
//
//		for (int i = 0; i < quads.size(); i++) {
//			BakedQuad quad = quads.get(i);
//
//			CTSpriteShiftEntry spriteShift = behaviour.get(state, quad.getDirection());
//			if (spriteShift == null)
//				continue;
//			if (quad.getSprite() != spriteShift.getOriginal())
//				continue;
//			int index = data.get(quad.getDirection());
//			if (index == -1)
//				continue;
//
//			BakedQuad newQuad = QuadHelper.clone(quad);
//			int[] vertexData = newQuad.getVertices();
//
//			for (int vertex = 0; vertex < vertexData.length; vertex += format.getIntegerSize()) {
//				int uvOffset = 16 / 4;
//				int uIndex = vertex + uvOffset;
//				int vIndex = vertex + uvOffset + 1;
//				float u = Float.intBitsToFloat(vertexData[uIndex]);
//				float v = Float.intBitsToFloat(vertexData[vIndex]);
//				vertexData[uIndex] = Float.floatToRawIntBits(spriteShift.getTargetU(u, index));
//				vertexData[vIndex] = Float.floatToRawIntBits(spriteShift.getTargetV(v, index));
//			}
//
//			quads.set(i, newQuad);
//		}
//		return quads;
//	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		CTData data = createCTData(blockView, pos, state);
		context.pushTransform(quad -> {
			TextureAtlasSprite original = SPRITE_FINDER.get().find(quad, 0);
			CTSpriteShiftEntry spriteShift = behaviour.get(state, quad.lightFace());
			if (spriteShift == null)
				return true;
			if (original != spriteShift.getOriginal())
				return true;
			int index = data.get(quad.lightFace());
			if (index == -1)
				return true;
			for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
				float u = quad.spriteU(vertexIndex, 0);
				float v = quad.spriteV(vertexIndex, 0);
				u = spriteShift.getTargetU(u, index);
				v = spriteShift.getTargetV(v, index);
				quad.sprite(vertexIndex, 0, u, v);
			}
			return true;
		});
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();
	}


}
