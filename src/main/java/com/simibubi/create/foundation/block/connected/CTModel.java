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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CTModel extends ForwardingBakedModel {

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

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		CTData data = createCTData(blockView, pos, state);

		SpriteFinder spriteFinder = SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS));
		context.pushTransform(quad -> {
			CTSpriteShiftEntry spriteShift = behaviour.get(state, quad.lightFace());
			if (spriteShift != null) {
				TextureAtlasSprite sprite = spriteFinder.find(quad, 0);
				if (sprite == spriteShift.getOriginal()) {
					int index = data.get(quad.lightFace());
					if (index != -1) {
						for (int vertex = 0; vertex < 4; vertex++) {
							float u = quad.spriteU(vertex, 0);
							float v = quad.spriteU(vertex, 0);
							quad.sprite(vertex, 0,
									spriteShift.getTargetU(u, index),
									spriteShift.getTargetV(v, index)
							);
						}
					}
				}
			}
			return true;
		});
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();
	}

}
