package com.simibubi.create.content.equipment.symmetryWand;

import java.util.function.Consumer;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.symmetryWand.mirror.EmptyMirror;
import com.simibubi.create.content.equipment.symmetryWand.mirror.SymmetryMirror;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@EventBusSubscriber(Dist.CLIENT)
public class SymmetryWandClient {

	private static int tickCounter = 0;

	public static void initializeClient(SymmetryWandItem item, Consumer<IClientItemExtensions> consumer) {
		consumer.accept(SimpleCustomRenderer.create(item, new SymmetryWandItemRenderer()));
	}

	public static void openScreen(ItemStack wand, InteractionHand hand) {
		ScreenOpener.open(new SymmetryWandScreen(wand, hand));
	}

	@SubscribeEvent
	public static void onRenderWorld(RenderLevelStageEvent.AfterTranslucentParticles event) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null)
			return;
		RandomSource random = RandomSource.create();

		for (int i = 0; i < Inventory.getSelectionSize(); i++) {
			ItemStack stackInSlot = player.getInventory()
				.getItem(i);
			if (!AllItems.WAND_OF_SYMMETRY.isIn(stackInSlot))
				continue;
			if (!SymmetryWandItem.isEnabled(stackInSlot))
				continue;
			SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
			if (mirror instanceof EmptyMirror)
				continue;

			BlockPos pos = BlockPos.containing(mirror.getPosition());

			float yShift = 0;
			double speed = 1 / 16d;
			yShift = Mth.sin((float) (AnimationTickHolder.getRenderTime() * speed)) / 5f;

			// TODO 26.2: Rebuild symmetry mirror preview rendering on the new BlockModel pipeline.
		}
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;

		if (mc.level == null || player == null)
			return;
		if (mc.isPaused())
			return;

		tickCounter++;

		if (tickCounter % 10 == 0) {
			for (int i = 0; i < Inventory.getSelectionSize(); i++) {
				ItemStack stackInSlot = player.getInventory()
					.getItem(i);

				if (stackInSlot != null && AllItems.WAND_OF_SYMMETRY.isIn(stackInSlot)
					&& SymmetryWandItem.isEnabled(stackInSlot)) {

					SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
					if (mirror instanceof EmptyMirror)
						continue;

					RandomSource random = mc.level.getRandom();
					double offsetX = (random.nextDouble() - 0.5) * 0.3;
					double offsetZ = (random.nextDouble() - 0.5) * 0.3;

					Vec3 pos = mirror.getPosition()
						.add(0.5 + offsetX, 1 / 4d, 0.5 + offsetZ);
					Vec3 speed = new Vec3(0, random.nextDouble() * 1 / 8f, 0);
					mc.level.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
				}
			}
		}

	}

	public static void drawEffect(BlockPos from, BlockPos to) {
		ClientLevel level = Minecraft.getInstance().level;
		RandomSource random = level.getRandom();

		double density = 0.8f;
		Vec3 start = Vec3.atLowerCornerOf(from)
			.add(0.5, 0.5, 0.5);
		Vec3 end = Vec3.atLowerCornerOf(to)
			.add(0.5, 0.5, 0.5);
		Vec3 diff = end.subtract(start);

		Vec3 step = diff.normalize()
			.scale(density);
		int steps = (int) (diff.length() / step.length());

		for (int i = 3; i < steps - 1; i++) {
			Vec3 pos = start.add(step.scale(i));
			Vec3 speed = new Vec3(0, random.nextDouble() * -40f, 0);

			level.addParticle(new DustParticleOptions(0xffffff, 1), pos.x, pos.y,
				pos.z, speed.x, speed.y, speed.z);
		}

		Vec3 speed = new Vec3(0, random.nextDouble() * 1 / 32f, 0);
		Vec3 pos = start.add(step.scale(2));
		level.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y,
			speed.z);

		speed = new Vec3(0, random.nextDouble() * 1 / 32f, 0);
		pos = start.add(step.scale(steps));
		level.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y,
			speed.z);
	}

}
