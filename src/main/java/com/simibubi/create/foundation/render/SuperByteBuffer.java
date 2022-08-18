package com.simibubi.create.foundation.render;

import java.util.function.IntPredicate;

import com.jozufozu.flywheel.api.vertex.ShadedVertexList;
import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.backend.ShadersModHandler;
import com.jozufozu.flywheel.core.model.ShadeSeparatedBufferBuilder;
import com.jozufozu.flywheel.core.vertex.BlockVertexList;
import com.jozufozu.flywheel.util.DiffuseLightCalculator;
import com.jozufozu.flywheel.util.transform.TStack;
import com.jozufozu.flywheel.util.transform.Transform;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class SuperByteBuffer implements Transform<SuperByteBuffer>, TStack<SuperByteBuffer> {

	private final VertexList template;
	private final IntPredicate shadedPredicate;

	// Vertex Position
	private final PoseStack transforms;

	// Vertex Coloring
	private boolean shouldColor;
	private int r, g, b, a;
	private boolean disableDiffuseMult;
	private DiffuseLightCalculator diffuseCalculator;

	// Vertex Texture Coords
	private SpriteShiftFunc spriteShiftFunc;

	// Vertex Overlay Color
	private boolean hasOverlay;
	private int overlay = OverlayTexture.NO_OVERLAY;

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

	public SuperByteBuffer(BufferBuilder buf) {
		if (buf instanceof ShadeSeparatedBufferBuilder separated) {
			ShadedVertexList template = new BlockVertexList.Shaded(separated);
			shadedPredicate = template::isShaded;
			this.template = template;
		} else {
			template = new BlockVertexList(buf);
			shadedPredicate = index -> true;
		}
		transforms = new PoseStack();
		transforms.pushPose();
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
			normalMat = input.last()
				.normal()
				.copy();
			Matrix3f localNormalTransforms = transforms.last()
				.normal();
			normalMat.mul(localNormalTransforms);
		} else {
			normalMat = transforms.last()
				.normal()
				.copy();
		}

		if (useWorldLight) {
			WORLD_LIGHT_CACHE.clear();
		}

		final Vector4f pos = new Vector4f();
		final Vector3f normal = new Vector3f();
		final Vector4f lightPos = new Vector4f();

		DiffuseLightCalculator diffuseCalculator = ForcedDiffuseState.getForcedCalculator();
		final boolean disableDiffuseMult =
			this.disableDiffuseMult || (ShadersModHandler.isShaderPackInUse() && diffuseCalculator == null);
		if (diffuseCalculator == null) {
			diffuseCalculator = this.diffuseCalculator;
			if (diffuseCalculator == null) {
				diffuseCalculator = DiffuseLightCalculator.forCurrentLevel();
			}
		}

		final int vertexCount = template.getVertexCount();
		for (int i = 0; i < vertexCount; i++) {
			float x = template.getX(i);
			float y = template.getY(i);
			float z = template.getZ(i);

			pos.set(x, y, z, 1F);
			pos.transform(modelMat);
			builder.vertex(pos.x(), pos.y(), pos.z());

			float normalX = template.getNX(i);
			float normalY = template.getNY(i);
			float normalZ = template.getNZ(i);

			normal.set(normalX, normalY, normalZ);
			normal.transform(normalMat);
			float nx = normal.x();
			float ny = normal.y();
			float nz = normal.z();

			byte r, g, b, a;
			if (shouldColor) {
				r = (byte) this.r;
				g = (byte) this.g;
				b = (byte) this.b;
				a = (byte) this.a;
			} else {
				r = template.getR(i);
				g = template.getG(i);
				b = template.getB(i);
				a = template.getA(i);
			}
			if (disableDiffuseMult) {
				builder.color(r, g, b, a);
			} else {
				float instanceDiffuse = diffuseCalculator.getDiffuse(nx, ny, nz, shadedPredicate.test(i));
				int colorR = transformColor(r, instanceDiffuse);
				int colorG = transformColor(g, instanceDiffuse);
				int colorB = transformColor(b, instanceDiffuse);
				builder.color(colorR, colorG, colorB, a);
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
				lightPos.set(((x - .5f) * 15 / 16f) + .5f, (y - .5f) * 15 / 16f + .5f, (z - .5f) * 15 / 16f + .5f, 1f);
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
		disableDiffuseMult = false;
		diffuseCalculator = null;
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

	public boolean isEmpty() {
		return template.isEmpty();
	}

	public PoseStack getTransforms() {
		return transforms;
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

	@Override
	public SuperByteBuffer mulPose(Matrix4f pose) {
		transforms.last()
			.pose()
			.multiply(pose);
		return this;
	}

	@Override
	public SuperByteBuffer mulNormal(Matrix3f normal) {
		transforms.last()
			.normal()
			.mul(normal);
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
		this.spriteShiftFunc = (builder, u, v) -> {
			builder.uv(entry.getTargetU(u), entry.getTargetV(v));
		};
		return this;
	}

	public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollV) {
		return this.shiftUVScrolling(entry, 0, scrollV);
	}

	public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollU, float scrollV) {
		this.spriteShiftFunc = (builder, u, v) -> {
			float targetU = u - entry.getOriginal()
				.getU0() + entry.getTarget()
					.getU0()
				+ scrollU;
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
				.getU((SpriteShiftEntry.getUnInterpolatedU(entry.getOriginal(), u) / sheetSize) + uTarget * 16);
			float targetV = entry.getTarget()
				.getV((SpriteShiftEntry.getUnInterpolatedV(entry.getOriginal(), v) / sheetSize) + vTarget * 16);
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
	 * Uses max light from calculated light (world light or custom light) and vertex
	 * light for the final light value. Ineffective if any other light method was
	 * not called.
	 */
	public SuperByteBuffer hybridLight() {
		hybridLight = true;
		return this;
	}

	/**
	 * Transforms normals not only by the local matrix stack, but also by the passed
	 * matrix stack.
	 */
	public SuperByteBuffer fullNormalTransform() {
		fullNormalTransform = true;
		return this;
	}

	public SuperByteBuffer forEntityRender() {
		disableDiffuse();
		overlay();
		fullNormalTransform();
		return this;
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

	@FunctionalInterface
	public interface SpriteShiftFunc {
		void shift(VertexConsumer builder, float u, float v);
	}

	@FunctionalInterface
	public interface VertexLighter {
		int getPackedLight(float x, float y, float z);
	}

}
