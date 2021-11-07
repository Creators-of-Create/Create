package com.simibubi.create.foundation.fluid;

import java.util.function.Function;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.render.RenderTypes;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public class FluidRenderer {

	public static VertexConsumer getFluidBuilder(MultiBufferSource buffer) {
		return buffer.getBuffer(RenderTypes.getFluid());
	}

	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
		boolean inbound, MultiBufferSource buffer, PoseStack ms, int light) {
		renderFluidStream(fluidStack, direction, radius, progress, inbound, getFluidBuilder(buffer), ms, light);
	}

	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
		boolean inbound, VertexConsumer builder, PoseStack ms, int light) {
		Fluid fluid = fluidStack.getFluid();
		FluidAttributes fluidAttributes = fluid.getAttributes();
		Function<ResourceLocation, TextureAtlasSprite> spriteAtlas = Minecraft.getInstance()
			.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
		TextureAtlasSprite flowTexture = spriteAtlas.apply(fluidAttributes.getFlowingTexture(fluidStack));
		TextureAtlasSprite stillTexture = spriteAtlas.apply(fluidAttributes.getStillTexture(fluidStack));

		int color = fluidAttributes.getColor(fluidStack);
		int blockLightIn = (light >> 4) & 0xF;
		int luminosity = Math.max(blockLightIn, fluidAttributes.getLuminosity(fluidStack));
		light = (light & 0xF00000) | luminosity << 4;

		if (inbound)
			direction = direction.getOpposite();

		MatrixTransformStack msr = MatrixTransformStack.of(ms);
		ms.pushPose();
		msr.centre()
			.rotateY(AngleHelper.horizontalAngle(direction))
			.rotateX(direction == Direction.UP ? 0 : direction == Direction.DOWN ? 180 : 90)
			.unCentre();
		ms.translate(.5, 0, .5);

		float h = (float) (radius);
		float hMin = (float) (-radius);
		float hMax = (float) (radius);
		float y = inbound ? 0 : .5f;
		float yMin = y;
		float yMax = y + Mth.clamp(progress * .5f - 1e-6f, 0, 1);

		for (int i = 0; i < 4; i++) {
			ms.pushPose();
			renderTiledHorizontalFace(h, Direction.SOUTH, hMin, yMin, hMax, yMax, builder, ms, light, color,
				flowTexture);
			ms.popPose();
			msr.rotateY(90);
		}

		if (progress != 1)
			renderTiledVerticalFace(yMax, Direction.UP, hMin, hMin, hMax, hMax, builder, ms, light, color,
				stillTexture);

		ms.popPose();
	}

	public static void renderTiledFluidBB(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax,
		float yMax, float zMax, MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom) {
		renderTiledFluidBB(fluidStack, xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light, renderBottom);
	}

	public static void renderTiledFluidBB(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax,
		float yMax, float zMax, VertexConsumer builder, PoseStack ms, int light, boolean renderBottom) {
		Fluid fluid = fluidStack.getFluid();
		FluidAttributes fluidAttributes = fluid.getAttributes();
		TextureAtlasSprite fluidTexture = Minecraft.getInstance()
			.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
			.apply(fluidAttributes.getStillTexture(fluidStack));

		int color = fluidAttributes.getColor(fluidStack);
		int blockLightIn = (light >> 4) & 0xF;
		int luminosity = Math.max(blockLightIn, fluidAttributes.getLuminosity(fluidStack));
		light = (light & 0xF00000) | luminosity << 4;

		Vec3 center = new Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
		MatrixTransformStack msr = MatrixTransformStack.of(ms);
		ms.pushPose();
		if (fluidStack.getFluid()
			.getAttributes()
			.isLighterThanAir())
			MatrixTransformStack.of(ms)
				.translate(center)
				.rotateX(180)
				.translateBack(center);

		for (Direction side : Iterate.directions) {
			if (side == Direction.DOWN && !renderBottom)
				continue;

			if (side.getAxis()
				.isHorizontal()) {
				ms.pushPose();

				if (side.getAxisDirection() == AxisDirection.NEGATIVE) {
					msr.translate(center)
						.rotateY(180)
						.translateBack(center);
					side = Direction.get(AxisDirection.POSITIVE, side.getAxis());
				}

				boolean X = side.getAxis() == Axis.X;
				renderTiledHorizontalFace(X ? xMax : zMax, side, X ? zMin : xMin, yMin, X ? zMax : xMax, yMax, builder,
					ms, light, color, fluidTexture);

				ms.popPose();
				continue;
			}

			renderTiledVerticalFace(side == Direction.UP ? yMax : yMin, side, xMin, zMin, xMax, zMax, builder, ms,
				light, color, fluidTexture);
		}

		ms.popPose();

	}

	private static void renderTiledVerticalFace(float y, Direction face, float xMin, float zMin, float xMax, float zMax,
		VertexConsumer builder, PoseStack ms, int light, int color, TextureAtlasSprite texture) {
		float x2 = 0;
		float z2 = 0;
		for (float x1 = xMin; x1 < xMax; x1 = x2) {
			x2 = Math.min((int) (x1 + 1), xMax);
			for (float z1 = zMin; z1 < zMax; z1 = z2) {
				z2 = Math.min((int) (z1 + 1), zMax);

				float u1 = texture.getU(local(x1) * 16);
				float v1 = texture.getV(local(z1) * 16);
				float u2 = texture.getU(x2 == xMax ? local(x2) * 16 : 16);
				float v2 = texture.getV(z2 == zMax ? local(z2) * 16 : 16);

				putVertex(builder, ms, x1, y, z2, color, u1, v2, face, light);
				putVertex(builder, ms, x2, y, z2, color, u2, v2, face, light);
				putVertex(builder, ms, x2, y, z1, color, u2, v1, face, light);
				putVertex(builder, ms, x1, y, z1, color, u1, v1, face, light);
			}
		}
	}

	private static void renderTiledHorizontalFace(float h, Direction face, float hMin, float yMin, float hMax,
		float yMax, VertexConsumer builder, PoseStack ms, int light, int color, TextureAtlasSprite texture) {
		boolean X = face.getAxis() == Axis.X;

		float h2 = 0;
		float y2 = 0;

		for (float h1 = hMin; h1 < hMax; h1 = h2) {
			h2 = Math.min((int) (h1 + 1), hMax);
			for (float y1 = yMin; y1 < yMax; y1 = y2) {
				y2 = Math.min((int) (y1 + 1), yMax);

				int multiplier = texture.getWidth() == 32 ? 8 : 16;
				float u1 = texture.getU(local(h1) * multiplier);
				float v1 = texture.getV(local(y1) * multiplier);
				float u2 = texture.getU(h2 == hMax ? local(h2) * multiplier : multiplier);
				float v2 = texture.getV(y2 == yMax ? local(y2) * multiplier : multiplier);

				float x1 = X ? h : h1;
				float x2 = X ? h : h2;
				float z1 = X ? h1 : h;
				float z2 = X ? h2 : h;

				putVertex(builder, ms, x2, y2, z1, color, u1, v2, face, light);
				putVertex(builder, ms, x1, y2, z2, color, u2, v2, face, light);
				putVertex(builder, ms, x1, y1, z2, color, u2, v1, face, light);
				putVertex(builder, ms, x2, y1, z1, color, u1, v1, face, light);
			}
		}
	}

	private static float local(float f) {
		if (f < 0)
			f += 10;
		return f - ((int) f);
	}

	private static void putVertex(VertexConsumer builder, PoseStack ms, float x, float y, float z, int color, float u,
		float v, Direction face, int light) {

		Vec3i n = face.getNormal();
		Pose peek = ms.last();
		int ff = 0xff;
		int a = color >> 24 & ff;
		int r = color >> 16 & ff;
		int g = color >> 8 & ff;
		int b = color & ff;

		builder.vertex(peek.pose(), x, y, z)
			.color(r, g, b, a)
			.uv(u, v)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(light)
			.normal(peek.normal(), n.getX(), n.getY(), n.getZ())
			.endVertex();
	}

}
