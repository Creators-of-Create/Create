package com.simibubi.create.modules.curiosities.zapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.utility.TessellatorHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@SuppressWarnings("deprecation")
@EventBusSubscriber(value = Dist.CLIENT)
public class ZapperRenderHandler {

	public static List<LaserBeam> cachedBeams;
	public static float leftHandAnimation;
	public static float rightHandAnimation;
	public static float lastLeftHandAnimation;
	public static float lastRightHandAnimation;

	private static boolean dontReequipLeft;
	private static boolean dontReequipRight;

	public static class LaserBeam {
		float itensity;
		Vec3d start;
		Vec3d end;
		boolean follow;
		boolean mainHand;

		public LaserBeam(Vec3d start, Vec3d end) {
			this.start = start;
			this.end = end;
			itensity = 1;
		}

		public LaserBeam followPlayer(boolean follow, boolean mainHand) {
			this.follow = follow;
			this.mainHand = mainHand;
			return this;
		}

		public Vec3d getStart() {
			if (follow)
				return getExactBarrelPos(mainHand);
			return start;
		}
	}

	public static Vec3d getExactBarrelPos(boolean mainHand) {
		float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
		ClientPlayerEntity player = Minecraft.getInstance().player;
		float yaw = (float) ((player.getYaw(partialTicks)) / -180 * Math.PI);
		float pitch = (float) ((player.getPitch(partialTicks)) / -180 * Math.PI);
		boolean rightHand = mainHand == (player.getPrimaryHand() == HandSide.RIGHT);
		float zOffset = ((float) Minecraft.getInstance().gameSettings.fov - 70) / -100;
		Vec3d barrelPosNoTransform = new Vec3d(rightHand ? -.35f : .35f, -0.115f, .75f + zOffset);
		Vec3d barrelPos = player.getEyePosition(partialTicks)
				.add(barrelPosNoTransform.rotatePitch(pitch).rotateYaw(yaw));
		return barrelPos;
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (event.phase == Phase.START)
			return;
		if (cachedBeams == null)
			cachedBeams = new LinkedList<>();
		ClientWorld world = Minecraft.getInstance().world;
		if (world == null)
			return;
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null)
			return;
		cachedBeams.removeIf(b -> b.itensity < .1f);
		cachedBeams.forEach(b -> b.itensity *= .4f);

		lastLeftHandAnimation = leftHandAnimation;
		lastRightHandAnimation = rightHandAnimation;
		leftHandAnimation *= 0.8f;
		rightHandAnimation *= 0.8f;
	}

	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event) {
		if (cachedBeams == null || cachedBeams.isEmpty())
			return;

		cachedBeams.forEach(beam -> {
			// TODO 1.15 buffered render
			TessellatorHelper.prepareForDrawing();
			RenderSystem.disableTexture();
			RenderSystem.lineWidth(beam.itensity * 40);

			BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
			bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
			bufferBuilder.vertex(beam.getStart().x, beam.getStart().y, beam.getStart().z).endVertex();
			bufferBuilder.vertex(beam.end.x, beam.end.y, beam.end.z).endVertex();
			Tessellator.getInstance().draw();

			RenderSystem.lineWidth(1);
			RenderSystem.enableTexture();
			TessellatorHelper.cleanUpAfterDrawing();
		});
	}

	public static void shoot(Hand hand) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		boolean rightHand = hand == Hand.MAIN_HAND ^ player.getPrimaryHand() == HandSide.LEFT;
		if (rightHand) {
			rightHandAnimation = .2f;
			dontReequipRight = false;
		} else {
			leftHandAnimation = .2f;
			dontReequipLeft = false;
		}
		playSound(hand, player.getPosition());
	}

	public static void playSound(Hand hand, BlockPos position) {
		float pitch = hand == Hand.MAIN_HAND ? 2f : 0.9f;
		Minecraft.getInstance().world.playSound(position, AllSoundEvents.BLOCKZAPPER_PLACE.get(), SoundCategory.BLOCKS, 0.8f, pitch, false);
	}

	public static void addBeam(LaserBeam beam) {
		Random r = new Random();
		double x = beam.end.x;
		double y = beam.end.y;
		double z = beam.end.z;
		ClientWorld world = Minecraft.getInstance().world;
		Supplier<Double> randomSpeed = () -> (r.nextDouble() - .5d) * .2f;
		Supplier<Double> randomOffset = () -> (r.nextDouble() - .5d) * .2f;
		for (int i = 0; i < 10; i++) {
			world.addParticle(ParticleTypes.END_ROD, x, y, z, randomSpeed.get(), randomSpeed.get(), randomSpeed.get());
			world.addParticle(ParticleTypes.FIREWORK, x + randomOffset.get(), y + randomOffset.get(),
					z + randomOffset.get(), 0, 0, 0);
		}

		cachedBeams.add(beam);
	}

	@SubscribeEvent
	public static void onRenderPlayerHand(RenderHandEvent event) {
		ItemStack heldItem = event.getItemStack();
		if (!(heldItem.getItem() instanceof ZapperItem))
			return;

		Minecraft mc = Minecraft.getInstance();
		boolean rightHand = event.getHand() == Hand.MAIN_HAND ^ mc.player.getPrimaryHand() == HandSide.LEFT;

		// TODO 1.15 buffered render
		RenderSystem.pushMatrix();

		float recoil = rightHand ? MathHelper.lerp(event.getPartialTicks(), lastRightHandAnimation, rightHandAnimation)
				: MathHelper.lerp(event.getPartialTicks(), lastLeftHandAnimation, leftHandAnimation);

		float equipProgress = event.getEquipProgress();

		if (rightHand && (rightHandAnimation > .01f || dontReequipRight))
			equipProgress = 0;
		if (!rightHand && (leftHandAnimation > .01f || dontReequipLeft))
			equipProgress = 0;
		
		// Render arm
		float f = rightHand ? 1.0F : -1.0F;
		float f1 = MathHelper.sqrt(event.getSwingProgress());
		float f2 = -0.3F * MathHelper.sin(f1 * (float) Math.PI);
		float f3 = 0.4F * MathHelper.sin(f1 * ((float) Math.PI * 2F));
		float f4 = -0.4F * MathHelper.sin(event.getSwingProgress() * (float) Math.PI);
		RenderSystem.translatef(f * (f2 + 0.64000005F - .1f), f3 + -0.4F + equipProgress * -0.6F,
				f4 + -0.71999997F + .3f + recoil);
		RenderSystem.rotatef(f * 75.0F, 0.0F, 1.0F, 0.0F);
		float f5 = MathHelper.sin(event.getSwingProgress() * event.getSwingProgress() * (float) Math.PI);
		float f6 = MathHelper.sin(f1 * (float) Math.PI);
		RenderSystem.rotatef(f * f6 * 70.0F, 0.0F, 1.0F, 0.0F);
		RenderSystem.rotatef(f * f5 * -20.0F, 0.0F, 0.0F, 1.0F);
		AbstractClientPlayerEntity abstractclientplayerentity = mc.player;
		mc.getTextureManager().bindTexture(abstractclientplayerentity.getLocationSkin());
		RenderSystem.translatef(f * -1.0F, 3.6F, 3.5F);
		RenderSystem.rotatef(f * 120.0F, 0.0F, 0.0F, 1.0F);
		RenderSystem.rotatef(200.0F, 1.0F, 0.0F, 0.0F);
		RenderSystem.rotatef(f * -135.0F, 0.0F, 1.0F, 0.0F);
		RenderSystem.translatef(f * 5.6F, 0.0F, 0.0F);
		RenderSystem.rotatef(f * 40.0F, 0.0F, 1.0F, 0.0F);
		PlayerRenderer playerrenderer = (PlayerRenderer) mc.getRenderManager().getRenderer(abstractclientplayerentity);
		RenderSystem.disableCull();
		if (rightHand) {
			playerrenderer.renderRightArm(abstractclientplayerentity);
		} else {
			playerrenderer.renderLeftArm(abstractclientplayerentity);
		}
		RenderSystem.enableCull();
		RenderSystem.popMatrix();

		// Render gun
		RenderSystem.pushMatrix();
		RenderSystem.translatef(f * (f2 + 0.64000005F - .1f), f3 + -0.4F + equipProgress * -0.6F,
				f4 + -0.71999997F - 0.1f + recoil);
		RenderSystem.rotatef(f * f6 * 70.0F, 0.0F, 1.0F, 0.0F);
		RenderSystem.rotatef(f * f5 * -20.0F, 0.0F, 0.0F, 1.0F);

		RenderSystem.translatef(f * -0.1f, 0.1f, -0.4f);
		RenderSystem.rotatef(f * 5.0F, 0.0F, 1.0F, 0.0F);

		FirstPersonRenderer firstPersonRenderer = mc.getFirstPersonRenderer();
		firstPersonRenderer.renderItemSide(mc.player, heldItem,
				rightHand ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
						: ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
				!rightHand);
		RenderSystem.popMatrix();

		event.setCanceled(true);
	}

	public static void dontAnimateItem(Hand hand) {
		boolean rightHand = hand == Hand.MAIN_HAND ^ Minecraft.getInstance().player.getPrimaryHand() == HandSide.LEFT;
		dontReequipRight |= rightHand;
		dontReequipLeft |= !rightHand;
	}

}
