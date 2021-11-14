package com.simibubi.create.content.curiosities.toolbox;

import static com.simibubi.create.foundation.gui.AllGuiTextures.TOOLBELT_HOTBAR_OFF;
import static com.simibubi.create.foundation.gui.AllGuiTextures.TOOLBELT_HOTBAR_ON;
import static com.simibubi.create.foundation.gui.AllGuiTextures.TOOLBELT_SELECTED_OFF;
import static com.simibubi.create.foundation.gui.AllGuiTextures.TOOLBELT_SELECTED_ON;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.AllPackets;

import com.simibubi.create.lib.helper.EntityHelper;

import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.fabricmc.fabric.api.entity.EntityPickInteractionAware;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ToolboxHandlerClient {

	static int COOLDOWN = 0;

	public static void clientTick() {
		if (COOLDOWN > 0 && !AllKeys.TOOLBELT.isPressed())
			COOLDOWN--;
	}

	public static boolean onPickItem() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null)
			return false;
		Level level = player.level;
		HitResult hitResult = mc.hitResult;

		if (hitResult == null || hitResult.getType() == HitResult.Type.MISS)
			return false;
		if (player.isCreative())
			return false;

		ItemStack result = ItemStack.EMPTY;
		List<ToolboxTileEntity> toolboxes = ToolboxHandler.getNearest(player.level, player, 8);

		if (toolboxes.isEmpty())
			return false;

		if (hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
			BlockState state = level.getBlockState(pos);
			if (state.getMaterial() == Material.AIR)
				return false;
			if (state.getBlock() instanceof BlockPickInteractionAware aware) {
				result = aware.getPickedStack(state, level, pos, player, hitResult);
			}

		} else if (hitResult.getType() == HitResult.Type.ENTITY) {
			Entity entity = ((EntityHitResult) hitResult).getEntity();
			if (entity instanceof EntityPickInteractionAware aware) {
				result = aware.getPickedStack(player, hitResult);
			}
		}

		if (result.isEmpty())
			return false;

		for (ToolboxTileEntity toolboxTileEntity : toolboxes) {
			ToolboxInventory inventory = toolboxTileEntity.inventory;
			for (int comp = 0; comp < 8; comp++) {
				ItemStack inSlot = inventory.takeFromCompartment(1, comp, true);
				if (inSlot.isEmpty())
					continue;
				if (inSlot.getItem() != result.getItem())
					continue;
				if (!ItemStack.tagMatches(inSlot, result))
					continue;

				AllPackets.channel.sendToServer(
					new ToolboxEquipPacket(toolboxTileEntity.getBlockPos(), comp, player.getInventory().selected));
				return true;
			}

		}

		return false;
	}

	public static void onKeyInput(int key, boolean pressed) {
		if (key != AllKeys.TOOLBELT.getBoundCode())
			return;
		if (COOLDOWN > 0)
			return;
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;
		Level level = player.level;

		List<ToolboxTileEntity> toolboxes = ToolboxHandler.getNearest(player.level, player, 8);
		CompoundTag compound = EntityHelper.getExtraCustomData(player)
			.getCompound("CreateToolboxData");

		String slotKey = String.valueOf(player.getInventory().selected);
		boolean equipped = compound.contains(slotKey);

		if (equipped) {
			BlockPos pos = NbtUtils.readBlockPos(compound.getCompound(slotKey)
				.getCompound("Pos"));
			double max = ToolboxHandler.getMaxRange(player);
			boolean canReachToolbox = ToolboxHandler.distance(player.position(), pos) < max * max;

			if (canReachToolbox) {
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity instanceof ToolboxTileEntity) {
					RadialToolboxMenu screen = new RadialToolboxMenu(ImmutableList.of((ToolboxTileEntity) blockEntity),
						RadialToolboxMenu.State.SELECT_ITEM_UNEQUIP);
					screen.prevSlot(compound.getCompound(slotKey)
						.getInt("Slot"));
					ScreenOpener.open(screen);
					return;
				}
			}

			ScreenOpener.open(new RadialToolboxMenu(ImmutableList.of(), RadialToolboxMenu.State.DETACH));
			return;
		}

		if (toolboxes.isEmpty())
			return;

		if (toolboxes.size() == 1)
			ScreenOpener.open(new RadialToolboxMenu(toolboxes, RadialToolboxMenu.State.SELECT_ITEM));
		else
			ScreenOpener.open(new RadialToolboxMenu(toolboxes, RadialToolboxMenu.State.SELECT_BOX));
	}

	public static void renderOverlay(PoseStack ms, MultiBufferSource buffer, int light, int overlay,
		float partialTicks) {
		Window mainWindow = Minecraft.getInstance()
			.getWindow();
		int x = mainWindow.getGuiScaledWidth() / 2 - 90;
		int y = mainWindow.getGuiScaledHeight() - 23;
		RenderSystem.enableDepthTest();

		Player player = Minecraft.getInstance().player;
		CompoundTag persistentData = EntityHelper.getExtraCustomData(player);
		if (!persistentData.contains("CreateToolboxData"))
			return;

		CompoundTag compound = EntityHelper.getExtraCustomData(player)
			.getCompound("CreateToolboxData");

		if (compound.isEmpty())
			return;

		ms.pushPose();
		for (int slot = 0; slot < 9; slot++) {
			String key = String.valueOf(slot);
			if (!compound.contains(key))
				continue;
			BlockPos pos = NbtUtils.readBlockPos(compound.getCompound(key)
				.getCompound("Pos"));
			double max = ToolboxHandler.getMaxRange(player);
			boolean selected = player.getInventory().selected == slot;
			int offset = selected ? 1 : 0;
			AllGuiTextures texture = ToolboxHandler.distance(player.position(), pos) < max * max
				? selected ? TOOLBELT_SELECTED_ON : TOOLBELT_HOTBAR_ON
				: selected ? TOOLBELT_SELECTED_OFF : TOOLBELT_HOTBAR_OFF;
			texture.draw(ms, x + 20 * slot - offset, y + offset);
		}
		ms.popPose();
	}

}
