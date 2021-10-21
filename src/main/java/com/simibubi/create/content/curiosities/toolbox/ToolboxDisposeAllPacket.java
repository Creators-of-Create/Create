package com.simibubi.create.content.curiosities.toolbox;

import java.util.function.Supplier;

import org.apache.commons.lang3.mutable.MutableBoolean;

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
import net.minecraftforge.items.ItemHandlerHelper;

public class ToolboxDisposeAllPacket extends SimplePacketBase {

	private BlockPos toolboxPos;

	public ToolboxDisposeAllPacket(BlockPos toolboxPos) {
		this.toolboxPos = toolboxPos;
	}

	public ToolboxDisposeAllPacket(PacketBuffer buffer) {
		toolboxPos = buffer.readBlockPos();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeBlockPos(toolboxPos);
	}

	@Override
	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			ServerPlayerEntity player = ctx.getSender();
			World world = player.level;
			TileEntity blockEntity = world.getBlockEntity(toolboxPos);

			double maxRange = ToolboxHandler.getMaxRange(player);
			if (player.distanceToSqr(toolboxPos.getX() + 0.5, toolboxPos.getY(), toolboxPos.getZ() + 0.5) > maxRange
				* maxRange)
				return;
			if (!(blockEntity instanceof ToolboxTileEntity))
				return;
			ToolboxTileEntity toolbox = (ToolboxTileEntity) blockEntity;

			CompoundNBT compound = player.getPersistentData()
				.getCompound("CreateToolboxData");
			MutableBoolean sendData = new MutableBoolean(false);

			toolbox.inventory.inLimitedMode(inventory -> {
				for (int i = 0; i < 36; i++) {
					String key = String.valueOf(i);
					if (compound.contains(key) && NBTUtil.readBlockPos(compound.getCompound(key)
						.getCompound("Pos"))
						.equals(toolboxPos)) {
						ToolboxHandler.unequip(player, i, true);
						sendData.setTrue();
					}
					
					ItemStack itemStack = player.inventory.getItem(i);
					ItemStack remainder = ItemHandlerHelper.insertItemStacked(toolbox.inventory, itemStack, false);
					if (remainder.getCount() != itemStack.getCount())
						player.inventory.setItem(i, remainder);
				}
			});
			
			if (sendData.booleanValue())
				ToolboxHandler.syncData(player);

		});
		ctx.setPacketHandled(true);
	}

}
