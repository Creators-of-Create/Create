package com.simibubi.create.content.optics.mirror;

import static com.simibubi.create.foundation.utility.VecHelper.UP;
import static net.minecraft.client.renderer.tileentity.BeaconTileEntityRenderer.TEXTURE_BEACON_BEAM;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.optics.BeamSegment;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

@ParametersAreNonnullByDefault
public class MirrorRenderer extends KineticTileEntityRenderer {
	public MirrorRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	private static void method_22741(MatrixStack ms, IVertexBuilder builder, float[] colors, float p_228840_5_, int p_228840_6_, int p_228840_7_, float p_228840_8_, float p_228840_9_, float p_228840_10_, float p_228840_11_, float p_228840_12_, float p_228840_13_, float p_228840_14_, float p_228840_15_, float p_228840_16_, float p_228840_17_, float p_228840_18_, float p_228840_19_) {
		MatrixStack.Entry matrixstack$entry = ms.peek();
		Matrix4f matrix4f = matrixstack$entry.getModel();
		Matrix3f matrix3f = matrixstack$entry.getNormal();
		method_22740(matrix4f, matrix3f, builder, colors[0], colors[1], colors[2], p_228840_5_, p_228840_6_, p_228840_7_, p_228840_8_, p_228840_9_, p_228840_10_, p_228840_11_, p_228840_16_, p_228840_17_, p_228840_18_, p_228840_19_);
		method_22740(matrix4f, matrix3f, builder, colors[0], colors[1], colors[2], p_228840_5_, p_228840_6_, p_228840_7_, p_228840_14_, p_228840_15_, p_228840_12_, p_228840_13_, p_228840_16_, p_228840_17_, p_228840_18_, p_228840_19_);
		method_22740(matrix4f, matrix3f, builder, colors[0], colors[1], colors[2], p_228840_5_, p_228840_6_, p_228840_7_, p_228840_10_, p_228840_11_, p_228840_14_, p_228840_15_, p_228840_16_, p_228840_17_, p_228840_18_, p_228840_19_);
		method_22740(matrix4f, matrix3f, builder, colors[0], colors[1], colors[2], p_228840_5_, p_228840_6_, p_228840_7_, p_228840_12_, p_228840_13_, p_228840_8_, p_228840_9_, p_228840_16_, p_228840_17_, p_228840_18_, p_228840_19_);
	}

	private static void method_22740(Matrix4f matrix, Matrix3f p_228839_1_, IVertexBuilder builder, float p_228839_3_, float p_228839_4_, float p_228839_5_, float p_228839_6_, int p_228839_7_, int p_228839_8_, float p_228839_9_, float p_228839_10_, float p_228839_11_, float p_228839_12_, float p_228839_13_, float p_228839_14_, float p_228839_15_, float p_228839_16_) {
		method_23076(matrix, p_228839_1_, builder, p_228839_3_, p_228839_4_, p_228839_5_, p_228839_6_, p_228839_8_, p_228839_9_, p_228839_10_, p_228839_14_, p_228839_15_);
		method_23076(matrix, p_228839_1_, builder, p_228839_3_, p_228839_4_, p_228839_5_, p_228839_6_, p_228839_7_, p_228839_9_, p_228839_10_, p_228839_14_, p_228839_16_);
		method_23076(matrix, p_228839_1_, builder, p_228839_3_, p_228839_4_, p_228839_5_, p_228839_6_, p_228839_7_, p_228839_11_, p_228839_12_, p_228839_13_, p_228839_16_);
		method_23076(matrix, p_228839_1_, builder, p_228839_3_, p_228839_4_, p_228839_5_, p_228839_6_, p_228839_8_, p_228839_11_, p_228839_12_, p_228839_13_, p_228839_15_);
	}

	private static void method_23076(Matrix4f p_228838_0_, Matrix3f p_228838_1_, IVertexBuilder builder, float p_228838_3_, float p_228838_4_, float p_228838_5_, float p_228838_6_, int p_228838_7_, float p_228838_8_, float p_228838_9_, float p_228838_10_, float p_228838_11_) {
		builder.vertex(p_228838_0_, p_228838_8_, (float) p_228838_7_, p_228838_9_)
				.color(p_228838_3_, p_228838_4_, p_228838_5_, p_228838_6_)
				.texture(p_228838_10_, p_228838_11_)
				.overlay(OverlayTexture.DEFAULT_UV)
				.light(15728880)
				.normal(p_228838_1_, 0.0F, 1.0F, 0.0F)
				.endVertex();
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
							  int light, int overlay) {

		// if (FastRenderDispatcher.available(te.getWorld())) return;

		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		MirrorTileEntity mirrorTe = (MirrorTileEntity) te;

		renderMirror(mirrorTe, partialTicks, ms, buffer, light);
		renderOutBeam(mirrorTe, partialTicks, ms, buffer);
	}

	private void renderOutBeam(MirrorTileEntity mirrorTe, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer) {

		int start = 0;

		for (int k = 0; k < mirrorTe.beam.size(); k++) {
			BeamSegment beamSegment = mirrorTe.beam.get(k);
			renderSegment(mirrorTe, beamSegment, ms, buffer, partialTicks, mirrorTe.getWorld()
							.getGameTime(),
					beamSegment.getDirection()
							.scale(start + beamSegment.getLength())
							.length());
			start += beamSegment.getLength();
		}
	}

	private void renderSegment(MirrorTileEntity mirrorTe, BeamSegment beamSegment, MatrixStack ms, IRenderTypeBuffer buffer, float partialTicks, long gameTime, double length) {
		float adjustedGameTime = (float) Math.floorMod(gameTime, 40L) + partialTicks;
		ms.push();
		ms.translate(0, 0.5D, 0.5D);
		ms.multiply(mirrorTe.getBeamRotationAround()
				.getRadialQuaternion((float) Math.acos(beamSegment.getNormalized()
						.dotProduct(UP))));
		ms.translate(0, -0.5D, -0.5D);
		ms.push();
		ms.translate(0.5D, 0.0D, 0.5D);
		ms.push();
		ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(adjustedGameTime * 2.25F - 45.0F));
		float f2 = MathHelper.fractionalPart(-adjustedGameTime * 0.2F - (float) MathHelper.floor(-adjustedGameTime * 0.1F));
		float f15 = f2 - 1;
		float f16 = (float) beamSegment.getLength() * 1F * (0.5F / 0.2F) + f15;
		method_22741(ms, buffer.getBuffer(RenderType.getBeaconBeam(TEXTURE_BEACON_BEAM, true)), beamSegment.colors, 1F, MathHelper.floor(length - beamSegment.getLength()), MathHelper.floor(length), 0F, 0.2F, .2F, 0F, -.2f, 0f, 0f, -.2f, 0f, 1f, f16, f15);
		ms.pop();
		ms.pop();
		ms.pop();
	}

	private void renderMirror(MirrorTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light) {

		final Direction.Axis facing = te.getBlockState()
				.get(BlockStateProperties.AXIS);
		SuperByteBuffer superBuffer = AllBlockPartials.MIRROR_PLANE.renderOn(te.getBlockState());

		float interpolatedAngle = te.getInterpolatedAngle(partialTicks - 1);
		kineticRotationTransform(superBuffer, te, facing, (float) (interpolatedAngle / 180 * Math.PI), light);

		switch (facing) {
			case X:
				superBuffer.rotateCentered(Direction.UP, AngleHelper.rad(90));
				break;
			case Y:
				superBuffer.rotateCentered(Direction.EAST, AngleHelper.rad(90));
				break;
			default:
				superBuffer.rotateCentered(Direction.UP, AngleHelper.rad(180));
		}

		superBuffer.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

	@Override
	public boolean isGlobalRenderer(KineticTileEntity tileEntity) {
		return true;
	}
}
