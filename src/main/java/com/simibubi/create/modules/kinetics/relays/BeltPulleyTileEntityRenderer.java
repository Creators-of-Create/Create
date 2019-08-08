package com.simibubi.create.modules.kinetics.relays;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.kinetics.base.KineticTileEntity;
import com.simibubi.create.modules.kinetics.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.client.model.data.EmptyModelData;

public class BeltPulleyTileEntityRenderer extends KineticTileEntityRenderer {

	protected static Cache<BeltPulleyTileEntity, CachedBeltBuffer> cachedBelts;
	public static IBakedModel beltModel;

	protected class CachedBeltBuffer {
		ByteBuffer original;
		ByteBuffer mutable;

		public CachedBeltBuffer(ByteBuffer original, float beltLength, float angle, Axis axis) {
			original.rewind();
			this.original = original;
			this.mutable = GLAllocation.createDirectByteBuffer(original.capacity());
			this.mutable.order(original.order());
			
			int limitBefore = this.original.limit();
			int integerSize = DefaultVertexFormats.BLOCK.getIntegerSize();
			int amtBytesCopied = 0; // 2/*Blocks*/ * 6/*Quads*/ * 4/*Vertices*/ * integerSize;
			
			this.original.limit(limitBefore + amtBytesCopied);
			this.mutable.limit(limitBefore + amtBytesCopied);
			
//			for (int i = 0; i < amtBytesCopied; i++)
//				this.original.put(i + limitBefore, this.original.get(i));
//			int vertex = limitBefore / integerSize;
//			putPos(this.original, vertex, getPos(this.original, vertex).add(0,1,0));

			mutable.put(this.original);
			this.original.rewind();
			mutable.rewind();
		}

		private Vec3d getPos(ByteBuffer buffer, int vertex) {
			return new Vec3d(buffer.getFloat(vertex), buffer.getFloat(vertex + 4), buffer.getFloat(vertex + 8));
		}

		private void putPos(ByteBuffer buffer, int vertex, Vec3d pos) {
			buffer.putFloat(vertex, (float) pos.x);
			buffer.putFloat(vertex + 4, (float) pos.y);
			buffer.putFloat(vertex + 8, (float) pos.z);
		}

		private Vec3d rotatePos(Vec3d pos, float angle, Axis axis) {
			return rotatePos(pos, MathHelper.sin(angle), MathHelper.cos(angle), axis);
		}

		private Vec3d rotatePos(Vec3d pos, float sin, float cos, Axis axis) {
			final float x = (float) pos.x;
			final float y = (float) pos.y;
			final float z = (float) pos.z;

			if (axis == Axis.X)
				return new Vec3d(x, y * cos - z * sin, z * cos + y * sin);
			if (axis == Axis.Y)
				return new Vec3d(x * cos + z * sin, y, z * cos - x * sin);
			if (axis == Axis.Z)
				return new Vec3d(x * cos - y * sin, y * cos + x * sin, z);

			return pos;
		}

		public ByteBuffer getTransformed(Vec3d translation, BeltPulleyTileEntity te, boolean flipped) {
			original.rewind();
			mutable.rewind();

			final Axis axis = te.getBlockState().get(BlockStateProperties.AXIS);
			final BlockPos diff = te.getTarget().subtract(te.getPos());
			float angle = 0;

			if (axis == Axis.X)
				angle = (float) MathHelper.atan2(-diff.getY(), diff.getZ());
			if (axis == Axis.Y)
				angle = (float) MathHelper.atan2(diff.getX(), diff.getZ());
			if (axis == Axis.Z)
				angle = (float) MathHelper.atan2(diff.getY(), diff.getX());

			final float time = Animation.getWorldTime(Minecraft.getInstance().world,
					Minecraft.getInstance().getRenderPartialTicks());
			final float progress = ((te.getSpeed() * time / 16) % 16) / 16f;
			float beltLength = (float) new Vec3d(te.getPos()).distanceTo(new Vec3d(te.getTarget())) + .5f;

			final BlockState blockState = te.getBlockState();
			final int packedLightCoords = blockState.getPackedLightmapCoords(getWorld(), te.getPos());
			final int formatLength = DefaultVertexFormats.BLOCK.getSize();
			final Vec3d half = new Vec3d(.5f, .5f, .5f);
			final float cos = MathHelper.cos(angle);
			final float sin = MathHelper.sin(angle);
			final Vec3d offset = new Vec3d(0, .25f, -.25f + progress + (te.getSpeed() < 0 ? 0 : -1));
			Vec3d trans = new Vec3d(Direction.getFacingFromAxis(AxisDirection.NEGATIVE, axis).getDirectionVec())
					.scale(.5f);
			TextureAtlasSprite sprite = Minecraft.getInstance().getTextureMap()
					.getSprite(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			final int texHeight = sprite.getHeight();

			for (int i = 0; i < original.limit() / formatLength; i++) {
				final int position = i * formatLength;

				// Cut-off
				mutable.putFloat(position + 20, original.getFloat(position + 20));

				Vec3d localPos = getPos(this.original, position).add(offset);
				if (localPos.z < -.25f) {
					mutable.putFloat(position + 20, (float) (original.getFloat(position + 20)
							- (Math.min(localPos.z + .25f, 0)) * .5f / texHeight));
					localPos = new Vec3d(localPos.x, localPos.y, -.25f);
				}
				if (localPos.z > beltLength - .25f) {
					mutable.putFloat(position + 20, (float) (original.getFloat(position + 20)
							- (Math.min(localPos.z - beltLength + .25f, 1f)) * .5f / texHeight));
					localPos = new Vec3d(localPos.x, localPos.y, beltLength - .25f);
				}

				// Transform Vertex
				Vec3d pos = localPos;

				if (axis == Axis.Z)
					pos = rotatePos(pos.subtract(half), (float) (Math.PI / 2f), Axis.Y).add(half);
				if (axis == Axis.Y)
					pos = rotatePos(pos, (float) (Math.PI / 2f), Axis.Z);

				pos = rotatePos(pos, sin, cos, axis).add(trans).add(half);

				// Flip
				if (flipped) {
					pos = rotatePos(pos.subtract(half), (float) Math.PI, axis);
					pos = pos.add(half).add(new Vec3d(diff));
				}

				putPos(mutable, position, pos.add(translation));
				mutable.putInt(position + 24, packedLightCoords);
			}
			return mutable;
		}
	}

	public BeltPulleyTileEntityRenderer() {
		super();
		cachedBelts = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.SECONDS).build();
	}

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, buffer);

		BeltPulleyTileEntity beltPulleyTE = (BeltPulleyTileEntity) te;
		if (!beltPulleyTE.isController())
			return;

		cacheBeltIfMissing(beltPulleyTE);
		renderBeltFromCache(te, new Vec3d(x, y, z), buffer);
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.AXIS.get().getDefaultState().with(BlockStateProperties.AXIS,
				te.getBlockState().get(BlockStateProperties.AXIS));
	}

	protected void cacheBeltIfMissing(BeltPulleyTileEntity te) {
		if (cachedBelts.getIfPresent(te) != null)
			return;

		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
		BufferBuilder builder = new BufferBuilder(0);
		Random random = new Random();

		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		float beltLength = (float) new Vec3d(te.getPos()).distanceTo(new Vec3d(te.getTarget())) + .5f;
		int length = MathHelper.ceil(beltLength) + 1;
		for (int segment = 0; segment < length; segment++) {
			builder.setTranslation(0, 0, segment);
			blockRenderer.renderModelFlat(getWorld(), beltModel, te.getBlockState(), BlockPos.ZERO, builder, true,
					random, 42, EmptyModelData.INSTANCE);
		}
		builder.finishDrawing();

		Axis axis = te.getBlockState().get(BlockStateProperties.AXIS);
		BlockPos diff = te.getTarget().subtract(te.getPos());
		float angle = 0;

		if (axis == Axis.X)
			angle = (float) MathHelper.atan2(-diff.getY(), diff.getZ());
		if (axis == Axis.Y)
			angle = (float) MathHelper.atan2(diff.getX(), diff.getZ());
		if (axis == Axis.Z)
			angle = (float) MathHelper.atan2(diff.getY(), diff.getX());

		cachedBelts.put(te, new CachedBeltBuffer(builder.getByteBuffer(), beltLength, angle, axis));

	}

	public void renderBeltFromCache(KineticTileEntity te, Vec3d translation, BufferBuilder buffer) {
		if (buffer == null)
			return;
		buffer.putBulkData(cachedBelts.getIfPresent(te).getTransformed(translation, (BeltPulleyTileEntity) te, false));
		buffer.putBulkData(cachedBelts.getIfPresent(te).getTransformed(translation, (BeltPulleyTileEntity) te, true));
	}
}
