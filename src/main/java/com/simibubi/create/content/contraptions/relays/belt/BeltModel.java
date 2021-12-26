package com.simibubi.create.content.contraptions.relays.belt;

import java.util.Random;
import java.util.function.Supplier;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CasingType;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class BeltModel extends ForwardingBakedModel {

	public BeltModel(BakedModel template) {
		wrapped = template;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		boolean applyTransform = false;
		if (blockView instanceof RenderAttachedBlockView attachmentProvider) {
			Object attachment = attachmentProvider.getBlockEntityRenderAttachment(pos);
			if (attachment instanceof CasingType casingType) {
				if (casingType != CasingType.NONE && casingType != CasingType.BRASS) {
					applyTransform =  true;
				}
			}
		}
		if (applyTransform) {
			TextureAtlasSprite original = AllSpriteShifts.ANDESIDE_BELT_CASING.getOriginal();
			TextureAtlasSprite target = AllSpriteShifts.ANDESIDE_BELT_CASING.getTarget();
			SpriteFinder spriteFinder = SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS));
			context.pushTransform(quad -> {
				TextureAtlasSprite sprite = spriteFinder.find(quad, 0);
				if (sprite == original) {
					for (int vertex = 0; vertex < 4; vertex++) {
						float u = quad.spriteU(vertex, 0);
						float v = quad.spriteV(vertex, 0);
						quad.sprite(vertex, 0,
								target.getU(SuperByteBuffer.getUnInterpolatedU(original, u)),
								target.getV(SuperByteBuffer.getUnInterpolatedV(original, v))
						);
					}
				}
				return true;
			});
		}
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		if (applyTransform) {
			context.popTransform();
		}
	}

}
