package com.simibubi.create.foundation.render;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.utility.MatrixStacker;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class SuperByteBuffer extends TemplateBuffer {

	public interface IVertexLighter {
		public int getPackedLight(float x, float y, float z);
	}

	// Vertex Position
	private MatrixStack transforms;

	// Vertex Texture Coords
	private SpriteShiftFunc spriteShiftFunc;
	private boolean isEntityModel;

	// Vertex Lighting
	private boolean shouldLight;
	private int packedLightCoords;
	private int otherBlockLight;
	private Matrix4f lightTransform;

	// Vertex Coloring
	private boolean shouldColor;
	private int r, g, b, a;

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
	Vector3f normal = new Vector3f();
	Vector4f lightPos = new Vector4f();

	public void renderInto(MatrixStack input, IVertexBuilder builder) {
		ByteBuffer buffer = template;
		if (((Buffer) buffer).limit() == 0)
			return;
		((Buffer) buffer).rewind();

		Matrix3f normalMat = transforms.peek()
			.getNormal()
			.copy();

		Matrix4f modelMat = input.peek()
			.getModel()
			.copy();

		Matrix4f localTransforms = transforms.peek()
			.getModel();
		modelMat.multiply(localTransforms);

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
			float normalX = getNX(buffer, i) / 127f;
			float normalY = getNY(buffer, i) / 127f;
			float normalZ = getNZ(buffer, i) / 127f;

			float staticDiffuse = LightUtil.diffuseLight(normalX, normalY, normalZ);
			normal.set(normalX, normalY, normalZ);
			normal.transform(normalMat);
			float nx = normal.getX();
			float ny = normal.getY();
			float nz = normal.getZ();
			float instanceDiffuse = LightUtil.diffuseLight(nx, ny, nz);

			pos.set(x, y, z, 1F);
			pos.transform(modelMat);
			builder.vertex(pos.getX(), pos.getY(), pos.getZ());

			if (isEntityModel) {
				builder.color(255, 255, 255, 255);
			} else if (shouldColor) {
				int colorR = Math.min(255, (int) (((float) this.r) * instanceDiffuse));
				int colorG = Math.min(255, (int) (((float) this.g) * instanceDiffuse));
				int colorB = Math.min(255, (int) (((float) this.b) * instanceDiffuse));
				builder.color(colorR, colorG, colorB, this.a);
			} else {
				float diffuseMult = instanceDiffuse / staticDiffuse;
				int colorR = Math.min(255, (int) (((float) Byte.toUnsignedInt(r)) * diffuseMult));
				int colorG = Math.min(255, (int) (((float) Byte.toUnsignedInt(g)) * diffuseMult));
				int colorB = Math.min(255, (int) (((float) Byte.toUnsignedInt(b)) * diffuseMult));
				builder.color(colorR, colorG, colorB, a);
			}

			float u = getU(buffer, i);
			float v = getV(buffer, i);

			if (spriteShiftFunc != null) {
				spriteShiftFunc.shift(builder, u, v);
			} else
				builder.texture(u, v);
			
			if (isEntityModel)
				builder.overlay(OverlayTexture.DEFAULT_UV);
			
			if (shouldLight) {
				int light = packedLightCoords;
				if (lightTransform != null) {
					lightPos.set(((x - f) * 15 / 16f) + f, (y - f) * 15 / 16f + f, (z - f) * 15 / 16f + f, 1F);
					lightPos.transform(localTransforms);
					lightPos.transform(lightTransform);

					light = getLight(Minecraft.getInstance().world, lightPos);
					if (otherBlockLight >= 0) {
						light = ContraptionRenderDispatcher.getMaxBlockLight(light, otherBlockLight);
					}
				}
				builder.light(light);
			} else
				builder.light(getLight(buffer, i));

			if (isEntityModel)
				builder.normal(input.peek().getNormal(), nx, ny, nz);
			else
				builder.normal(nx, ny, nz);
			builder.endVertex();
		}

		transforms = new MatrixStack();

		spriteShiftFunc = null;
		shouldColor = false;
		isEntityModel = false;
		shouldLight = false;
		otherBlockLight = -1;
	}

	public MatrixStacker matrixStacker() {
		return MatrixStacker.of(transforms);
	}

	public SuperByteBuffer translate(Vector3d vec) {
		return translate(vec.x, vec.y, vec.z);
	}

	public SuperByteBuffer translate(double x, double y, double z) {
		return translate((float) x, (float) y, (float) z);
	}

	public SuperByteBuffer translate(float x, float y, float z) {
		transforms.translate(x, y, z);
		return this;
	}

	public SuperByteBuffer transform(MatrixStack stack) {
		transforms.peek()
			.getModel()
			.multiply(stack.peek()
				.getModel());
		transforms.peek()
			.getNormal()
			.multiply(stack.peek()
				.getNormal());
		return this;
	}

	public SuperByteBuffer rotate(Direction axis, float radians) {
		if (radians == 0)
			return this;
		transforms.multiply(axis.getUnitVector()
			.getRadialQuaternion(radians));
		return this;
	}

	public SuperByteBuffer rotate(Quaternion q) {
		transforms.multiply(q);
		return this;
	}

	public SuperByteBuffer rotateCentered(Direction axis, float radians) {
		return translate(.5f, .5f, .5f).rotate(axis, radians)
			.translate(-.5f, -.5f, -.5f);
	}

	public SuperByteBuffer rotateCentered(Quaternion q) {
		return translate(.5f, .5f, .5f).rotate(q)
			.translate(-.5f, -.5f, -.5f);
	}

	public SuperByteBuffer shiftUV(SpriteShiftEntry entry) {
		this.spriteShiftFunc = (builder, u, v) -> {
			float targetU = entry.getTarget()
				.getInterpolatedU((getUnInterpolatedU(entry.getOriginal(), u)));
			float targetV = entry.getTarget()
				.getInterpolatedV((getUnInterpolatedV(entry.getOriginal(), v)));
			builder.texture(targetU, targetV);
		};
		return this;
	}

	public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollV) {
		this.spriteShiftFunc = (builder, u, v) -> {
			float targetU = u - entry.getOriginal()
				.getMinU() + entry.getTarget()
					.getMinU();
			float targetV = v - entry.getOriginal()
				.getMinV() + entry.getTarget()
					.getMinV()
				+ scrollV;
			builder.texture(targetU, targetV);
		};
		return this;
	}

	public SuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize) {
		this.spriteShiftFunc = (builder, u, v) -> {
			float targetU = entry.getTarget()
				.getInterpolatedU((getUnInterpolatedU(entry.getOriginal(), u) / sheetSize) + uTarget * 16);
			float targetV = entry.getTarget()
				.getInterpolatedV((getUnInterpolatedV(entry.getOriginal(), v) / sheetSize) + vTarget * 16);
			builder.texture(targetU, targetV);
		};
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

	public SuperByteBuffer light(Matrix4f lightTransform, int otherBlockLight) {
		shouldLight = true;
		this.lightTransform = lightTransform;
		this.otherBlockLight = otherBlockLight;
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
	
	public SuperByteBuffer asEntityModel() {
		isEntityModel = true;
		return this;
	}

	private static int getLight(World world, Vector4f lightPos) {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		double sky = 0, block = 0;
		pos.setPos(lightPos.getX() + 0, lightPos.getY() + 0, lightPos.getZ() + 0);
		sky += skyLightCache.computeIfAbsent(pos.toLong(), $ -> world.getLightLevel(LightType.SKY, pos));
		block += blockLightCache.computeIfAbsent(pos.toLong(), $ -> world.getLightLevel(LightType.BLOCK, pos));
		return ((int) sky) << 20 | ((int) block) << 4;
	}
	
	public boolean isEmpty() {
		return ((Buffer) template).limit() == 0;
	}

	@FunctionalInterface
	public interface SpriteShiftFunc {
		void shift(IVertexBuilder builder, float u, float v);
	}

}
