package com.simibubi.create.content.contraptions.components.structureMovement.train;

import static net.minecraft.util.math.MathHelper.lerp;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class CouplingRenderer {

	public static void renderAll(MatrixStack ms, IRenderTypeBuffer buffer) {
		CouplingHandler.forEachLoadedCoupling(Minecraft.getInstance().level,
			c -> {
				if (c.getFirst().hasContraptionCoupling(true))
					return;
				CouplingRenderer.renderCoupling(ms, buffer, c.map(MinecartController::cart));
			});
	}

	public static void tickDebugModeRenders() {
		if (KineticDebugger.isActive())
			CouplingHandler.forEachLoadedCoupling(Minecraft.getInstance().level, CouplingRenderer::doDebugRender);
	}

	public static void renderCoupling(MatrixStack ms, IRenderTypeBuffer buffer, Couple<AbstractMinecartEntity> carts) {
		ClientWorld world = Minecraft.getInstance().level;

		if (carts.getFirst() == null || carts.getSecond() == null)
			return;

		Couple<Integer> lightValues =
			carts.map(c -> WorldRenderer.getLightColor(world, new BlockPos(c.getBoundingBox()
				.getCenter())));

		Vector3d center = carts.getFirst()
				.position()
				.add(carts.getSecond()
						.position())
				.scale(.5f);

		Couple<CartEndpoint> transforms = carts.map(c -> getSuitableCartEndpoint(c, center));

		BlockState renderState = Blocks.AIR.defaultBlockState();
		IVertexBuilder builder = buffer.getBuffer(RenderType.solid());
		SuperByteBuffer attachment = PartialBufferer.get(AllBlockPartials.COUPLING_ATTACHMENT, renderState);
		SuperByteBuffer ring = PartialBufferer.get(AllBlockPartials.COUPLING_RING, renderState);
		SuperByteBuffer connector = PartialBufferer.get(AllBlockPartials.COUPLING_CONNECTOR, renderState);

		Vector3d zero = Vector3d.ZERO;
		Vector3d firstEndpoint = transforms.getFirst()
				.apply(zero);
		Vector3d secondEndpoint = transforms.getSecond()
				.apply(zero);
		Vector3d endPointDiff = secondEndpoint.subtract(firstEndpoint);
		double connectorYaw = -Math.atan2(endPointDiff.z, endPointDiff.x) * 180.0D / Math.PI;
		double connectorPitch = Math.atan2(endPointDiff.y, endPointDiff.multiply(1, 0, 1)
				.length()) * 180 / Math.PI;

		MatrixStacker msr = MatrixStacker.of(ms);
		carts.forEachWithContext((cart, isFirst) -> {
			CartEndpoint cartTransform = transforms.get(isFirst);

			ms.pushPose();
			cartTransform.apply(ms);
			attachment.light(lightValues.get(isFirst))
				.renderInto(ms, builder);
			msr.rotateY(connectorYaw - cartTransform.yaw);
			ring.light(lightValues.get(isFirst))
				.renderInto(ms, builder);
			ms.popPose();
		});

		int l1 = lightValues.getFirst();
		int l2 = lightValues.getSecond();
		int meanBlockLight = (((l1 >> 4) & 0xf) + ((l2 >> 4) & 0xf)) / 2;
		int meanSkyLight = (((l1 >> 20) & 0xf) + ((l2 >> 20) & 0xf)) / 2;

		ms.pushPose();
		msr.translate(firstEndpoint)
			.rotateY(connectorYaw)
			.rotateZ(connectorPitch);
		ms.scale((float) endPointDiff.length(), 1, 1);

		connector.light(meanSkyLight << 20 | meanBlockLight << 4)
			.renderInto(ms, builder);
		ms.popPose();
	}

	private static CartEndpoint getSuitableCartEndpoint(AbstractMinecartEntity cart, Vector3d centerOfCoupling) {
		long i = cart.getId() * 493286711L;
		i = i * i * 4392167121L + i * 98761L;
		float x = (((float) (i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float y = (((float) (i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F + 0.375F;
		float z = (((float) (i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;

		float pt = AnimationTickHolder.getPartialTicks();

		double xIn = lerp(pt, cart.xOld, cart.getX());
		double yIn = lerp(pt, cart.yOld, cart.getY());
		double zIn = lerp(pt, cart.zOld, cart.getZ());

		float yaw = lerp(pt, cart.yRotO, cart.yRot);
		float pitch = lerp(pt, cart.xRotO, cart.xRot);
		float roll = cart.getHurtTime() - pt;

		float rollAmplifier = cart.getDamage() - pt;
		if (rollAmplifier < 0.0F)
			rollAmplifier = 0.0F;
		roll = roll > 0 ? MathHelper.sin(roll) * roll * rollAmplifier / 10.0F * cart.getHurtDir() : 0;

		Vector3d positionVec = new Vector3d(xIn, yIn, zIn);
		Vector3d frontVec = positionVec.add(VecHelper.rotate(new Vector3d(.5, 0, 0), 180 - yaw, Axis.Y));
		Vector3d backVec = positionVec.add(VecHelper.rotate(new Vector3d(-.5, 0, 0), 180 - yaw, Axis.Y));

		Vector3d railVecOfPos = cart.getPos(xIn, yIn, zIn);
		boolean flip = false;

		if (railVecOfPos != null) {
			frontVec = cart.getPosOffs(xIn, yIn, zIn, (double) 0.3F);
			backVec = cart.getPosOffs(xIn, yIn, zIn, (double) -0.3F);
			if (frontVec == null)
				frontVec = railVecOfPos;
			if (backVec == null)
				backVec = railVecOfPos;

			x += railVecOfPos.x;
			y += (frontVec.y + backVec.y) / 2;
			z += railVecOfPos.z;

			Vector3d endPointDiff = backVec.add(-frontVec.x, -frontVec.y, -frontVec.z);
			if (endPointDiff.length() != 0.0D) {
				endPointDiff = endPointDiff.normalize();
				yaw = (float) (Math.atan2(endPointDiff.z, endPointDiff.x) * 180.0D / Math.PI);
				pitch = (float) (Math.atan(endPointDiff.y) * 73.0D);
			}
		} else {
			x += xIn;
			y += yIn;
			z += zIn;
		}

		final float offsetMagnitude = 13 / 16f;
		boolean isBackFaceCloser =
			frontVec.distanceToSqr(centerOfCoupling) > backVec.distanceToSqr(centerOfCoupling);
		flip = isBackFaceCloser;
		float offset = isBackFaceCloser ? -offsetMagnitude : offsetMagnitude;

		return new CartEndpoint(x, y + 2 / 16f, z, 180 - yaw, -pitch, roll, offset, flip);
	}

	static class CartEndpoint {

		float x;
		float y;
		float z;
		float yaw;
		float pitch;
		float roll;
		float offset;
		boolean flip;

		public CartEndpoint(float x, float y, float z, float yaw, float pitch, float roll, float offset, boolean flip) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.yaw = yaw;
			this.pitch = pitch;
			this.roll = roll;
			this.offset = offset;
			this.flip = flip;
		}

		public Vector3d apply(Vector3d vec) {
			vec = vec.add(offset, 0, 0);
			vec = VecHelper.rotate(vec, roll, Axis.X);
			vec = VecHelper.rotate(vec, pitch, Axis.Z);
			vec = VecHelper.rotate(vec, yaw, Axis.Y);
			return vec.add(x, y, z);
		}

		public void apply(MatrixStack ms) {
			ms.translate(x, y, z);
			ms.mulPose(Vector3f.YP.rotationDegrees(yaw));
			ms.mulPose(Vector3f.ZP.rotationDegrees(pitch));
			ms.mulPose(Vector3f.XP.rotationDegrees(roll));
			ms.translate(offset, 0, 0);
			if (flip)
				ms.mulPose(Vector3f.YP.rotationDegrees(180));
		}

	}

	public static void doDebugRender(Couple<MinecartController> c) {
		int yOffset = 1;
		MinecartController first = c.getFirst();
		AbstractMinecartEntity mainCart = first.cart();
		Vector3d mainCenter = mainCart.position()
				.add(0, yOffset, 0);
		Vector3d connectedCenter = c.getSecond()
				.cart()
				.position()
				.add(0, yOffset, 0);

		int color = ColorHelper.mixColors(0xabf0e9, 0xee8572, (float) MathHelper
				.clamp(Math.abs(first.getCouplingLength(true) - connectedCenter.distanceTo(mainCenter)) * 8, 0, 1));

		CreateClient.OUTLINER.showLine(mainCart.getId() + "", mainCenter, connectedCenter)
				.colored(color)
				.lineWidth(1 / 8f);

		Vector3d point = mainCart.position()
				.add(0, yOffset, 0);
		CreateClient.OUTLINER.showLine(mainCart.getId() + "_dot", point, point.add(0, 1 / 128f, 0))
				.colored(0xffffff)
				.lineWidth(1 / 4f);
	}

}
