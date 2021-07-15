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
import net.minecraft.util.math.vector.Vector3d;
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
			.isClientSide())
			return;
		if (!(event.getEntity() instanceof PlayerEntity))
			return;

		PlayerEntity player = (PlayerEntity) event.getEntity();
		PlayerInventory inv = player.inventory;
		for (int i = 0; i < PlayerInventory.getSelectionSize(); i++) {
			if (!inv.getItem(i)
				.isEmpty()
				&& inv.getItem(i)
					.getItem() == AllItems.WAND_OF_SYMMETRY.get()) {
				SymmetryWandItem.apply(player.level, inv.getItem(i), player, event.getPos(),
					event.getPlacedBlock());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockDestroyed(BreakEvent event) {
		if (event.getWorld()
			.isClientSide())
			return;

		PlayerEntity player = event.getPlayer();
		PlayerInventory inv = player.inventory;
		for (int i = 0; i < PlayerInventory.getSelectionSize(); i++) {
			if (!inv.getItem(i)
				.isEmpty() && AllItems.WAND_OF_SYMMETRY.isIn(inv.getItem(i))) {
				SymmetryWandItem.remove(player.level, inv.getItem(i), player, event.getPos());
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;

		for (int i = 0; i < PlayerInventory.getSelectionSize(); i++) {
			ItemStack stackInSlot = player.inventory.getItem(i);
			if (!AllItems.WAND_OF_SYMMETRY.isIn(stackInSlot))
				continue;
			if (!SymmetryWandItem.isEnabled(stackInSlot))
				continue;
			SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
			if (mirror instanceof EmptyMirror)
				continue;

			BlockPos pos = new BlockPos(mirror.getPosition());

			float yShift = 0;
			double speed = 1 / 16d;
			yShift = MathHelper.sin((float) (AnimationTickHolder.getRenderTime() * speed)) / 5f;

			IRenderTypeBuffer.Impl buffer = Minecraft.getInstance()
				.renderBuffers()
				.bufferSource();
			ActiveRenderInfo info = mc.gameRenderer.getMainCamera();
			Vector3d view = info.getPosition();

			MatrixStack ms = event.getMatrixStack();
			ms.pushPose();
			ms.translate(-view.x(), -view.y(), -view.z());
			ms.translate(pos.getX(), pos.getY(), pos.getZ());
			ms.translate(0, yShift + .2f, 0);
			mirror.applyModelTransform(ms);
			IBakedModel model = mirror.getModel()
				.get();
			IVertexBuilder builder = buffer.getBuffer(RenderType.solid());

			mc.getBlockRenderer()
				.getModelRenderer()
				.renderModel(player.level, model, Blocks.AIR.defaultBlockState(), pos, ms, builder, true,
					player.level.getRandom(), MathHelper.getSeed(pos), OverlayTexture.NO_OVERLAY,
					EmptyModelData.INSTANCE);

			buffer.endBatch();
			ms.popPose();
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (event.phase == Phase.START)
			return;
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;

		if (mc.level == null)
			return;
		if (mc.isPaused())
			return;

		tickCounter++;

		if (tickCounter % 10 == 0) {
			for (int i = 0; i < PlayerInventory.getSelectionSize(); i++) {
				ItemStack stackInSlot = player.inventory.getItem(i);

				if (stackInSlot != null && AllItems.WAND_OF_SYMMETRY.isIn(stackInSlot)
					&& SymmetryWandItem.isEnabled(stackInSlot)) {

					SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
					if (mirror instanceof EmptyMirror)
						continue;

					Random r = new Random();
					double offsetX = (r.nextDouble() - 0.5) * 0.3;
					double offsetZ = (r.nextDouble() - 0.5) * 0.3;

					Vector3d pos = mirror.getPosition()
						.add(0.5 + offsetX, 1 / 4d, 0.5 + offsetZ);
					Vector3d speed = new Vector3d(0, r.nextDouble() * 1 / 8f, 0);
					mc.level.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
				}
			}
		}

	}

	public static void drawEffect(BlockPos from, BlockPos to) {
		double density = 0.8f;
		Vector3d start = Vector3d.atLowerCornerOf(from).add(0.5, 0.5, 0.5);
		Vector3d end = Vector3d.atLowerCornerOf(to).add(0.5, 0.5, 0.5);
		Vector3d diff = end.subtract(start);

		Vector3d step = diff.normalize()
			.scale(density);
		int steps = (int) (diff.length() / step.length());

		Random r = new Random();
		for (int i = 3; i < steps - 1; i++) {
			Vector3d pos = start.add(step.scale(i));
			Vector3d speed = new Vector3d(0, r.nextDouble() * -40f, 0);

			Minecraft.getInstance().level.addParticle(new RedstoneParticleData(1, 1, 1, 1), pos.x, pos.y, pos.z,
				speed.x, speed.y, speed.z);
		}

		Vector3d speed = new Vector3d(0, r.nextDouble() * 1 / 32f, 0);
		Vector3d pos = start.add(step.scale(2));
		Minecraft.getInstance().level.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y,
			speed.z);

		speed = new Vector3d(0, r.nextDouble() * 1 / 32f, 0);
		pos = start.add(step.scale(steps));
		Minecraft.getInstance().level.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y,
			speed.z);
	}

}
