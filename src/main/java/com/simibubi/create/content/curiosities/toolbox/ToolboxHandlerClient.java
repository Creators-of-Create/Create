package com.simibubi.create.content.curiosities.toolbox;

import static com.simibubi.create.foundation.gui.AllGuiTextures.TOOLBELT_HOTBAR_OFF;
import static com.simibubi.create.foundation.gui.AllGuiTextures.TOOLBELT_HOTBAR_ON;
import static com.simibubi.create.foundation.gui.AllGuiTextures.TOOLBELT_SELECTED_OFF;
import static com.simibubi.create.foundation.gui.AllGuiTextures.TOOLBELT_SELECTED_ON;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ToolboxHandlerClient {

	static int COOLDOWN = 0;

	public static void clientTick() {
		if (COOLDOWN > 0 && !AllKeys.TOOLBELT.isPressed())
			COOLDOWN--;
	}

	public static boolean onPickItem() {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		if (player == null)
			return false;
		World level = player.level;
		RayTraceResult hitResult = mc.hitResult;

		if (hitResult == null || hitResult.getType() == RayTraceResult.Type.MISS)
			return false;
		if (player.isCreative())
			return false;

		ItemStack result = ItemStack.EMPTY;
		List<ToolboxTileEntity> toolboxes = ToolboxHandler.getNearest(player.level, player, 8);

		if (toolboxes.isEmpty())
			return false;

		if (hitResult.getType() == RayTraceResult.Type.BLOCK) {
			BlockPos pos = ((BlockRayTraceResult) hitResult).getBlockPos();
			BlockState state = level.getBlockState(pos);
			if (state.getMaterial() == Material.AIR)
				return false;
			result = state.getPickBlock(hitResult, level, pos, player);

		} else if (hitResult.getType() == RayTraceResult.Type.ENTITY) {
			Entity entity = ((EntityRayTraceResult) hitResult).getEntity();
			result = entity.getPickedResult(hitResult);
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
					new ToolboxEquipPacket(toolboxTileEntity.getBlockPos(), comp, player.inventory.selected));
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
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null)
			return;
		World level = player.level;

		List<ToolboxTileEntity> toolboxes = ToolboxHandler.getNearest(player.level, player, 8);
		
		if (!toolboxes.isEmpty())
			Collections.sort(toolboxes, (te1, te2) -> te1.getUniqueId()
				.compareTo(te2.getUniqueId()));

		CompoundNBT compound = player.getPersistentData()
			.getCompound("CreateToolboxData");

		String slotKey = String.valueOf(player.inventory.selected);
		boolean equipped = compound.contains(slotKey);

		if (equipped) {
			BlockPos pos = NBTUtil.readBlockPos(compound.getCompound(slotKey)
				.getCompound("Pos"));
			double max = ToolboxHandler.getMaxRange(player);
			boolean canReachToolbox = ToolboxHandler.distance(player.position(), pos) < max * max;

			if (canReachToolbox) {
				TileEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity instanceof ToolboxTileEntity) {
					RadialToolboxMenu screen = new RadialToolboxMenu(toolboxes,
						RadialToolboxMenu.State.SELECT_ITEM_UNEQUIP, (ToolboxTileEntity) blockEntity);
					screen.prevSlot(compound.getCompound(slotKey)
						.getInt("Slot"));
					ScreenOpener.open(screen);
					return;
				}
			}

			ScreenOpener.open(new RadialToolboxMenu(ImmutableList.of(), RadialToolboxMenu.State.DETACH, null));
			return;
		}

		if (toolboxes.isEmpty())
			return;

		if (toolboxes.size() == 1)
			ScreenOpener.open(new RadialToolboxMenu(toolboxes, RadialToolboxMenu.State.SELECT_ITEM, toolboxes.get(0)));
		else
			ScreenOpener.open(new RadialToolboxMenu(toolboxes, RadialToolboxMenu.State.SELECT_BOX, null));
	}

	public static void renderOverlay(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay,
		float partialTicks) {
		MainWindow mainWindow = Minecraft.getInstance()
			.getWindow();
		int x = mainWindow.getGuiScaledWidth() / 2 - 90;
		int y = mainWindow.getGuiScaledHeight() - 23;
		RenderSystem.enableDepthTest();

		PlayerEntity player = Minecraft.getInstance().player;
		CompoundNBT persistentData = player.getPersistentData();
		if (!persistentData.contains("CreateToolboxData"))
			return;

		CompoundNBT compound = player.getPersistentData()
			.getCompound("CreateToolboxData");

		if (compound.isEmpty())
			return;

		ms.pushPose();
		for (int slot = 0; slot < 9; slot++) {
			String key = String.valueOf(slot);
			if (!compound.contains(key))
				continue;
			BlockPos pos = NBTUtil.readBlockPos(compound.getCompound(key)
				.getCompound("Pos"));
			double max = ToolboxHandler.getMaxRange(player);
			boolean selected = player.inventory.selected == slot;
			int offset = selected ? 1 : 0;
			AllGuiTextures texture = ToolboxHandler.distance(player.position(), pos) < max * max
				? selected ? TOOLBELT_SELECTED_ON : TOOLBELT_HOTBAR_ON
				: selected ? TOOLBELT_SELECTED_OFF : TOOLBELT_HOTBAR_OFF;
			texture.draw(ms, x + 20 * slot - offset, y + offset);
		}
		ms.popPose();
	}

}
