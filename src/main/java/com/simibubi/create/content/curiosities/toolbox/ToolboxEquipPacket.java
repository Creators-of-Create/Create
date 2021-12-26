package com.simibubi.create.content.curiosities.toolbox;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.lib.transfer.item.ItemHandlerHelper;
import com.simibubi.create.lib.util.EntityHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

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
	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			ServerPlayer player = ctx.getSender();
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
			if (!(blockEntity instanceof ToolboxTileEntity))
				return;

			ToolboxHandler.unequip(player, hotbarSlot, false);

			if (slot < 0 || slot >= 8) {
				ToolboxHandler.syncData(player);
				return;
			}

			ToolboxTileEntity toolboxTileEntity = (ToolboxTileEntity) blockEntity;

			ItemStack playerStack = player.getInventory().getItem(hotbarSlot);
			if (!playerStack.isEmpty() && !ToolboxInventory.canItemsShareCompartment(playerStack,
				toolboxTileEntity.inventory.filters.get(slot))) {
				toolboxTileEntity.inventory.inLimitedMode(inventory -> {
					ItemStack remainder = ItemHandlerHelper.insertItemStacked(inventory, playerStack, false);
					if (!remainder.isEmpty())
						remainder = ItemHandlerHelper.insertItemStacked(new ItemReturnInvWrapper(player.getInventory()),
							remainder, false);
					if (remainder.getCount() != playerStack.getCount())
						player.getInventory().setItem(hotbarSlot, remainder);
				});
			}

			CompoundTag compound = EntityHelper.getExtraCustomData(player)
				.getCompound("CreateToolboxData");
			String key = String.valueOf(hotbarSlot);

			CompoundTag data = new CompoundTag();
			data.putInt("Slot", slot);
			data.put("Pos", NbtUtils.writeBlockPos(toolboxPos));
			compound.put(key, data);

			EntityHelper.getExtraCustomData(player)
				.put("CreateToolboxData", compound);

			toolboxTileEntity.connectPlayer(slot, player, hotbarSlot);
			ToolboxHandler.syncData(player);
		});
		ctx.setPacketHandled(true);
	}

}
