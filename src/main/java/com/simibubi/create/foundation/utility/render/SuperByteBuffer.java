package com.simibubi.create.foundation.utility.render;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class SuperByteBuffer extends TemplateBuffer {

	public interface IVertexLighter {
		public int getPackedLight(float x, float y, float z);
	}

	// Vertex Position
	private MatrixStack transforms;

	// Vertex Texture Coords
	private boolean shouldShiftUV;
	private SpriteShiftEntry spriteShift;
	private float uTarget, vTarget;

	// Vertex Lighting
	private boolean shouldLight;
	private int packedLightCoords;
	private Matrix4f lightTransform;

	// Vertex Coloring
	private boolean shouldColor;
	private int r, g, b, a;
	private float sheetSize;

	public SuperByteBuffer(BufferBuilder buf) {
		super(buf);
		transforms = new MatrixStack();
	}

	public static float getUnInterpolatedU(TextureAtlasSprite sprite, float u) {
		float f = sprite.getMaxU() - sprite.getMinU();
		return (u - sprite.getMinU()) / f * 16.0F;
	}

	public static float getUnInterpolatedV(TextureAtlasSprite sprite, float v) {
		float f = sprite.getMaxV() - sprite.getMinV();
		return (v - sprite.getMinV()) / f * 16.0F;
	}

	private static final Long2DoubleMap skyLightCache = new Long2DoubleOpenHashMap();
	private static final Long2DoubleMap blockLightCache = new Long2DoubleOpenHashMap();
	Vector4f pos = new Vector4f();
	Vector4f lightPos = new Vector4f();

	public void renderInto(MatrixStack input, IVertexBuilder builder) {
		ByteBuffer buffer = template;
		if (((Buffer) buffer).limit() == 0)
			return;
		((Buffer) buffer).rewind();

		Matrix4f t = input.peek()
			.getModel()
			.copy();
		Matrix4f localTransforms = transforms.peek()
			.getModel();
		t.multiply(localTransforms);

		if (shouldLight && lightTransform != null) {
			skyLightCache.clear();
			blockLightCache.clear();
		}

		float f = .5f;
		int vertexCount = vertexCount(buffer);
		for (int i = 0; i < vertexCount; i++) {
			float x = getX(buffer, i);
			float y = getY(buffer, i);
			float z = getZ(buffer, i);
			byte r = getR(buffer, i);
			byte g = getG(buffer, i);
			byte b = getB(buffer, i);
			byte a = getA(buffer, i);

			pos.set(x, y, z, 1F);
			pos.transform(t);
			builder.vertex(pos.getX(), pos.getY(), pos.getZ());

			if (shouldColor) {
				float lum = (r < 0 ? 255 + r : r) / 256f;
				builder.color((int) (this.r * lum), (int) (this.g * lum), (int) (this.b * lum), this.a);
			} else
				builder.color(r, g, b, a);

			float u = getU(buffer, i);
			float v = getV(buffer, i);

			if (shouldShiftUV) {
				float targetU = spriteShift.getTarget()
					.getInterpolatedU((getUnInterpolatedU(spriteShift.getOriginal(), u) / sheetSize) + uTarget * 16);
				float targetV = spriteShift.getTarget()
					.getInterpolatedV((getUnInterpolatedV(spriteShift.getOriginal(), v) / sheetSize) + vTarget * 16);
				builder.texture(targetU, targetV);
			} else
				builder.texture(u, v);

			if (shouldLight) {
				int light = packedLightCoords;
				if (lightTransform != null) {
					lightPos.set(((x - f) * 15 / 16f) + f, (y - f) * 15 / 16f + f, (z - f) * 15 / 16f + f, 1F);
					lightPos.transform(localTransforms);
					lightPos.transform(lightTransform);
					light = getLight(Minecraft.getInstance().world, lightPos);
				}
				builder.light(light);
			} else
				builder.light(getLight(buffer, i));

			builder.normal(getNX(buffer, i), getNY(buffer, i), getNZ(buffer, i))
				.endVertex();
		}

		transforms = new MatrixStack();
		shouldShiftUV = false;
		shouldColor = false;
		shouldLight = false;
	}

	public SuperByteBuffer translate(double x, double y, double z) {
		return translate((float) x, (float) y, (float) z);
	}

	public SuperByteBuffer translate(float x, float y, float z) {
		transforms.translate(x, y, z);
		return this;
	}

	public SuperByteBuffer rotate(Direction axis, float radians) {
		if (radians == 0)
			return this;
		transforms.multiply(axis.getUnitVector()
			.getRadialQuaternion(radians));
		return this;
	}

	public SuperByteBuffer rotateCentered(Direction axis, float radians) {
		return translate(.5f, .5f, .5f).rotate(axis, radians)
			.translate(-.5f, -.5f, -.5f);
	}

	public SuperByteBuffer shiftUV(SpriteShiftEntry entry) {
		shouldShiftUV = true;
		spriteShift = entry;
		uTarget = 0;
		vTarget = 0;
		sheetSize = 1;
		return this;
	}

	public SuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize) {
		shouldShiftUV = true;
		spriteShift = entry;
		this.uTarget = uTarget;
		this.vTarget = vTarget;
		this.sheetSize = sheetSize;
		return this;
	}

	public SuperByteBuffer light(int packedLightCoords) {
		shouldLight = true;
		lightTransform = null;
		this.packedLightCoords = packedLightCoords;
		return this;
	}

	public SuperByteBuffer light(Matrix4f lightTransform) {
		shouldLight = true;
		this.lightTransform = lightTransform;
		return this;
	}

	public SuperByteBuffer color(int color) {
		shouldColor = true;
		r = ((color >> 16) & 0xFF);
		g = ((color >> 8) & 0xFF);
		b = (color & 0xFF);
		a = 255;
		return this;
	}

	private static int getLight(World world, Vector4f lightPos) {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		double sky = 0, block = 0;
		float offset = 1 / 8f;
//		for (float zOffset = offset; zOffset >= -offset; zOffset -= 2 * offset) {
//			for (float yOffset = offset; yOffset >= -offset; yOffset -= 2 * offset) {
//				for (float xOffset = offset; xOffset >= -offset; xOffset -= 2 * offset) {
//					pos.setPos(lightPos.getX() + xOffset, lightPos.getY() + yOffset, lightPos.getZ() + zOffset);
		pos.setPos(lightPos.getX() + 0, lightPos.getY() + 0, lightPos.getZ() + 0);
		sky += skyLightCache.computeIfAbsent(pos.toLong(), $ -> world.getLightLevel(LightType.SKY, pos));
		block += blockLightCache.computeIfAbsent(pos.toLong(), $ -> world.getLightLevel(LightType.BLOCK, pos));
//				}
//			}
//		}

		return ((int) sky) << 20 | ((int) block) << 4;
	}

	public boolean isEmpty() {
		return ((Buffer) template).limit() == 0;
	}

}
