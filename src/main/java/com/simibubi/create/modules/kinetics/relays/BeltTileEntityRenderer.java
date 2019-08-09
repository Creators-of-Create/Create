package com.simibubi.create.modules.kinetics.relays;

import java.nio.ByteBuffer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.BufferManipulator;
import com.simibubi.create.modules.kinetics.base.IRotate;
import com.simibubi.create.modules.kinetics.base.KineticTileEntity;
import com.simibubi.create.modules.kinetics.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.animation.Animation;

public class BeltTileEntityRenderer extends KineticTileEntityRenderer {

	protected static class BeltModelAnimator extends BufferManipulator {
		protected static TextureAtlasSprite beltTextures;
		protected static TextureAtlasSprite originalTexture;

		public BeltModelAnimator(ByteBuffer template) {
			super(template);

			if (beltTextures == null)
				initSprites();
		}

		private void initSprites() {
			AtlasTexture textureMap = Minecraft.getInstance().getTextureMap();

			originalTexture = textureMap.getSprite(new ResourceLocation(Create.ID, "block/belt"));
			beltTextures = textureMap.getSprite(new ResourceLocation(Create.ID, "block/belt_animated"));
		}

		public ByteBuffer getTransformed(Vec3d translation, BeltTileEntity te) {
			original.rewind();
			mutable.rewind();

			float textureOffsetX = 0;
			float textureOffsetY = 0;

			if (te.getSpeed() != 0) {
				float time = Animation.getWorldTime(Minecraft.getInstance().world,
						Minecraft.getInstance().getRenderPartialTicks());
				Direction direction = te.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
				if (direction == Direction.EAST || direction == Direction.NORTH)
					time = -time;
				int textureIndex = (int) ((te.getSpeed() * time / 8) % 16);
				if (textureIndex < 0)
					textureIndex += 16;

				textureOffsetX = beltTextures.getInterpolatedU((textureIndex % 4) * 4) - originalTexture.getMinU();
				textureOffsetY = beltTextures.getInterpolatedV((textureIndex / 4) * 4) - originalTexture.getMinV();
			}

			final BlockState blockState = te.getBlockState();
			final int packedLightCoords = blockState.getPackedLightmapCoords(te.getWorld(), te.getPos());
			final float texOffX = textureOffsetX;
			final float texOffY = textureOffsetY;
			
			forEachVertex(original, index -> {
				Vec3d pos = getPos(original, index);
				putPos(mutable, index, pos.add(translation));
				mutable.putFloat(index + 16, original.getFloat(index + 16) + texOffX);
				mutable.putFloat(index + 20, original.getFloat(index + 20) + texOffY);
				mutable.putInt(index + 24, packedLightCoords);
			});

			return mutable;
		}
	}

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		BeltTileEntity beltEntity = (BeltTileEntity) te;

		if (beltEntity.hasPulley())
			super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, buffer);

		cacheIfMissing(beltEntity.getBlockState(), BeltModelAnimator::new);
		renderBeltFromCache(beltEntity, new Vec3d(x, y, z), buffer);
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.AXIS.get().getDefaultState().with(BlockStateProperties.AXIS,
				((IRotate) AllBlocks.BELT.get()).getRotationAxis(te.getBlockState()));
	}

	public void renderBeltFromCache(BeltTileEntity te, Vec3d translation, BufferBuilder buffer) {
		buffer.putBulkData(((BeltModelAnimator) cachedBuffers.get(te.getBlockState())).getTransformed(translation, te));
	}
}
