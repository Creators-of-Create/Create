package com.simibubi.create.foundation.render;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jozufozu.flywheel.lib.math.DiffuseLightCalculator;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.utility.Color;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SuperByteBuffer implements TransformStack<SuperByteBuffer> {
	private final TemplateMesh template;
	private final int unshadedStartVertex;

	// Vertex Position and Normals
	private final PoseStack transforms = new PoseStack();

	// Vertex Coloring
	private boolean shouldColor;
	private float r, g, b, a;
	private boolean disableDiffuseMult;
	private DiffuseLightCalculator diffuseCalculator;

	// Vertex Texture Coords
	private SpriteShiftFunc spriteShiftFunc;

	// Vertex Overlay Color
	private int overlay = OverlayTexture.NO_OVERLAY;

	// Vertex Lighting
	private boolean useWorldLight;
	private Matrix4f lightTransform;
	private boolean hasCustomLight;
	private int packedLightCoords;
	private boolean hybridLight;

	// Temporary
	private static final Long2IntMap WORLD_LIGHT_CACHE = new Long2IntOpenHashMap();

	public SuperByteBuffer(TemplateMesh template, int unshadedStartVertex) {
		this.template = template;
		this.unshadedStartVertex = unshadedStartVertex;

		transforms.pushPose();
	}

	public void renderInto(PoseStack input, VertexConsumer builder) {
		if (isEmpty())
			return;

		Matrix4f modelMat = new Matrix4f(input.last()
			.pose());
		Matrix4f localTransforms = transforms.last()
			.pose();
		modelMat.mul(localTransforms);

		Matrix3f normalMat = new Matrix3f(input.last()
			.normal());
		Matrix3f localNormalTransforms = transforms.last()
			.normal();
		normalMat.mul(localNormalTransforms);

		if (useWorldLight) {
			WORLD_LIGHT_CACHE.clear();
		}

		class ShiftOutput implements SpriteShiftFunc.Output {
			public float u;
			public float v;

			@Override
			public void accept(float u, float v) {
				this.u = u;
				this.v = v;
			}
		};

		final Vector4f pos = new Vector4f();
		final Vector3f normal = new Vector3f();
		final ShiftOutput shiftOutput = new ShiftOutput();
		final Vector4f lightPos = new Vector4f();

		DiffuseLightCalculator diffuseCalculator = ForcedDiffuseState.getForcedCalculator();
		final boolean disableDiffuseMult =
			this.disableDiffuseMult || (ShadersModHandler.isShaderPackInUse() && diffuseCalculator == null);
		if (diffuseCalculator == null) {
			diffuseCalculator = this.diffuseCalculator;
			if (diffuseCalculator == null) {
				diffuseCalculator = DiffuseLightCalculator.forLevel(Minecraft.getInstance().level);
			}
		}

		final int vertexCount = template.vertexCount();
		for (int i = 0; i < vertexCount; i++) {
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
			normalX = normal.x();
			normalY = normal.y();
			normalZ = normal.z();

			float r, g, b, a;
			if (shouldColor) {
				r = this.r;
				g = this.g;
				b = this.b;
				a = this.a;
			} else {
				int color = template.color(i);
				r = (color & 0xFF) / 255.0f;
				g = ((color >>> 8) & 0xFF) / 255.0f;
				b = ((color >>> 16) & 0xFF) / 255.0f;
				a = ((color >>> 24) & 0xFF) / 255.0f;
			}
			if (!disableDiffuseMult) {
				// Transformed normal is in camera space, but it is needed in world space to calculate diffuse.
				normal.mul(RenderSystem.getInverseViewRotationMatrix());
				float diffuse = diffuseCalculator.getDiffuse(normal.x(), normal.y(), normal.z(), i < unshadedStartVertex);
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

			int light;
			if (useWorldLight) {
				lightPos.set(((x - .5f) * 15 / 16f) + .5f, (y - .5f) * 15 / 16f + .5f, (z - .5f) * 15 / 16f + .5f, 1f);
				lightPos.mul(localTransforms);
				if (lightTransform != null) {
					lightPos.mul(lightTransform);
				}

				light = getLight(Minecraft.getInstance().level, lightPos);
				if (hasCustomLight) {
					light = maxLight(light, packedLightCoords);
				}
			} else if (hasCustomLight) {
				light = packedLightCoords;
			} else {
				light = template.light(i);
			}
			if (hybridLight) {
				light = maxLight(light, template.light(i));
			}

			builder.vertex(pos.x(), pos.y(), pos.z(), r, g, b, a, u, v, overlay, light, normalX, normalY, normalZ);
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
		disableDiffuseMult = false;
		diffuseCalculator = null;
		spriteShiftFunc = null;
		overlay = OverlayTexture.NO_OVERLAY;
		useWorldLight = false;
		lightTransform = null;
		hasCustomLight = false;
		packedLightCoords = 0;
		hybridLight = false;
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
	public SuperByteBuffer rotate(Quaternionf quaternion) {
		transforms.mulPose(quaternion);
		return this;
	}

	@Override
	public SuperByteBuffer translate(double x, double y, double z) {
		transforms.translate(x, y, z);
		return this;
	}

	@Override
	public SuperByteBuffer mulPose(Matrix4f pose) {
		transforms.last()
			.pose()
			.mul(pose);
		return this;
	}

	@Override
	public SuperByteBuffer mulNormal(Matrix3f normal) {
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
		shouldColor = true;
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

	/**
	 * Prevents vertex colors from being multiplied by the diffuse value calculated
	 * from the final transformed normal vector. Useful for entity rendering, when
	 * diffuse is applied automatically later.
	 */
	public SuperByteBuffer disableDiffuse() {
		disableDiffuseMult = true;
		return this;
	}

	public SuperByteBuffer diffuseCalculator(DiffuseLightCalculator diffuseCalculator) {
		this.diffuseCalculator = diffuseCalculator;
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
	 * Uses max light from calculated light (world light or custom light) and vertex
	 * light for the final light value. Ineffective if any other light method was
	 * not called.
	 */
	public SuperByteBuffer hybridLight() {
		hybridLight = true;
		return this;
	}

	public static int maxLight(int packedLight1, int packedLight2) {
		int blockLight1 = LightTexture.block(packedLight1);
		int skyLight1 = LightTexture.sky(packedLight1);
		int blockLight2 = LightTexture.block(packedLight2);
		int skyLight2 = LightTexture.sky(packedLight2);
		return LightTexture.pack(Math.max(blockLight1, blockLight2), Math.max(skyLight1, skyLight2));
	}

	private static int getLight(Level world, Vector4f lightPos) {
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

	@FunctionalInterface
	public interface VertexLighter {
		int getPackedLight(float x, float y, float z);
	}
}
