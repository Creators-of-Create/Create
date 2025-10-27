package com.simibubi.create.content.equipment.toolbox;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.items.ItemHandlerHelper;

public record ToolboxEquipPacket(BlockPos toolboxPos, int slot, int hotbarSlot) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, ToolboxEquipPacket> STREAM_CODEC = StreamCodec.composite(
			CatnipStreamCodecBuilders.nullable(BlockPos.STREAM_CODEC), ToolboxEquipPacket::toolboxPos,
			ByteBufCodecs.VAR_INT, ToolboxEquipPacket::slot,
			ByteBufCodecs.VAR_INT, ToolboxEquipPacket::hotbarSlot,
	        ToolboxEquipPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.TOOLBOX_EQUIP;
	}

	@Override
	public void handle(ServerPlayer player) {
		Level world = player.level();

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
		if (!(blockEntity instanceof ToolboxBlockEntity toolboxBlockEntity))
			return;

		ToolboxHandler.unequip(player, hotbarSlot, false);

		if (slot < 0 || slot >= 8) {
			ToolboxHandler.syncData(player);
			return;
		}

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
	}
}
