package com.simibubi.create.foundation.render;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.mixin.accessor.RenderSystemAccessor;
import com.simibubi.create.foundation.utility.Color;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.util.ShadersModHandler;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

public class SuperByteBuffer implements TransformStack<SuperByteBuffer> {
	private static final Long2IntMap WORLD_LIGHT_CACHE = new Long2IntOpenHashMap();

	private final TemplateMesh template;
	private final int[] shadeSwapVertices;

	// Vertex Position and Normals
	private final PoseStack transforms = new PoseStack();

	// Vertex Coloring
	private float r, g, b, a;
	private boolean disableDiffuse;

	// Vertex Texture Coords
	@Nullable
	private SpriteShiftFunc spriteShiftFunc;

	// Vertex Overlay
	private boolean hasCustomOverlay;
	private int overlay;

	// Vertex Light
	private boolean hasCustomLight;
	private int packedLight;
	private boolean useLevelLight;
	@Nullable
	private BlockAndTintGetter levelWithLight;
	@Nullable
	private Matrix4f lightTransform;

	// Reused objects
	private final Matrix4f modelMat = new Matrix4f();
	private final Matrix3f normalMat = new Matrix3f();
	private final Vector4f pos = new Vector4f();
	private final Vector3f normal = new Vector3f();
	private final Vector3f lightDir0 = new Vector3f();
	private final Vector3f lightDir1 = new Vector3f();
	private final ShiftOutput shiftOutput = new ShiftOutput();
	private final Vector4f lightPos = new Vector4f();

	public SuperByteBuffer(TemplateMesh template, int[] shadeSwapVertices) {
		this.template = template;
		this.shadeSwapVertices = shadeSwapVertices;
		reset();
	}

	public SuperByteBuffer(TemplateMesh template) {
		this(template, new int[0]);
	}

	public void renderInto(PoseStack input, VertexConsumer builder) {
		if (isEmpty()) {
			return;
		}

		if (useLevelLight) {
			WORLD_LIGHT_CACHE.clear();
		}

		Matrix4f modelMat = this.modelMat.set(input.last()
			.pose());
		Matrix4f localTransforms = transforms.last()
			.pose();
		modelMat.mul(localTransforms);

		Matrix3f normalMat = this.normalMat.set(input.last()
			.normal());
		Matrix3f localNormalTransforms = transforms.last()
			.normal();
		normalMat.mul(localNormalTransforms);

		Vector4f pos = this.pos;
		Vector3f normal = this.normal;
		ShiftOutput shiftOutput = this.shiftOutput;
		Vector3f lightDir0 = this.lightDir0;
		Vector3f lightDir1 = this.lightDir1;
		Vector4f lightPos = this.lightPos;

		boolean applyDiffuse = !disableDiffuse && !ShadersModHandler.isShaderPackInUse();
		boolean shaded = true;
		int shadeSwapIndex = 0;
		int nextShadeSwapVertex = shadeSwapIndex < shadeSwapVertices.length ? shadeSwapVertices[shadeSwapIndex] : -1;
		float unshadedDiffuse = 1;
		if (applyDiffuse) {
			lightDir0.set(RenderSystemAccessor.create$getShaderLightDirections()[0]).normalize();
			lightDir1.set(RenderSystemAccessor.create$getShaderLightDirections()[1]).normalize();
			if (shadeSwapVertices.length > 0) {
				normal.set(0, 1, 0);
				normal.mul(normalMat);
				unshadedDiffuse = calculateDiffuse(normal, lightDir0, lightDir1);
			}
		}

		int vertexCount = template.vertexCount();
		for (int i = 0; i < vertexCount; i++) {
			if (i == nextShadeSwapVertex) {
				shaded = !shaded;
				shadeSwapIndex++;
				nextShadeSwapVertex = shadeSwapIndex < shadeSwapVertices.length ? shadeSwapVertices[shadeSwapIndex] : -1;
			}

			float x = template.x(i);
			float y = template.y(i);
			float z = template.z(i);
			pos.set(x, y, z, 1.0f);
			pos.mul(modelMat);

			int packedNormal = template.normal(i);
			float normalX = ((byte) (packedNormal & 0xFF)) / 127.0f;
			float normalY = ((byte) ((packedNormal >>> 8) & 0xFF)) / 127.0f;
			float normalZ = ((byte) ((packedNormal >>> 16) & 0xFF)) / 127.0f;
			normal.set(normalX, normalY, normalZ);
			normal.mul(normalMat);

			int color = template.color(i);
			float r = (color & 0xFF) / 255.0f * this.r;
			float g = ((color >>> 8) & 0xFF) / 255.0f * this.g;
			float b = ((color >>> 16) & 0xFF) / 255.0f * this.b;
			float a = ((color >>> 24) & 0xFF) / 255.0f * this.a;
			if (applyDiffuse) {
				float diffuse = shaded ? calculateDiffuse(normal, lightDir0, lightDir1) : unshadedDiffuse;
				r *= diffuse;
				g *= diffuse;
				b *= diffuse;
			}

			float u = template.u(i);
			float v = template.v(i);
			if (spriteShiftFunc != null) {
				spriteShiftFunc.shift(u, v, shiftOutput);
				u = shiftOutput.u;
				v = shiftOutput.v;
			}

			int overlay;
			if (hasCustomOverlay) {
				overlay = this.overlay;
			} else {
				overlay = template.overlay(i);
			}

			int light = template.light(i);
			if (hasCustomLight) {
				light = maxLight(light, packedLight);
			}
			if (useLevelLight) {
				lightPos.set(((x - .5f) * 15 / 16f) + .5f, (y - .5f) * 15 / 16f + .5f, (z - .5f) * 15 / 16f + .5f, 1f);
				lightPos.mul(localTransforms);
				if (lightTransform != null) {
					lightPos.mul(lightTransform);
				}
				light = maxLight(light, getLight(levelWithLight, lightPos));
			}

			builder.vertex(pos.x(), pos.y(), pos.z(), r, g, b, a, u, v, overlay, light, normal.x(), normal.y(), normal.z());
		}

		reset();
	}

	public SuperByteBuffer reset() {
		while (!transforms.clear())
			transforms.popPose();
		transforms.pushPose();

		r = 1;
		g = 1;
		b = 1;
		a = 1;
		disableDiffuse = false;
		spriteShiftFunc = null;
		hasCustomOverlay = false;
		overlay = OverlayTexture.NO_OVERLAY;
		hasCustomLight = false;
		packedLight = 0;
		useLevelLight = false;
		levelWithLight = null;
		lightTransform = null;
		return this;
	}

	public boolean isEmpty() {
		return template.isEmpty();
	}

	public PoseStack getTransforms() {
		return transforms;
	}

	@Override
	public SuperByteBuffer scale(float factorX, float factorY, float factorZ) {
		transforms.scale(factorX, factorY, factorZ);
		return this;
	}

	@Override
	public SuperByteBuffer rotate(Quaternionfc quaternion) {
		var last = transforms.last();
		last.pose().rotate(quaternion);
		last.normal().rotate(quaternion);
		return this;
	}

	@Override
	public SuperByteBuffer translate(float x, float y, float z) {
		transforms.translate(x, y, z);
		return this;
	}

	@Override
	public SuperByteBuffer mulPose(Matrix4fc pose) {
		transforms.last()
			.pose()
			.mul(pose);
		return this;
	}

	@Override
	public SuperByteBuffer mulNormal(Matrix3fc normal) {
		transforms.last()
			.normal()
			.mul(normal);
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

	public SuperByteBuffer color(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		return this;
	}

	public SuperByteBuffer color(int r, int g, int b, int a) {
		color(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
		return this;
	}

	public SuperByteBuffer color(int color) {
		color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, 255);
		return this;
	}

	public SuperByteBuffer color(Color c) {
		return color(c.getRGB());
	}

	public SuperByteBuffer disableDiffuse() {
		disableDiffuse = true;
		return this;
	}

	public SuperByteBuffer shiftUV(SpriteShiftEntry entry) {
		spriteShiftFunc = (u, v, output) -> {
			output.accept(entry.getTargetU(u), entry.getTargetV(v));
		};
		return this;
	}

	public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollV) {
		return shiftUVScrolling(entry, 0, scrollV);
	}

	public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollU, float scrollV) {
		spriteShiftFunc = (u, v, output) -> {
			float targetU = u - entry.getOriginal()
				.getU0() + entry.getTarget()
					.getU0()
				+ scrollU;
			float targetV = v - entry.getOriginal()
				.getV0() + entry.getTarget()
					.getV0()
				+ scrollV;
			output.accept(targetU, targetV);
		};
		return this;
	}

	public SuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize) {
		spriteShiftFunc = (u, v, output) -> {
			float targetU = entry.getTarget()
				.getU((SpriteShiftEntry.getUnInterpolatedU(entry.getOriginal(), u) / sheetSize) + uTarget * 16);
			float targetV = entry.getTarget()
				.getV((SpriteShiftEntry.getUnInterpolatedV(entry.getOriginal(), v) / sheetSize) + vTarget * 16);
			output.accept(targetU, targetV);
		};
		return this;
	}

	public SuperByteBuffer overlay(int overlay) {
		hasCustomOverlay = true;
		this.overlay = overlay;
		return this;
	}

	public SuperByteBuffer light(int packedLight) {
		hasCustomLight = true;
		this.packedLight = packedLight;
		return this;
	}

	public SuperByteBuffer useLevelLight(BlockAndTintGetter level) {
		useLevelLight = true;
		levelWithLight = level;
		return this;
	}

	public SuperByteBuffer useLevelLight(BlockAndTintGetter level, Matrix4f lightTransform) {
		useLevelLight = true;
		levelWithLight = level;
		this.lightTransform = lightTransform;
		return this;
	}

	public static int maxLight(int packedLight1, int packedLight2) {
		int blockLight1 = LightTexture.block(packedLight1);
		int skyLight1 = LightTexture.sky(packedLight1);
		int blockLight2 = LightTexture.block(packedLight2);
		int skyLight2 = LightTexture.sky(packedLight2);
		return LightTexture.pack(Math.max(blockLight1, blockLight2), Math.max(skyLight1, skyLight2));
	}

	// Adapted from minecraft:shaders/include/light.glsl
	private static float calculateDiffuse(Vector3fc normal, Vector3fc lightDir0, Vector3fc lightDir1) {
	    float light0 = Math.max(0.0f, lightDir0.dot(normal));
	    float light1 = Math.max(0.0f, lightDir1.dot(normal));
	    return Math.min(1.0f, (light0 + light1) * 0.6f + 0.4f);
	}

	private static int getLight(BlockAndTintGetter world, Vector4f lightPos) {
		BlockPos pos = BlockPos.containing(lightPos.x(), lightPos.y(), lightPos.z());
		return WORLD_LIGHT_CACHE.computeIfAbsent(pos.asLong(), $ -> LevelRenderer.getLightColor(world, pos));
	}

	@FunctionalInterface
	public interface SpriteShiftFunc {
		void shift(float u, float v, Output output);

		interface Output {
			void accept(float u, float v);
		}
	}

	private class ShiftOutput implements SpriteShiftFunc.Output {
		public float u;
		public float v;

		@Override
		public void accept(float u, float v) {
			this.u = u;
			this.v = v;
		}
	};
}
