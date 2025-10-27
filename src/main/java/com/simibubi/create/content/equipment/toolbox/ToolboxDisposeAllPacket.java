package com.simibubi.create.content.equipment.toolbox;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllPackets;

import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.items.ItemHandlerHelper;

public record ToolboxDisposeAllPacket(BlockPos toolboxPos) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, ToolboxDisposeAllPacket> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
			ToolboxDisposeAllPacket::new, ToolboxDisposeAllPacket::toolboxPos
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.TOOLBOX_DISPOSE_ALL;
	}

	@Override
	public void handle(ServerPlayer player) {
		Level world = player.level();
		BlockEntity blockEntity = world.getBlockEntity(toolboxPos);

		double maxRange = ToolboxHandler.getMaxRange(player);
		if (player.distanceToSqr(toolboxPos.getX() + 0.5, toolboxPos.getY(), toolboxPos.getZ() + 0.5) > maxRange
				* maxRange)
			return;
		if (!(blockEntity instanceof ToolboxBlockEntity toolbox))
			return;

		CompoundTag compound = player.getPersistentData()
				.getCompound("CreateToolboxData");
		MutableBoolean sendData = new MutableBoolean(false);

		toolbox.inventory.inLimitedMode(inventory -> {
			for (int i = 0; i < 36; i++) {
				String key = String.valueOf(i);
				if (compound.contains(key) && NBTHelper.readBlockPos(compound.getCompound(key), "Pos")
					.equals(toolboxPos)) {
					ToolboxHandler.unequip(player, i, true);
					sendData.setTrue();
				}

				ItemStack itemStack = player.getInventory().getItem(i);
				ItemStack remainder = ItemHandlerHelper.insertItemStacked(toolbox.inventory, itemStack, false);
				if (remainder.getCount() != itemStack.getCount())
					player.getInventory().setItem(i, remainder);
			}
		});

		if (sendData.booleanValue())
			ToolboxHandler.syncData(player);
	}
}
