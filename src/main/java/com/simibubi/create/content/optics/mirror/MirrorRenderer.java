package com.simibubi.create.content.optics.mirror;

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

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.client.renderer.tileentity.BeaconTileEntityRenderer.TEXTURE_BEACON_BEAM;

@ParametersAreNonnullByDefault
public class MirrorRenderer extends KineticTileEntityRenderer {
	public MirrorRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	private static void renderBeam(MatrixStack ms, IVertexBuilder builder, float[] colors, double segemntLength, double length, float p_228840_8_, float p_228840_9_, float p_228840_10_, float p_228840_11_, float p_228840_12_, float p_228840_13_, float p_228840_14_, float p_228840_15_, float p_228840_16_, float p_228840_17_, float p_228840_18_, float p_228840_19_) {
		MatrixStack.Entry matrixstack$entry = ms.peek();
		Matrix4f model = matrixstack$entry.getModel();
		Matrix3f normal = matrixstack$entry.getNormal();
		putVertices(model, normal, builder, colors, segemntLength, length, p_228840_8_, p_228840_9_, p_228840_10_, p_228840_11_, p_228840_16_, p_228840_17_, p_228840_18_, p_228840_19_);
		putVertices(model, normal, builder, colors, segemntLength, length, p_228840_14_, p_228840_15_, p_228840_12_, p_228840_13_, p_228840_16_, p_228840_17_, p_228840_18_, p_228840_19_);
		putVertices(model, normal, builder, colors, segemntLength, length, p_228840_10_, p_228840_11_, p_228840_14_, p_228840_15_, p_228840_16_, p_228840_17_, p_228840_18_, p_228840_19_);
		putVertices(model, normal, builder, colors, segemntLength, length, p_228840_12_, p_228840_13_, p_228840_8_, p_228840_9_, p_228840_16_, p_228840_17_, p_228840_18_, p_228840_19_);
	}

	private static void putVertices(Matrix4f model, Matrix3f normal, IVertexBuilder builder, float[] colors, double segemntLength, double length, float p_228839_9_, float p_228839_10_, float p_228839_11_, float p_228839_12_, float p_228839_13_, float p_228839_14_, float p_228839_15_, float p_228839_16_) {
		putVertex(model, normal, builder, colors, (float) length, p_228839_9_, p_228839_10_, p_228839_14_, p_228839_15_);
		putVertex(model, normal, builder, colors, (float) segemntLength, p_228839_9_, p_228839_10_, p_228839_14_, p_228839_16_);
		putVertex(model, normal, builder, colors, (float) segemntLength, p_228839_11_, p_228839_12_, p_228839_13_, p_228839_16_);
		putVertex(model, normal, builder, colors, (float) length, p_228839_11_, p_228839_12_, p_228839_13_, p_228839_15_);
	}

	private static void putVertex(Matrix4f model, Matrix3f normal, IVertexBuilder builder, float[] colors, float vertexY, float vertexX, float vertexZ, float textureX, float textureY) {
		builder.vertex(model, vertexX, vertexY, vertexZ)
				.color(colors[0], colors[1], colors[2], 1f)
				.texture(textureX, textureY)
				.overlay(OverlayTexture.DEFAULT_UV)
				.light(15728880)
				.normal(normal, 0.0F, 1.0F, 0.0F)
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
		double totalLength = 0;
		for (int k = 0; k < mirrorTe.beam.size(); k++) {
			BeamSegment beamSegment = mirrorTe.beam.get(k);
			double beamLength = beamSegment.getDirection()
					.scale(beamSegment.getLength())
					.length();

			renderSegment(beamSegment, ms, buffer, partialTicks, mirrorTe.getWorld()
							.getGameTime(),
					beamLength,
					totalLength
			);
			totalLength += beamLength;
		}
	}

	private void renderSegment(BeamSegment beamSegment, MatrixStack ms, IRenderTypeBuffer buffer, float partialTicks, long gameTime, double beamLength, double prevLength) {
		float adjustedGameTime = (float) Math.floorMod(gameTime, 40L) + partialTicks;
		double totalLength = beamLength + prevLength;
		ms.push();
		ms.translate(0.5, 0.5D, 0.5D);
		ms.multiply(beamSegment.getBeaconBeamModifier());
		ms.translate(-0.5D, -0.5D, -0.5D);
		ms.push();
		ms.translate(0.5D, 0.5D, 0.5D);
		ms.push();
		ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(adjustedGameTime * 2.25F - 45.0F));
		float textureOffset1 = MathHelper.fractionalPart(-adjustedGameTime * 0.2F - (float) MathHelper.floor(-adjustedGameTime * 0.1F)) - 1;
		float textureOffset2 = (float) beamSegment.getLength() * 2.5f + textureOffset1;
		renderBeam(ms, buffer.getBuffer(RenderType.getBeaconBeam(TEXTURE_BEACON_BEAM, true)), beamSegment.colors,
				prevLength, totalLength, 0F, 0.2F, .2F, 0F, -.2f,
				0f, 0f, -.2f, 0f, 1f, textureOffset2, textureOffset1);
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
