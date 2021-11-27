package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.jozufozu.flywheel.util.transform.Rotate;
import com.jozufozu.flywheel.util.transform.Scale;
import com.jozufozu.flywheel.util.transform.TStack;
import com.jozufozu.flywheel.util.transform.Translate;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.utility.Color;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class SuperByteBuffer implements Scale<SuperByteBuffer>, Translate<SuperByteBuffer>, Rotate<SuperByteBuffer>, TStack<SuperByteBuffer> {

	private final BufferBuilderReader template;

	// Vertex Position
	private PoseStack transforms;

	// Vertex Coloring
	private boolean shouldColor;
	private int r, g, b, a;
	private boolean disableDiffuseDiv;
	private boolean disableDiffuseMult;

	// Vertex Texture Coords
	private SpriteShiftFunc spriteShiftFunc;

	// Vertex Overlay Color
	private boolean hasOverlay;
	private int overlay = OverlayTexture.NO_OVERLAY;;

	// Vertex Lighting
	private boolean useWorldLight;
	private Matrix4f lightTransform;
	private boolean hasCustomLight;
	private int packedLightCoords;
	private boolean hybridLight;

	// Vertex Normals
	private boolean fullNormalTransform;

	// Temporary
	private static final Long2IntMap WORLD_LIGHT_CACHE = new Long2IntOpenHashMap();
	private final Vector4f pos = new Vector4f();
	private final Vector3f normal = new Vector3f();
	private final Vector4f lightPos = new Vector4f();

	public SuperByteBuffer(BufferBuilder buf) {
		template = new BufferBuilderReader(buf);
		transforms = new PoseStack();
		transforms.pushPose();
	}

	public static float getUnInterpolatedU(TextureAtlasSprite sprite, float u) {
		float f = sprite.getU1() - sprite.getU0();
		return (u - sprite.getU0()) / f * 16.0F;
	}

	public static float getUnInterpolatedV(TextureAtlasSprite sprite, float v) {
		float f = sprite.getV1() - sprite.getV0();
		return (v - sprite.getV0()) / f * 16.0F;
	}

	public void renderInto(PoseStack input, VertexConsumer builder) {
		if (isEmpty())
			return;

		Matrix4f modelMat = input.last()
				.pose()
				.copy();
		Matrix4f localTransforms = transforms.last()
				.pose();
		modelMat.multiply(localTransforms);

		Matrix3f normalMat;
		if (fullNormalTransform) {
			normalMat = input.last().normal().copy();
			Matrix3f localNormalTransforms = transforms.last().normal();
			normalMat.mul(localNormalTransforms);
		} else {
			normalMat = transforms.last().normal().copy();
		}

		if (useWorldLight) {
			WORLD_LIGHT_CACHE.clear();
		}

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

			normal.set(normalX, normalY, normalZ);
			normal.transform(normalMat);
			float nx = normal.x();
			float ny = normal.y();
			float nz = normal.z();

			float staticDiffuse = LightUtil.diffuseLight(normalX, normalY, normalZ);
			float instanceDiffuse = LightUtil.diffuseLight(nx, ny, nz);

			pos.set(x, y, z, 1F);
			pos.transform(modelMat);
			builder.vertex(pos.x(), pos.y(), pos.z());

			if (shouldColor) {
				if (disableDiffuseMult) {
					builder.color(this.r, this.g, this.b, this.a);
				} else {
					int colorR = transformColor(this.r, instanceDiffuse);
					int colorG = transformColor(this.g, instanceDiffuse);
					int colorB = transformColor(this.b, instanceDiffuse);
					builder.color(colorR, colorG, colorB, this.a);
				}
			} else {
				if (disableDiffuseDiv && disableDiffuseMult) {
					builder.color(r, g, b, a);
				} else {
					float diffuseMult;
					if (disableDiffuseDiv) {
						diffuseMult = instanceDiffuse;
					} else if (disableDiffuseMult) {
						diffuseMult = 1 / staticDiffuse;
					} else {
						diffuseMult = instanceDiffuse / staticDiffuse;
					}
					int colorR = transformColor(r, diffuseMult);
					int colorG = transformColor(g, diffuseMult);
					int colorB = transformColor(b, diffuseMult);
					builder.color(colorR, colorG, colorB, a);
				}
			}

			float u = template.getU(i);
			float v = template.getV(i);
			if (spriteShiftFunc != null) {
				spriteShiftFunc.shift(builder, u, v);
			} else {
				builder.uv(u, v);
			}

			if (hasOverlay) {
				builder.overlayCoords(overlay);
			}

			int light;
			if (useWorldLight) {
				lightPos.set(((x - f) * 15 / 16f) + f, (y - f) * 15 / 16f + f, (z - f) * 15 / 16f + f, 1F);
				lightPos.transform(localTransforms);
				if (lightTransform != null) {
					lightPos.transform(lightTransform);
				}

				light = getLight(Minecraft.getInstance().level, lightPos);
				if (hasCustomLight) {
					light = maxLight(light, packedLightCoords);
				}
			} else if (hasCustomLight) {
				light = packedLightCoords;
			} else {
				light = template.getLight(i);
			}

			if (hybridLight) {
				builder.uv2(maxLight(light, template.getLight(i)));
			} else {
				builder.uv2(light);
			}

			builder.normal(nx, ny, nz);

			builder.endVertex();
		}

		reset();
	}

	public SuperByteBuffer reset() {
		while (!transforms.clear())
			transforms.popPose();
		transforms.pushPose();

		shouldColor = false;
		r = 0;
		g = 0;
		b = 0;
		a = 0;
		disableDiffuseDiv = false;
		disableDiffuseMult = false;
		spriteShiftFunc = null;
		hasOverlay = false;
		overlay = OverlayTexture.NO_OVERLAY;
		useWorldLight = false;
		lightTransform = null;
		hasCustomLight = false;
		packedLightCoords = 0;
		hybridLight = false;
		fullNormalTransform = false;
		return this;
	}

	@Override
	public SuperByteBuffer translate(double x, double y, double z) {
		transforms.translate(x, y, z);
		return this;
	}

	@Override
	public SuperByteBuffer multiply(Quaternion quaternion) {
		transforms.mulPose(quaternion);
		return this;
	}

	public SuperByteBuffer transform(PoseStack stack) {
		transforms.last()
			.pose()
			.multiply(stack.last()
				.pose());
		transforms.last()
			.normal()
			.mul(stack.last()
				.normal());
		return this;
	}

	public SuperByteBuffer rotateCentered(Direction axis, float radians) {
		translate(.5f, .5f, .5f).rotate(axis, radians)
			.translate(-.5f, -.5f, -.5f);
		return this;
	}

	public SuperByteBuffer rotateCentered(Quaternion q) {
		translate(.5f, .5f, .5f).multiply(q)
			.translate(-.5f, -.5f, -.5f);
		return this;
	}

	public SuperByteBuffer color(int r, int g, int b, int a) {
		shouldColor = true;
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
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

	public SuperByteBuffer color(Color c) {
		return color(c.getRGB());
	}

	/**
	 * Prevents vertex colors from being divided by the diffuse value calculated from the raw untransformed normal vector.
	 * Useful when passed vertex colors do not have diffuse baked in.
	 * Disabled when custom color is used.
	 */
	public SuperByteBuffer disableDiffuseDiv() {
		disableDiffuseDiv = true;
		return this;
	}

	/**
	 * Prevents vertex colors from being multiplied by the diffuse value calculated from the final transformed normal vector.
	 * Useful for entity rendering, when diffuse is applied automatically later.
	 */
	public SuperByteBuffer disableDiffuseMult() {
		disableDiffuseMult = true;
		return this;
	}

	public SuperByteBuffer shiftUV(SpriteShiftEntry entry) {
		this.spriteShiftFunc = (builder, u, v) -> {
			float targetU = entry.getTarget()
				.getU((getUnInterpolatedU(entry.getOriginal(), u)));
			float targetV = entry.getTarget()
				.getV((getUnInterpolatedV(entry.getOriginal(), v)));
			builder.uv(targetU, targetV);
		};
		return this;
	}

	public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollV) {
		this.spriteShiftFunc = (builder, u, v) -> {
			float targetU = u - entry.getOriginal()
				.getU0() + entry.getTarget()
					.getU0();
			float targetV = v - entry.getOriginal()
				.getV0() + entry.getTarget()
					.getV0()
				+ scrollV;
			builder.uv(targetU, targetV);
		};
		return this;
	}

	public SuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize) {
		this.spriteShiftFunc = (builder, u, v) -> {
			float targetU = entry.getTarget()
				.getU((getUnInterpolatedU(entry.getOriginal(), u) / sheetSize) + uTarget * 16);
			float targetV = entry.getTarget()
				.getV((getUnInterpolatedV(entry.getOriginal(), v) / sheetSize) + vTarget * 16);
			builder.uv(targetU, targetV);
		};
		return this;
	}

	public SuperByteBuffer overlay() {
		hasOverlay = true;
		return this;
	}

	public SuperByteBuffer overlay(int overlay) {
		hasOverlay = true;
		this.overlay = overlay;
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
		hasCustomLight = true;
		this.packedLightCoords = packedLightCoords;
		return this;
	}

	public SuperByteBuffer light(Matrix4f lightTransform, int packedLightCoords) {
		light(lightTransform);
		light(packedLightCoords);
		return this;
	}

	/**
	 * Uses max light from calculated light (world light or custom light) and vertex light for the final light value.
	 * Ineffective if any other light method was not called.
	 */
	public SuperByteBuffer hybridLight() {
		hybridLight = true;
		return this;
	}

	/**
	 * Transforms normals not only by the local matrix stack, but also by the passed matrix stack.
	 */
	public SuperByteBuffer fullNormalTransform() {
		fullNormalTransform = true;
		return this;
	}

	public SuperByteBuffer forEntityRender() {
		disableDiffuseMult();
		overlay();
		fullNormalTransform();
		return this;
	}

	public boolean isEmpty() {
		return template.isEmpty();
	}

	public static int transformColor(byte component, float scale) {
		return Mth.clamp((int) (Byte.toUnsignedInt(component) * scale), 0, 255);
	}

	public static int transformColor(int component, float scale) {
		return Mth.clamp((int) (component * scale), 0, 255);
	}

	public static int maxLight(int packedLight1, int packedLight2) {
		int blockLight1 = LightTexture.block(packedLight1);
		int skyLight1 = LightTexture.sky(packedLight1);
		int blockLight2 = LightTexture.block(packedLight2);
		int skyLight2 = LightTexture.sky(packedLight2);
		return LightTexture.pack(Math.max(blockLight1, blockLight2), Math.max(skyLight1, skyLight2));
	}

	private static int getLight(Level world, Vector4f lightPos) {
		BlockPos pos = new BlockPos(lightPos.x(), lightPos.y(), lightPos.z());
		return WORLD_LIGHT_CACHE.computeIfAbsent(pos.asLong(), $ -> LevelRenderer.getLightColor(world, pos));
	}

	@Override
	public SuperByteBuffer scale(float factorX, float factorY, float factorZ) {
		transforms.scale(factorX, factorY, factorZ);
		return this;
	}

	@Override
	public SuperByteBuffer pushPose() {
		transforms.pushPose();
		return this;
	}

	@Override
	public SuperByteBuffer popPose() {
		transforms.popPose();
		return this;
	}

	@FunctionalInterface
	public interface SpriteShiftFunc {
		void shift(VertexConsumer builder, float u, float v);
	}

	@FunctionalInterface
	public interface IVertexLighter {
		public int getPackedLight(float x, float y, float z);
	}

}
