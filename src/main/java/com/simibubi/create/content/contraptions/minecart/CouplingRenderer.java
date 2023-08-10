package com.simibubi.create.content.contraptions.minecart;

import static net.minecraft.util.Mth.lerp;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.minecart.capability.MinecartController;
import com.simibubi.create.content.kinetics.KineticDebugger;
import com.simibubi.create.foundation.render.CachedPartialBuffers;

import net.createmod.catnip.CatnipClient;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CouplingRenderer {

	public static void renderAll(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {
		CouplingHandler.forEachLoadedCoupling(Minecraft.getInstance().level, c -> {
			if (c.getFirst()
				.hasContraptionCoupling(true))
				return;
			CouplingRenderer.renderCoupling(ms, buffer, camera, c.map(MinecartController::cart));
		});
	}

	public static void tickDebugModeRenders() {
		if (KineticDebugger.isActive())
			CouplingHandler.forEachLoadedCoupling(Minecraft.getInstance().level, CouplingRenderer::doDebugRender);
	}

	public static void renderCoupling(PoseStack ms, MultiBufferSource buffer, Vec3 camera, Couple<AbstractMinecart> carts) {
		ClientLevel world = Minecraft.getInstance().level;

		if (carts.getFirst() == null || carts.getSecond() == null)
			return;

		Couple<Integer> lightValues = carts.map(c -> LevelRenderer.getLightColor(world, new BlockPos(c.getBoundingBox()
			.getCenter())));

		Vec3 center = carts.getFirst()
			.position()
			.add(carts.getSecond()
				.position())
			.scale(.5f);

		Couple<CartEndpoint> transforms = carts.map(c -> getSuitableCartEndpoint(c, center));

		BlockState renderState = Blocks.AIR.defaultBlockState();
		VertexConsumer builder = buffer.getBuffer(RenderType.solid());
		SuperByteBuffer attachment = CachedPartialBuffers.partial(AllPartialModels.COUPLING_ATTACHMENT, renderState);
		SuperByteBuffer ring = CachedPartialBuffers.partial(AllPartialModels.COUPLING_RING, renderState);
		SuperByteBuffer connector = CachedPartialBuffers.partial(AllPartialModels.COUPLING_CONNECTOR, renderState);

		Vec3 zero = Vec3.ZERO;
		Vec3 firstEndpoint = transforms.getFirst()
			.apply(zero);
		Vec3 secondEndpoint = transforms.getSecond()
			.apply(zero);
		Vec3 endPointDiff = secondEndpoint.subtract(firstEndpoint);
		double connectorYaw = -Math.atan2(endPointDiff.z, endPointDiff.x) * 180.0D / Math.PI;
		double connectorPitch = Math.atan2(endPointDiff.y, endPointDiff.multiply(1, 0, 1)
			.length()) * 180 / Math.PI;

		TransformStack msr = TransformStack.cast(ms);
		carts.forEachWithContext((cart, isFirst) -> {
			CartEndpoint cartTransform = transforms.get(isFirst);

			ms.pushPose();
			cartTransform.apply(ms, camera);
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
		msr.translate(firstEndpoint.subtract(camera))
			.rotateY(connectorYaw)
			.rotateZ(connectorPitch);
		ms.scale((float) endPointDiff.length(), 1, 1);

		connector.light(meanSkyLight << 20 | meanBlockLight << 4)
			.renderInto(ms, builder);
		ms.popPose();
	}

	private static CartEndpoint getSuitableCartEndpoint(AbstractMinecart cart, Vec3 centerOfCoupling) {
		long i = cart.getId() * 493286711L;
		i = i * i * 4392167121L + i * 98761L;
		double x = (((float) (i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		double y = (((float) (i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F + 0.375F;
		double z = (((float) (i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;

		float pt = AnimationTickHolder.getPartialTicks();

		double xIn = lerp(pt, cart.xOld, cart.getX());
		double yIn = lerp(pt, cart.yOld, cart.getY());
		double zIn = lerp(pt, cart.zOld, cart.getZ());

		float yaw = lerp(pt, cart.yRotO, cart.getYRot());
		float pitch = lerp(pt, cart.xRotO, cart.getXRot());
		float roll = cart.getHurtTime() - pt;

		float rollAmplifier = cart.getDamage() - pt;
		if (rollAmplifier < 0.0F)
			rollAmplifier = 0.0F;
		roll = roll > 0 ? Mth.sin(roll) * roll * rollAmplifier / 10.0F * cart.getHurtDir() : 0;

		Vec3 positionVec = new Vec3(xIn, yIn, zIn);
		Vec3 frontVec = positionVec.add(VecHelper.rotate(new Vec3(.5, 0, 0), 180 - yaw, Axis.Y));
		Vec3 backVec = positionVec.add(VecHelper.rotate(new Vec3(-.5, 0, 0), 180 - yaw, Axis.Y));

		Vec3 railVecOfPos = cart.getPos(xIn, yIn, zIn);
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

			Vec3 endPointDiff = backVec.add(-frontVec.x, -frontVec.y, -frontVec.z);
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
		boolean isBackFaceCloser = frontVec.distanceToSqr(centerOfCoupling) > backVec.distanceToSqr(centerOfCoupling);
		flip = isBackFaceCloser;
		float offset = isBackFaceCloser ? -offsetMagnitude : offsetMagnitude;

		return new CartEndpoint(x, y + 2 / 16f, z, 180 - yaw, -pitch, roll, offset, flip);
	}

	static class CartEndpoint {

		double x;
		double y;
		double z;
		float yaw;
		float pitch;
		float roll;
		float offset;
		boolean flip;

		public CartEndpoint(double x, double y, double z, float yaw, float pitch, float roll, float offset, boolean flip) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.yaw = yaw;
			this.pitch = pitch;
			this.roll = roll;
			this.offset = offset;
			this.flip = flip;
		}

		public Vec3 apply(Vec3 vec) {
			vec = vec.add(offset, 0, 0);
			vec = VecHelper.rotate(vec, roll, Axis.X);
			vec = VecHelper.rotate(vec, pitch, Axis.Z);
			vec = VecHelper.rotate(vec, yaw, Axis.Y);
			return vec.add(x, y, z);
		}

		public void apply(PoseStack ms, Vec3 camera) {
			TransformStack.cast(ms)
				.translate(camera.scale(-1)
					.add(x, y, z))
				.rotateY(yaw)
				.rotateZ(pitch)
				.rotateX(roll)
				.translate(offset, 0, 0)
				.rotateY(flip ? 180 : 0);
		}

	}

	public static void doDebugRender(Couple<MinecartController> c) {
		int yOffset = 1;
		MinecartController first = c.getFirst();
		AbstractMinecart mainCart = first.cart();
		Vec3 mainCenter = mainCart.position()
			.add(0, yOffset, 0);
		Vec3 connectedCenter = c.getSecond()
			.cart()
			.position()
			.add(0, yOffset, 0);

		int color = Color.mixColors(0xabf0e9, 0xee8572, (float) Mth
			.clamp(Math.abs(first.getCouplingLength(true) - connectedCenter.distanceTo(mainCenter)) * 8, 0, 1));

		CatnipClient.OUTLINER.showLine(mainCart.getId() + "", mainCenter, connectedCenter)
			.colored(color)
			.lineWidth(1 / 8f);

		Vec3 point = mainCart.position()
			.add(0, yOffset, 0);
		CatnipClient.OUTLINER.showLine(mainCart.getId() + "_dot", point, point.add(0, 1 / 128f, 0))
			.colored(0xffffff)
			.lineWidth(1 / 4f);
	}

}
