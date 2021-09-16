package com.simibubi.create.content.curiosities.toolbox;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.ItemHandlerHelper;

public class ToolboxEquipPacket extends SimplePacketBase {

	private BlockPos toolboxPos;
	private int slot;
	private int hotbarSlot;

	public ToolboxEquipPacket(BlockPos toolboxPos, int slot, int hotbarSlot) {
		this.toolboxPos = toolboxPos;
		this.slot = slot;
		this.hotbarSlot = hotbarSlot;
	}

	public ToolboxEquipPacket(PacketBuffer buffer) {
		if (buffer.readBoolean())
			toolboxPos = buffer.readBlockPos();
		slot = buffer.readVarInt();
		hotbarSlot = buffer.readVarInt();
	}

	@Override
	public void write(PacketBuffer buffer) {
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
			ServerPlayerEntity player = ctx.getSender();
			World world = player.level;

			if (toolboxPos == null) {
				player.getPersistentData()
					.getCompound("CreateToolboxData")
					.remove(String.valueOf(hotbarSlot));
				sendData(player);
				return;
			}

			TileEntity blockEntity = world.getBlockEntity(toolboxPos);

			double maxRange = ToolboxHandler.getMaxRange(player);
			if (ctx.getSender()
				.distanceToSqr(toolboxPos.getX() + 0.5, toolboxPos.getY(),
					toolboxPos.getZ() + 0.5) > maxRange * maxRange)
				return;
			if (!(blockEntity instanceof ToolboxTileEntity))
				return;

			ToolboxHandler.unequip(player, hotbarSlot);

			if (slot < 0 || slot >= 8) {
				sendData(player);
				return;
			}

			ToolboxTileEntity toolboxTileEntity = (ToolboxTileEntity) blockEntity;

			ItemStack playerStack = player.inventory.getItem(hotbarSlot);
			if (!playerStack.isEmpty()
				&& !ItemHandlerHelper.canItemStacksStack(playerStack, toolboxTileEntity.inventory.filters.get(slot))) {
				ItemStack remainder =
					ItemHandlerHelper.insertItemStacked(toolboxTileEntity.inventory, playerStack, false);
				if (!remainder.isEmpty())
					remainder = ItemHandlerHelper.insertItemStacked(new ItemReturnInvWrapper(player.inventory),
						remainder, false);
				if (remainder.getCount() != playerStack.getCount())
					player.inventory.setItem(hotbarSlot, remainder);
			}

			CompoundNBT compound = player.getPersistentData()
				.getCompound("CreateToolboxData");
			String key = String.valueOf(hotbarSlot);

			CompoundNBT data = new CompoundNBT();
			data.putInt("Slot", slot);
			data.put("Pos", NBTUtil.writeBlockPos(toolboxPos));
			compound.put(key, data);

			player.getPersistentData()
				.put("CreateToolboxData", compound);
			sendData(player);

			toolboxTileEntity.connectPlayer(slot, player, hotbarSlot);
		});
		ctx.setPacketHandled(true);
	}

	private void sendData(ServerPlayerEntity player) {
		AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> player), new ISyncPersistentData.Packet(player));
	}

}
