package com.simibubi.create.content.curiosities.symmetry;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.curiosities.symmetry.mirror.EmptyMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.SymmetryMirror;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE)
public class SymmetryHandler {

	private static int tickCounter = 0;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockPlaced(EntityPlaceEvent event) {
		if (event.getWorld()
			.isRemote())
			return;
		if (!(event.getEntity() instanceof PlayerEntity))
			return;

		PlayerEntity player = (PlayerEntity) event.getEntity();
		PlayerInventory inv = player.inventory;
		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			if (!inv.getStackInSlot(i)
				.isEmpty()
				&& inv.getStackInSlot(i)
					.getItem() == AllItems.SYMMETRY_WAND.get()) {
				SymmetryWandItem.apply(player.world, inv.getStackInSlot(i), player, event.getPos(),
					event.getPlacedBlock());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockDestroyed(BreakEvent event) {
		if (event.getWorld()
			.isRemote())
			return;

		PlayerEntity player = event.getPlayer();
		PlayerInventory inv = player.inventory;
		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			if (!inv.getStackInSlot(i)
				.isEmpty() && AllItems.SYMMETRY_WAND.typeOf(inv.getStackInSlot(i))) {
				SymmetryWandItem.remove(player.world, inv.getStackInSlot(i), player, event.getPos());
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;

		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			ItemStack stackInSlot = player.inventory.getStackInSlot(i);
			if (!AllItems.SYMMETRY_WAND.typeOf(stackInSlot))
				continue;
			if (!SymmetryWandItem.isEnabled(stackInSlot))
				continue;
			SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
			if (mirror instanceof EmptyMirror)
				continue;

			BlockPos pos = new BlockPos(mirror.getPosition());

			float yShift = 0;
			double speed = 1 / 16d;
			yShift = MathHelper.sin((float) (AnimationTickHolder.getRenderTick() * speed)) / 5f;

			IRenderTypeBuffer buffer = Minecraft.getInstance()
				.getBufferBuilders()
				.getEntityVertexConsumers();
			ActiveRenderInfo info = mc.gameRenderer.getActiveRenderInfo();
			Vec3d view = info.getProjectedView();

			MatrixStack ms = event.getMatrixStack();
			ms.push();
			ms.translate(-view.getX(), -view.getY(), -view.getZ());
			ms.translate(pos.getX(), pos.getY(), pos.getZ());
			ms.translate(0, yShift + .2f, 0);
			mirror.applyModelTransform(ms);
			IBakedModel model = mirror.getModel()
				.get();
			IVertexBuilder builder = buffer.getBuffer(RenderType.getTranslucent());

			mc.getBlockRendererDispatcher()
				.getBlockModelRenderer()
				.renderModel(player.world, model, Blocks.AIR.getDefaultState(), pos, ms, builder, true,
					player.world.getRandom(), MathHelper.getPositionRandom(pos), OverlayTexture.DEFAULT_UV,
					EmptyModelData.INSTANCE);

			Minecraft.getInstance()
				.getBufferBuilders()
				.getEntityVertexConsumers()
				.draw(Atlases.getEntityTranslucent());

			ms.pop();
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (event.phase == Phase.START)
			return;
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;

		if (mc.world == null)
			return;
		if (mc.isGamePaused())
			return;

		tickCounter++;

		if (tickCounter % 10 == 0) {
			for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
				ItemStack stackInSlot = player.inventory.getStackInSlot(i);

				if (stackInSlot != null && AllItems.SYMMETRY_WAND.typeOf(stackInSlot)
					&& SymmetryWandItem.isEnabled(stackInSlot)) {

					SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
					if (mirror instanceof EmptyMirror)
						continue;

					Random r = new Random();
					double offsetX = (r.nextDouble() - 0.5) * 0.3;
					double offsetZ = (r.nextDouble() - 0.5) * 0.3;

					Vec3d pos = mirror.getPosition()
						.add(0.5 + offsetX, 1 / 4d, 0.5 + offsetZ);
					Vec3d speed = new Vec3d(0, r.nextDouble() * 1 / 8f, 0);
					mc.world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
				}
			}
		}

	}

	public static void drawEffect(BlockPos from, BlockPos to) {
		double density = 0.8f;
		Vec3d start = new Vec3d(from).add(0.5, 0.5, 0.5);
		Vec3d end = new Vec3d(to).add(0.5, 0.5, 0.5);
		Vec3d diff = end.subtract(start);

		Vec3d step = diff.normalize()
			.scale(density);
		int steps = (int) (diff.length() / step.length());

		Random r = new Random();
		for (int i = 3; i < steps - 1; i++) {
			Vec3d pos = start.add(step.scale(i));
			Vec3d speed = new Vec3d(0, r.nextDouble() * -40f, 0);

			Minecraft.getInstance().world.addParticle(new RedstoneParticleData(1, 1, 1, 1), pos.x, pos.y, pos.z,
				speed.x, speed.y, speed.z);
		}

		Vec3d speed = new Vec3d(0, r.nextDouble() * 1 / 32f, 0);
		Vec3d pos = start.add(step.scale(2));
		Minecraft.getInstance().world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y,
			speed.z);

		speed = new Vec3d(0, r.nextDouble() * 1 / 32f, 0);
		pos = start.add(step.scale(steps));
		Minecraft.getInstance().world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y,
			speed.z);
	}

}
