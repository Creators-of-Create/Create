package com.simibubi.create.content.contraptions.components.structureMovement.train;

import static net.minecraft.util.math.MathHelper.lerp;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CouplingRenderer {

	public static void renderAll(MatrixStack ms, IRenderTypeBuffer buffer) {
		CouplingHandler.forEachLoadedCoupling(Minecraft.getInstance().world,
			c -> {
				if (c.getFirst().hasContraptionCoupling(true))
					return;
				CouplingRenderer.renderCoupling(ms, buffer, c.map(MinecartController::cart));	
			});
	}

	public static void tickDebugModeRenders() {
		if (KineticDebugger.isActive())
			CouplingHandler.forEachLoadedCoupling(Minecraft.getInstance().world, CouplingRenderer::doDebugRender);
	}

	public static void renderCoupling(MatrixStack ms, IRenderTypeBuffer buffer, Couple<AbstractMinecartEntity> carts) {
		ClientWorld world = Minecraft.getInstance().world;
		
		if (carts.getFirst() == null || carts.getSecond() == null)
			return;
		
		Couple<Integer> lightValues =
			carts.map(c -> WorldRenderer.getLightmapCoordinates(world, new BlockPos(c.getBoundingBox()
				.getCenter())));

		Vec3d center = carts.getFirst()
			.getPositionVec()
			.add(carts.getSecond()
				.getPositionVec())
			.scale(.5f);

		Couple<CartEndpoint> transforms = carts.map(c -> getSuitableCartEndpoint(c, center));

		BlockState renderState = Blocks.AIR.getDefaultState();
		IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
		SuperByteBuffer attachment = AllBlockPartials.COUPLING_ATTACHMENT.renderOn(renderState);
		SuperByteBuffer ring = AllBlockPartials.COUPLING_RING.renderOn(renderState);
		SuperByteBuffer connector = AllBlockPartials.COUPLING_CONNECTOR.renderOn(renderState);

		Vec3d zero = Vec3d.ZERO;
		Vec3d firstEndpoint = transforms.getFirst()
			.apply(zero);
		Vec3d secondEndpoint = transforms.getSecond()
			.apply(zero);
		Vec3d endPointDiff = secondEndpoint.subtract(firstEndpoint);
		double connectorYaw = -Math.atan2(endPointDiff.z, endPointDiff.x) * 180.0D / Math.PI;
		double connectorPitch = Math.atan2(endPointDiff.y, endPointDiff.mul(1, 0, 1)
			.length()) * 180 / Math.PI;

		MatrixStacker msr = MatrixStacker.of(ms);
		carts.forEachWithContext((cart, isFirst) -> {
			CartEndpoint cartTransform = transforms.get(isFirst);

			ms.push();
			cartTransform.apply(ms);
			attachment.light(lightValues.get(isFirst))
				.renderInto(ms, builder);
			msr.rotateY(connectorYaw - cartTransform.yaw);
			ring.light(lightValues.get(isFirst))
				.renderInto(ms, builder);
			ms.pop();
		});

		int l1 = lightValues.getFirst();
		int l2 = lightValues.getSecond();
		int meanBlockLight = (((l1 >> 4) & 0xf) + ((l2 >> 4) & 0xf)) / 2;
		int meanSkyLight = (((l1 >> 20) & 0xf) + ((l2 >> 20) & 0xf)) / 2;

		ms.push();
		msr.translate(firstEndpoint)
			.rotateY(connectorYaw)
			.rotateZ(connectorPitch);
		ms.scale((float) endPointDiff.length(), 1, 1);

		connector.light(meanSkyLight << 20 | meanBlockLight << 4)
			.renderInto(ms, builder);
		ms.pop();
	}

	private static CartEndpoint getSuitableCartEndpoint(AbstractMinecartEntity cart, Vec3d centerOfCoupling) {
		long i = cart.getEntityId() * 493286711L;
		i = i * i * 4392167121L + i * 98761L;
		float x = (((float) (i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float y = (((float) (i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F + 0.375F;
		float z = (((float) (i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;

		float pt = Minecraft.getInstance()
			.getRenderPartialTicks();

		double xIn = lerp(pt, cart.lastTickPosX, cart.getX());
		double yIn = lerp(pt, cart.lastTickPosY, cart.getY());
		double zIn = lerp(pt, cart.lastTickPosZ, cart.getZ());

		float yaw = lerp(pt, cart.prevRotationYaw, cart.rotationYaw);
		float pitch = lerp(pt, cart.prevRotationPitch, cart.rotationPitch);
		float roll = cart.getRollingAmplitude() - pt;

		float rollAmplifier = cart.getDamage() - pt;
		if (rollAmplifier < 0.0F)
			rollAmplifier = 0.0F;
		roll = roll > 0 ? MathHelper.sin(roll) * roll * rollAmplifier / 10.0F * cart.getRollingDirection() : 0;

		Vec3d positionVec = new Vec3d(xIn, yIn, zIn);
		Vec3d frontVec = positionVec.add(VecHelper.rotate(new Vec3d(.5, 0, 0), 180 - yaw, Axis.Y));
		Vec3d backVec = positionVec.add(VecHelper.rotate(new Vec3d(-.5, 0, 0), 180 - yaw, Axis.Y));

		Vec3d railVecOfPos = cart.getPos(xIn, yIn, zIn);
		boolean flip = false;

		if (railVecOfPos != null) {
			frontVec = cart.getPosOffset(xIn, yIn, zIn, (double) 0.3F);
			backVec = cart.getPosOffset(xIn, yIn, zIn, (double) -0.3F);
			if (frontVec == null)
				frontVec = railVecOfPos;
			if (backVec == null)
				backVec = railVecOfPos;

			x += railVecOfPos.x;
			y += (frontVec.y + backVec.y) / 2;
			z += railVecOfPos.z;

			Vec3d endPointDiff = backVec.add(-frontVec.x, -frontVec.y, -frontVec.z);
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
			frontVec.squareDistanceTo(centerOfCoupling) > backVec.squareDistanceTo(centerOfCoupling);
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

		public Vec3d apply(Vec3d vec) {
			vec = vec.add(offset, 0, 0);
			vec = VecHelper.rotate(vec, roll, Axis.X);
			vec = VecHelper.rotate(vec, pitch, Axis.Z);
			vec = VecHelper.rotate(vec, yaw, Axis.Y);
			return vec.add(x, y, z);
		}

		public void apply(MatrixStack ms) {
			ms.translate(x, y, z);
			ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(yaw));
			ms.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(pitch));
			ms.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(roll));
			ms.translate(offset, 0, 0);
			if (flip)
				ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180));
		}

	}

	public static void doDebugRender(Couple<MinecartController> c) {
		int yOffset = 1;
		MinecartController first = c.getFirst();
		AbstractMinecartEntity mainCart = first.cart();
		Vec3d mainCenter = mainCart.getPositionVec()
			.add(0, yOffset, 0);
		Vec3d connectedCenter = c.getSecond()
			.cart()
			.getPositionVec()
			.add(0, yOffset, 0);

		int color = ColorHelper.mixColors(0xabf0e9, 0xee8572, (float) MathHelper
			.clamp(Math.abs(first.getCouplingLength(true) - connectedCenter.distanceTo(mainCenter)) * 8, 0, 1));

		CreateClient.outliner.showLine(mainCart.getEntityId() + "", mainCenter, connectedCenter)
			.colored(color)
			.lineWidth(1 / 8f);

		Vec3d point = mainCart.getPositionVec()
			.add(0, yOffset, 0);
		CreateClient.outliner.showLine(mainCart.getEntityId() + "_dot", point, point.add(0, 1 / 128f, 0))
			.colored(0xffffff)
			.lineWidth(1 / 4f);
	}

}
