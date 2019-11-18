package com.simibubi.create.modules.contraptions.relays.belt;

import java.nio.ByteBuffer;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.BufferManipulator;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public class BeltModelAnimator extends BufferManipulator {
	protected static TextureAtlasSprite beltTextures;
	protected static TextureAtlasSprite originalTexture;

	public BeltModelAnimator(ByteBuffer template) {
		super(template);
	}

	private void initSprites() {
		AtlasTexture textureMap = Minecraft.getInstance().getTextureMap();
		originalTexture = textureMap.getSprite(new ResourceLocation(Create.ID, "block/belt"));
		beltTextures = textureMap.getSprite(new ResourceLocation(Create.ID, "block/belt_animated"));
	}

	public ByteBuffer getTransformed(BeltTileEntity te, float x, float y, float z, int color) {
		original.rewind();
		mutable.rewind();

		float textureOffsetX = 0;
		float textureOffsetY = 0;

		if (te.getSpeed() != 0) {
			float time = AnimationTickHolder.getRenderTick();
			Direction direction = te.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
			if (direction == Direction.EAST || direction == Direction.NORTH)
				time = -time;
			int textureIndex = (int) ((te.getSpeed() * time / 8) % 16);
			if (textureIndex < 0)
				textureIndex += 16;

			if (beltTextures == null)
				initSprites();
			textureOffsetX = beltTextures.getInterpolatedU((textureIndex % 4) * 4) - originalTexture.getMinU();
			textureOffsetY = beltTextures.getInterpolatedV((textureIndex / 4) * 4) - originalTexture.getMinV();
		}

		final BlockState blockState = te.getBlockState();
		int packedLightCoords = blockState.getPackedLightmapCoords(te.getWorld(), te.getPos());
		float texOffX = textureOffsetX;
		float texOffY = textureOffsetY;

		boolean defaultColor = color == -1;
		int b = defaultColor ? 128 : color & 0xFF;
		int g = defaultColor ? 128 : (color >> 8) & 0xFF;
		int r = defaultColor ? 128 : (color >> 16) & 0xFF;

		for (int vertex = 0; vertex < vertexCount(original); vertex++) {
			putPos(mutable, vertex, getX(original, vertex) + x, getY(original, vertex) + y, getZ(original, vertex) + z);
			putLight(mutable, vertex, packedLightCoords);

			int bufferPosition = getBufferPosition(vertex);
			mutable.putFloat(bufferPosition + 16, original.getFloat(bufferPosition + 16) + texOffX);
			mutable.putFloat(bufferPosition + 20, original.getFloat(bufferPosition + 20) + texOffY);

			byte lumByte = getR(original, vertex);
			float lum = (lumByte < 0 ? 255 + lumByte : lumByte) / 256f;

			int r2 = (int) (r * lum);
			int g2 = (int) (g * lum);
			int b2 = (int) (b * lum);
			putColor(mutable, vertex, (byte) r2, (byte) g2, (byte) b2, (byte) 255);
		}

		return mutable;
	}

	public static void invalidateCache() {
		beltTextures = null;
	}
}