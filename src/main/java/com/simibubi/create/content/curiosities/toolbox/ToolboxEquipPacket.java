package com.simibubi.create.content.curiosities.toolbox;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkEvent.Context;

public class ToolboxEquipPacket extends SimplePacketBase {

	private BlockPos toolboxPos;
	private int slot;
	private int hotbarSlot;

	public ToolboxEquipPacket(BlockPos toolboxPos, int slot, int hotbarSlot) {
		this.toolboxPos = toolboxPos;
		this.slot = slot;
		this.hotbarSlot = hotbarSlot;
	}

	public ToolboxEquipPacket(FriendlyByteBuf buffer) {
		if (buffer.readBoolean())
			toolboxPos = buffer.readBlockPos();
		slot = buffer.readVarInt();
		hotbarSlot = buffer.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(toolboxPos != null);
		if (toolboxPos != null)
			buffer.writeBlockPos(toolboxPos);
		buffer.writeVarInt(slot);
		buffer.writeVarInt(hotbarSlot);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			Level world = player.level;

			if (toolboxPos == null) {
				ToolboxHandler.unequip(player, hotbarSlot, false);
				ToolboxHandler.syncData(player);
				return;
			}

			BlockEntity blockEntity = world.getBlockEntity(toolboxPos);

			double maxRange = ToolboxHandler.getMaxRange(player);
			if (player.distanceToSqr(toolboxPos.getX() + 0.5, toolboxPos.getY(), toolboxPos.getZ() + 0.5) > maxRange
				* maxRange)
				return;
			if (!(blockEntity instanceof ToolboxBlockEntity))
				return;

			ToolboxHandler.unequip(player, hotbarSlot, false);

			if (slot < 0 || slot >= 8) {
				ToolboxHandler.syncData(player);
				return;
			}

			ToolboxBlockEntity toolboxBlockEntity = (ToolboxBlockEntity) blockEntity;

			ItemStack playerStack = player.getInventory().getItem(hotbarSlot);
			if (!playerStack.isEmpty() && !ToolboxInventory.canItemsShareCompartment(playerStack,
				toolboxBlockEntity.inventory.filters.get(slot))) {
				toolboxBlockEntity.inventory.inLimitedMode(inventory -> {
					ItemStack remainder = ItemHandlerHelper.insertItemStacked(inventory, playerStack, false);
					if (!remainder.isEmpty())
						remainder = ItemHandlerHelper.insertItemStacked(new ItemReturnInvWrapper(player.getInventory()),
							remainder, false);
					if (remainder.getCount() != playerStack.getCount())
						player.getInventory().setItem(hotbarSlot, remainder);
				});
			}

			CompoundTag compound = player.getPersistentData()
				.getCompound("CreateToolboxData");
			String key = String.valueOf(hotbarSlot);

			CompoundTag data = new CompoundTag();
			data.putInt("Slot", slot);
			data.put("Pos", NbtUtils.writeBlockPos(toolboxPos));
			compound.put(key, data);

			player.getPersistentData()
				.put("CreateToolboxData", compound);

			toolboxBlockEntity.connectPlayer(slot, player, hotbarSlot);
			ToolboxHandler.syncData(player);
		});
		return true;
	}

}
