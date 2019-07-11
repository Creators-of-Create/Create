package com.simibubi.create.item;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllItems;
import com.simibubi.create.item.symmetry.SymmetryElement;
import com.simibubi.create.item.symmetry.SymmetryEmptySlot;
import com.simibubi.create.utility.TessellatorHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@EventBusSubscriber(bus = Bus.FORGE)
public class SymmetryHandler {

	private static int tickCounter = 0;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockPlaced(EntityPlaceEvent event) {
		if (event.getWorld().isRemote())
			return;
		if (!(event.getEntity() instanceof PlayerEntity))
			return;

		PlayerEntity player = (PlayerEntity) event.getEntity();
		PlayerInventory inv = player.inventory;
		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			if (!inv.getStackInSlot(i).isEmpty() && inv.getStackInSlot(i).getItem() == AllItems.SYMMETRY_WAND.get()) {
				ItemWandSymmetry.apply(player.world, inv.getStackInSlot(i), player, event.getPos(),
						event.getPlacedBlock());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockDestroyed(BreakEvent event) {
		if (event.getWorld().isRemote())
			return;
		
		PlayerEntity player = event.getPlayer();
		PlayerInventory inv = player.inventory;
		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			if (!inv.getStackInSlot(i).isEmpty() && AllItems.SYMMETRY_WAND.typeOf(inv.getStackInSlot(i))) {
				ItemWandSymmetry.remove(player.world, inv.getStackInSlot(i), player, event.getPos());
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;

		GL11.glEnable(GL11.GL_BLEND);

		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			ItemStack stackInSlot = player.inventory.getStackInSlot(i);
			if (stackInSlot != null && AllItems.SYMMETRY_WAND.typeOf(stackInSlot)
					&& ItemWandSymmetry.isEnabled(stackInSlot)) {
				SymmetryElement mirror = ItemWandSymmetry.getMirror(stackInSlot);
				if (mirror instanceof SymmetryEmptySlot)
					continue;

				TessellatorHelper.prepareForDrawing();
				BlockPos pos = new BlockPos(mirror.getPosition());

				float yShift = 0;
				double speed = 1 / 16d;
				yShift = MathHelper.sin((float) ((tickCounter + event.getPartialTicks()) * speed)) / 5f;

				BufferBuilder buffer = Tessellator.getInstance().getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
				GlStateManager.pushMatrix();
				GlStateManager.translated(0, yShift + .2f, 0);
				mc.getBlockRendererDispatcher().renderBlock(mirror.getModel(), pos, player.world, buffer, player.world.getRandom(),
						EmptyModelData.INSTANCE);
				Tessellator.getInstance().draw();
				GlStateManager.popMatrix();
				TessellatorHelper.cleanUpAfterDrawing();

			}
		}

		GL11.glDisable(GL11.GL_BLEND);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
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
						&& ItemWandSymmetry.isEnabled(stackInSlot)) {

					SymmetryElement mirror = ItemWandSymmetry.getMirror(stackInSlot);
					if (mirror instanceof SymmetryEmptySlot)
						continue;

					Random r = new Random();
					double offsetX = (r.nextDouble() - 0.5) * 0.3;
					double offsetZ = (r.nextDouble() - 0.5) * 0.3;

					Vec3d pos = mirror.getPosition().add(0.5 + offsetX, 1 / 4d, 0.5 + offsetZ);
					Vec3d speed = new Vec3d(0, r.nextDouble() * 1 / 8f, 0);
					mc.world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
				}
			}
		}

	}

	public static void drawEffect(BlockPos from, BlockPos to) {
		double density = 0.3f;
		Vec3d start = new Vec3d(from).add(0.5, 0.5, 0.5);
		Vec3d end = new Vec3d(to).add(0.5, 0.5, 0.5);
		Vec3d diff = end.subtract(start);

		Vec3d step = diff.normalize().scale(density);
		int steps = (int) (diff.length() / step.length());

		Random r = new Random();
		for (int i = 5; i < steps - 1; i++) {
			Vec3d pos = start.add(step.scale(i));
			Vec3d speed = new Vec3d(0, r.nextDouble() * -40f, 0);
			Minecraft.getInstance().world.addParticle(ParticleTypes.WITCH, pos.x, pos.y, pos.z, speed.x,
					speed.y, speed.z);
		}

		Vec3d speed = new Vec3d(0, r.nextDouble() * 1 / 32f, 0);
		Vec3d pos = start.add(step.scale(5));
		Minecraft.getInstance().world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y,
				speed.z);

		speed = new Vec3d(0, r.nextDouble() * 1 / 32f, 0);
		pos = start.add(step.scale(steps));
		Minecraft.getInstance().world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y,
				speed.z);
	}

}
