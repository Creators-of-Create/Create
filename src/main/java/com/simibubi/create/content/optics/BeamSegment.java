package com.simibubi.create.content.optics;

import static com.simibubi.create.foundation.utility.VecHelper.UP;
import static net.minecraft.client.renderer.tileentity.BeaconTileEntityRenderer.TEXTURE_BEACON_BEAM;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LazyValue;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BeamSegment {
	public final float[] colors;
	private final Vector3d direction;
	private final Vector3d start;
	private final LazyValue<Vector3d> normalized;
	private final LazyValue<Float> totalSectionLength;
	private final ILightHandler handler;
	@Nullable
	private Quaternion beaconBeamModifier;
	private double length;

	public BeamSegment(ILightHandler handler, @Nonnull float[] color, Vector3d start, Vector3d direction) {
		this.handler = handler;
		this.colors = color;
		this.direction = direction;
		this.start = start;
		this.length = 1;
		this.normalized = new LazyValue<>(direction::normalize);
		beaconBeamModifier = null;
		totalSectionLength = new LazyValue<>(() -> (float) getDirection().scale(getLength())
				.length());
	}

	@OnlyIn(Dist.CLIENT)
	private static void renderBeam(MatrixStack ms, IVertexBuilder builder, float[] colors, float alpha, float segmentLength, float p_228840_9_, float p_228840_10_, float p_228840_11_, float p_228840_12_, float p_228840_13_, float p_228840_14_, float p_228840_15_, float p_228840_18_, float p_228840_19_) {
		MatrixStack.Entry entry = ms.peek();
		Matrix4f model = entry.getModel();
		Matrix3f normal = entry.getNormal();
		putVertices(model, normal, builder, colors, alpha, segmentLength, 0F, p_228840_9_, p_228840_10_, p_228840_11_, p_228840_18_, p_228840_19_);
		putVertices(model, normal, builder, colors, alpha, segmentLength, p_228840_14_, p_228840_15_, p_228840_12_, p_228840_13_, p_228840_18_, p_228840_19_);
		putVertices(model, normal, builder, colors, alpha, segmentLength, p_228840_10_, p_228840_11_, p_228840_14_, p_228840_15_, p_228840_18_, p_228840_19_);
		putVertices(model, normal, builder, colors, alpha, segmentLength, p_228840_12_, p_228840_13_, 0F, p_228840_9_, p_228840_18_, p_228840_19_);
	}

	@OnlyIn(Dist.CLIENT)
	private static void putVertices(Matrix4f model, Matrix3f normal, IVertexBuilder builder, float[] colors, float alpha, float segmentLength, float p_228839_9_, float p_228839_10_, float p_228839_11_, float p_228839_12_, float p_228839_15_, float p_228839_16_) {
		putVertex(model, normal, builder, colors, alpha, segmentLength, p_228839_9_, p_228839_10_, 1F, p_228839_15_);
		putVertex(model, normal, builder, colors, alpha, 0F, p_228839_9_, p_228839_10_, 1F, p_228839_16_);
		putVertex(model, normal, builder, colors, alpha, 0F, p_228839_11_, p_228839_12_, 0F, p_228839_16_);
		putVertex(model, normal, builder, colors, alpha, segmentLength, p_228839_11_, p_228839_12_, 0F, p_228839_15_);
	}

	@OnlyIn(Dist.CLIENT)
	private static void putVertex(Matrix4f model, Matrix3f normal, IVertexBuilder builder, float[] colors, float alpha, float vertexY, float vertexX, float vertexZ, float textureX, float textureY) {
		builder.vertex(model, vertexX, vertexY, vertexZ)
				.color(colors[0], colors[1], colors[2], alpha)
				.texture(textureX, textureY)
				.overlay(OverlayTexture.DEFAULT_UV)
				.light(15728880)
				.normal(normal, 0.0F, 1.0F, 0.0F)
				.endVertex();
	}

	public void incrementLength() {
		++this.length;
	}

	public float[] getColors() {
		return this.colors;
	}

	public double getLength() {
		return this.length;
	}

	public Vector3d getDirection() {
		return direction;
	}

	public Vector3d getStart() {
		return start;
	}

	public Vector3d getNormalized() {
		return normalized.getValue();
	}

	public ILightHandler getHandler() {
		return handler;
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public Quaternion getBeaconBeamModifier() {
		if (beaconBeamModifier == null) {
			Vector3d axis = UP.crossProduct(getDirection())
					.normalize();
			if (axis.equals(Vector3d.ZERO))
				axis = new Vector3d(1, 0, 0);
			beaconBeamModifier = new Vector3f(axis).getRadialQuaternion((float) Math.acos(getNormalized()
					.dotProduct(UP)));
		}
		return beaconBeamModifier;
	}

	public long getWorldTick() {
		World world = getHandler()
				.getHandlerWorld();
		if (world == null)
			return 0;
		return world.getGameTime();
	}

	@OnlyIn(Dist.CLIENT)
	public void renderSegment(MatrixStack ms, IRenderTypeBuffer buffer, float partialTicks) {
		float adjustedGameTime = (float) Math.floorMod(getWorldTick(), 40L) + partialTicks;
		float textureOffset1 = MathHelper.fractionalPart(-adjustedGameTime * 0.2F - (float) MathHelper.floor(-adjustedGameTime * 0.1F)) - 1;
		float textureOffset2 = (float) this.getLength() * 2.5f + textureOffset1;

		MatrixStacker stacker = MatrixStacker.of(ms)
				.push()
				.translate(getStart().subtract(VecHelper.getCenterOf(getHandler().getBlockPos())))
				.push()
				.translate(VecHelper.CENTER_OF_ORIGIN)
				.multiply(getBeaconBeamModifier())
				.push()
				.multiply(Vector3f.POSITIVE_Y, adjustedGameTime * 2.25F - 45.0F);

		renderBeam(stacker.unwrap(), buffer.getBuffer(RenderType.getBeaconBeam(TEXTURE_BEACON_BEAM, false)), this.colors, 1F,
				totalSectionLength.getValue(), 0.2F, .2F, 0F, -.2f,
				0f, 0f, -.2f, textureOffset2, textureOffset1);
		stacker.pop();
		renderBeam(stacker.unwrap(), buffer.getBuffer(RenderType.getBeaconBeam(TEXTURE_BEACON_BEAM, true)), this.colors, 0.125F,
				totalSectionLength.getValue(), -.25f, .25f, -.25f, -.25f,
				.25f, .25f, .25f, textureOffset2, textureOffset1);
		stacker.pop()
				.pop();
	}

	public void setLength(double length) {
		this.length = length;
	}
}
