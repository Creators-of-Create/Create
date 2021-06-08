package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.utility.MatrixStacker;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class SuperByteBuffer {

	private final BufferBuilderReader template;

	// Vertex Position
	private MatrixStack transforms;

	// Vertex Texture Coords
	private SpriteShiftFunc spriteShiftFunc;

	// Vertex Coloring
	private boolean shouldColor;
	private int r, g, b, a;

	// Vertex Lighting
	private boolean useWorldLight;
	private boolean hybridLight;
	private int packedLightCoords;
	private Matrix4f lightTransform;

	public SuperByteBuffer(BufferBuilder buf) {
		template = new BufferBuilderReader(buf);
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

	private static final Long2IntMap WORLD_LIGHT_CACHE = new Long2IntOpenHashMap();
	Vector4f pos = new Vector4f();
	Vector3f normal = new Vector3f();
	Vector4f lightPos = new Vector4f();

	public void renderInto(MatrixStack input, IVertexBuilder builder) {
		if (isEmpty())
			return;

		Matrix3f normalMat = transforms.peek()
				.getNormal()
				.copy();

		Matrix4f modelMat = input.peek()
				.getModel()
				.copy();

		Matrix4f localTransforms = transforms.peek()
				.getModel();
		modelMat.multiply(localTransforms);

		if (useWorldLight) {
			WORLD_LIGHT_CACHE.clear();
		}

		boolean hasDefaultLight = packedLightCoords != 0;
		float f = .5f;
		int vertexCount = template.getVertexCount();
		for (int i = 0; i < vertexCount; i++) {
			float x = template.getX(i);
			float y = template.getY(i);
			float z = template.getZ(i);
			byte r = template.getR(i);
			byte g = template.getG(i);
			byte b = template.getB(i);
			byte a = template.getA(i);
			float normalX = template.getNX(i) / 127f;
			float normalY = template.getNY(i) / 127f;
			float normalZ = template.getNZ(i) / 127f;

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

			// builder.color((byte) Math.max(0, nx * 255), (byte) Math.max(0, ny * 255), (byte) Math.max(0, nz * 255), a);
			if (shouldColor) {
				// float lum = (r < 0 ? 255 + r : r) / 256f;
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

			float u = template.getU(i);
			float v = template.getV(i);

			if (spriteShiftFunc != null) {
				spriteShiftFunc.shift(builder, u, v);
			} else
				builder.texture(u, v);

			int light;
			if (useWorldLight) {
				lightPos.set(((x - f) * 15 / 16f) + f, (y - f) * 15 / 16f + f, (z - f) * 15 / 16f + f, 1F);
				lightPos.transform(localTransforms);
				if (lightTransform != null) {
					lightPos.transform(lightTransform);
				}

				light = getLight(Minecraft.getInstance().world, lightPos);
				if (hasDefaultLight) {
					light = maxLight(light, packedLightCoords);
				}
			} else if (hasDefaultLight) {
				light = packedLightCoords;
			} else {
				light = template.getLight(i);
			}

			if (hybridLight) {
				builder.light(maxLight(light, template.getLight(i)));
			} else {
				builder.light(light);
			}

			builder.normal(nx, ny, nz)
				.endVertex();
		}

		reset();
	}

	public SuperByteBuffer reset() {
		transforms = new MatrixStack();
		spriteShiftFunc = null;
		shouldColor = false;
		r = 0;
		g = 0;
		b = 0;
		a = 0;
		useWorldLight = false;
		hybridLight = false;
		packedLightCoords = 0;
		lightTransform = null;
		return this;
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

	public SuperByteBuffer color(int color) {
		shouldColor = true;
		r = ((color >> 16) & 0xFF);
		g = ((color >> 8) & 0xFF);
		b = (color & 0xFF);
		a = 255;
		return this;
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

	public SuperByteBuffer light() {
		useWorldLight = true;
		return this;
	}

	public SuperByteBuffer light(Matrix4f lightTransform) {
		useWorldLight = true;
		this.lightTransform = lightTransform;
		return this;
	}

	public SuperByteBuffer light(int packedLightCoords) {
		this.packedLightCoords = packedLightCoords;
		return this;
	}

	public SuperByteBuffer light(Matrix4f lightTransform, int packedLightCoords) {
		useWorldLight = true;
		this.lightTransform = lightTransform;
		this.packedLightCoords = packedLightCoords;
		return this;
	}

	public SuperByteBuffer hybridLight() {
		hybridLight = true;
		return this;
	}

	public static int maxLight(int packedLight1, int packedLight2) {
		int blockLight1 = LightTexture.getBlockLightCoordinates(packedLight1);
		int skyLight1 = LightTexture.getSkyLightCoordinates(packedLight1);
		int blockLight2 = LightTexture.getBlockLightCoordinates(packedLight2);
		int skyLight2 = LightTexture.getSkyLightCoordinates(packedLight2);
		return LightTexture.pack(Math.max(blockLight1, blockLight2), Math.max(skyLight1, skyLight2));
	}

	private static int getLight(World world, Vector4f lightPos) {
		BlockPos pos = new BlockPos(lightPos.getX(), lightPos.getY(), lightPos.getZ());
		return WORLD_LIGHT_CACHE.computeIfAbsent(pos.toLong(), $ -> WorldRenderer.getLightmapCoordinates(world, pos));
	}

	public boolean isEmpty() {
		return template.isEmpty();
	}

	@FunctionalInterface
	public interface SpriteShiftFunc {
		void shift(IVertexBuilder builder, float u, float v);
	}

	@FunctionalInterface
	public interface IVertexLighter {
		public int getPackedLight(float x, float y, float z);
	}

}
